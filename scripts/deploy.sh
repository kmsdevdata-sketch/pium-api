#!/usr/bin/env bash

# live 슬롯을 확인하고 반대쪽 슬롯에 새 이미지를 띄운 뒤,actuator/health가 통과하면
# /etc/nginx/conf.d/pium-upstream.conf 를 새포트로 바꾸고 Nginx를 reload한다
set -euo pipefail

APP_DIR="${APP_DIR:-/opt/pium}"
COMPOSE_FILE="${APP_DIR}/compose.deploy.yaml"
ENV_FILE="${APP_DIR}/.env"
UPSTREAM_FILE="${UPSTREAM_FILE:-/etc/nginx/conf.d/pium-upstream.conf}"
STATE_FILE="${STATE_FILE:-${APP_DIR}/.active_slot}"
BLUE_PORT="${APP_BLUE_PORT:-8081}"
GREEN_PORT="${APP_GREEN_PORT:-8082}"

: "${APP_IMAGE:?APP_IMAGE is required}"
: "${GHCR_USERNAME:?GHCR_USERNAME is required}"
: "${GHCR_TOKEN:?GHCR_TOKEN is required}"

if [[ ! -f "${COMPOSE_FILE}" ]]; then
  echo "compose.deploy.yaml not found: ${COMPOSE_FILE}"
  exit 1
fi

if [[ ! -f "${ENV_FILE}" ]]; then
  echo ".env not found: ${ENV_FILE}"
  exit 1
fi

cd "${APP_DIR}"

echo "${GHCR_TOKEN}" | docker login ghcr.io -u "${GHCR_USERNAME}" --password-stdin

export APP_IMAGE

current_slot=""
if [[ -f "${STATE_FILE}" ]]; then
  current_slot="$(tr -d '[:space:]' < "${STATE_FILE}")"
fi

if [[ -z "${current_slot}" && -f "${UPSTREAM_FILE}" ]]; then
  if grep -q "127.0.0.1:${BLUE_PORT}" "${UPSTREAM_FILE}"; then
    current_slot="blue"
  elif grep -q "127.0.0.1:${GREEN_PORT}" "${UPSTREAM_FILE}"; then
    current_slot="green"
  fi
fi

if [[ "${current_slot}" == "blue" ]]; then
  target_slot="green"
  target_port="${GREEN_PORT}"
  old_slot="blue"
elif [[ "${current_slot}" == "green" ]]; then
  target_slot="blue"
  target_port="${BLUE_PORT}"
  old_slot="green"
else
  target_slot="blue"
  target_port="${BLUE_PORT}"
  old_slot=""
fi

target_service="app-${target_slot}"
old_service=""
if [[ -n "${old_slot}" ]]; then
  old_service="app-${old_slot}"
fi

docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d mysql
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" pull "${target_service}"
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" up -d --no-deps "${target_service}"

set -a
source "${ENV_FILE}"
set +a

HEALTH_URL="http://127.0.0.1:${target_port}/actuator/health"

for i in {1..60}; do
  if curl -fsS "${HEALTH_URL}" >/dev/null; then
    cat <<EOF | sudo tee "${UPSTREAM_FILE}" >/dev/null
upstream pium_app {
    server 127.0.0.1:${target_port};
    keepalive 32;
}
EOF

    sudo nginx -t
    sudo systemctl reload nginx

    printf '%s\n' "${target_slot}" > "${STATE_FILE}"

    if [[ -n "${old_service}" ]]; then
      docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" stop "${old_service}"
    fi

    echo "Deployment successful: ${target_service} is now live on port ${target_port}"
    exit 0
  fi
  sleep 2
done

echo "Health check failed: ${HEALTH_URL}"
docker compose --env-file "${ENV_FILE}" -f "${COMPOSE_FILE}" logs --tail=100 "${target_service}" || true
exit 1
