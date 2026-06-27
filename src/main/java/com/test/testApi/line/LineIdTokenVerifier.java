package com.test.testApi.line;

import com.test.testApi.config.LineProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 驗證家長端 LIFF 登入時拿到的 ID Token，確保真的是 LINE 平台發出、且是發給我們這個 channel 的，
 * 並從中取出可信任的 LINE userId（sub），避免前端直接帶 userId 過來被冒用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LineIdTokenVerifier {

    private static final String VERIFY_URL = "https://api.line.me/oauth2/v2.1/verify";

    private final LineProperties lineProperties;
    private final RestTemplate restTemplate;

    public VerifiedProfile verify(String idToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("id_token", idToken);
        form.add("client_id", lineProperties.getChannelId());

        VerifyResponse res = restTemplate.postForObject(VERIFY_URL, new HttpEntity<>(form, headers), VerifyResponse.class);
        if (res == null || res.getSub() == null) {
            throw new RuntimeException("LINE ID Token 驗證失敗");
        }
        return new VerifiedProfile(res.getSub(), res.getName());
    }

    @Data
    private static class VerifyResponse {
        private String sub;
        private String name;
        private String aud;
    }

    public record VerifiedProfile(String userId, String name) {
    }
}
