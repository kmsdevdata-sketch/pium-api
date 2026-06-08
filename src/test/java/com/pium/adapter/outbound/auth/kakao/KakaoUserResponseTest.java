package com.pium.adapter.outbound.auth.kakao;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KakaoUserResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void 카카오_사용자정보_JSON에서_id와_profile_nickname을_파싱한다() throws Exception {
        String json = """
                {
                  "id": 123456789,
                  "kakao_account": {
                    "profile_nickname_needs_agreement": false,
                    "profile": {
                      "nickname": "카카오사용자"
                    }
                  }
                }
                """;

        KakaoUserResponse response = objectMapper.readValue(json, KakaoUserResponse.class);

        assertThat(response.id()).isEqualTo(123456789L);
        assertThat(response.kakaoAccount().profile().nickname()).isEqualTo("카카오사용자");
    }
}
