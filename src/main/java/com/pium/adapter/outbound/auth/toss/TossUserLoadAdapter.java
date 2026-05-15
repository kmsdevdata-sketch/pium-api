package com.pium.adapter.outbound.auth.toss;

import com.pium.adapter.outbound.auth.exception.AuthAdapterErrorCode;
import com.pium.adapter.outbound.auth.exception.AuthAdapterException;
import com.pium.application.auth.required.LoadTossUserPort;
import com.pium.application.auth.required.dto.TossAuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossUserLoadAdapter implements LoadTossUserPort {


    private static final String BEARER_PREFIX = "Bearer ";
    private static final int IV_LENGTH = 12;
    private static final int AUTH_TAG_BIT_LENGTH = 16 * Byte.SIZE;

    private final RestClient.Builder restClientBuilder;
    private final TossAuthProperties tossAuthProperties;

    @Override
    public TossAuthenticatedUser load(String accessToken) {
        TossUserResponse response = restClientBuilder.build()
                .get()
                .uri(tossAuthProperties.baseUrl() + "/api-partner/v1/apps-in-toss/user/oauth2/login-me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(TossUserResponse.class);

        if (response == null || response.success() == null) {
            throw new AuthAdapterException(AuthAdapterErrorCode.TOSS_USER_LOAD_FAILED);
        }

        TossUserResponse.Success success = response.success();

        return new TossAuthenticatedUser(
                String.valueOf(success.userKey()),
                decryptName(success.name())
        );
    }

    private String decryptName(String encryptedName) {
        if (encryptedName == null || encryptedName.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedName);
            byte[] keyBytes = Base64.getDecoder().decode(tossAuthProperties.decryptKey());

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
            GCMParameterSpec nonceSpec = new GCMParameterSpec(AUTH_TAG_BIT_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, key, nonceSpec);
            cipher.updateAAD(tossAuthProperties.aad().getBytes(StandardCharsets.UTF_8));

            byte[] decrypted = cipher.doFinal(decoded, IV_LENGTH, decoded.length - IV_LENGTH);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to decrypt Toss user name", e);
        }
    }
}
