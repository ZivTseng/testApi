package com.test.testApi.line;

import com.test.testApi.config.LineProperties;
import com.test.testApi.entity.Parent;
import com.test.testApi.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineMessagingService {

    private static final String REPLY_URL = "https://api.line.me/v2/bot/message/reply";
    private static final String PUSH_URL = "https://api.line.me/v2/bot/message/push";

    private final LineProperties lineProperties;
    private final RestTemplate restTemplate;

    /** 驗證 LINE Webhook 請求的 X-Line-Signature，確保請求真的來自 LINE 平台 */
    public boolean isValidSignature(String requestBody, String signatureHeader) {
        if (signatureHeader == null) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(lineProperties.getChannelSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(requestBody.getBytes(StandardCharsets.UTF_8));
            String computedSignature = Base64.getEncoder().encodeToString(computed);
            return MessageDigest.isEqual(computedSignature.getBytes(StandardCharsets.UTF_8), signatureHeader.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("LINE 簽名驗證失敗", e);
            return false;
        }
    }

    public void reply(String replyToken, String text) {
        if (!isConfigured()) {
            log.warn("LINE 尚未設定 channel access token，略過回覆訊息");
            return;
        }
        Map<String, Object> body = Map.of(
                "replyToken", replyToken,
                "messages", List.of(Map.of("type", "text", "text", text))
        );
        send(REPLY_URL, body);
    }

    public void push(String lineUserId, String text) {
        if (!isConfigured()) {
            log.warn("LINE 尚未設定 channel access token，略過推送訊息");
            return;
        }
        if (lineUserId == null || lineUserId.isBlank()) {
            return;
        }
        Map<String, Object> body = Map.of(
                "to", lineUserId,
                "messages", List.of(Map.of("type", "text", "text", text))
        );
        send(PUSH_URL, body);
    }

    /** 推送訊息給某學員綁定 LINE 的所有家長 */
    public void pushToStudentParents(Student student, String text) {
        for (Parent parent : student.getParents()) {
            if (parent.getLineUserId() != null && !parent.getLineUserId().isBlank()) {
                push(parent.getLineUserId(), text);
            }
        }
    }

    private void send(String url, Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(lineProperties.getChannelAccessToken());
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        } catch (Exception e) {
            log.error("呼叫 LINE Messaging API 失敗: {}", e.getMessage());
        }
    }

    private boolean isConfigured() {
        return lineProperties.getChannelAccessToken() != null && !lineProperties.getChannelAccessToken().isBlank();
    }
}
