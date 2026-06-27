package com.test.testApi.line;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.Parent;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.ParentRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/line")
@RequiredArgsConstructor
public class LineWebhookController {

    private final LineMessagingService lineMessagingService;
    private final ParentRepository parentRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(HttpServletRequest request) throws Exception {
        String body;
        try (BufferedReader reader = request.getReader()) {
            body = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        }

        String signature = request.getHeader("X-Line-Signature");
        if (!lineMessagingService.isValidSignature(body, signature)) {
            log.warn("收到簽名驗證失敗的 LINE Webhook 請求");
            return ResponseEntity.status(401).build();
        }

        LineWebhookEventDto.Root root = objectMapper.readValue(body, LineWebhookEventDto.Root.class);
        if (root.getEvents() != null) {
            for (LineWebhookEventDto event : root.getEvents()) {
                handleEvent(event);
            }
        }
        return ResponseEntity.ok().build();
    }

    private void handleEvent(LineWebhookEventDto event) {
        String userId = event.getSource() != null ? event.getSource().getUserId() : null;

        if ("follow".equals(event.getType())) {
            lineMessagingService.reply(event.getReplyToken(),
                    "歡迎加入幼兒體能館！\n請輸入館方提供給您的「綁定驗證碼」，完成帳號綁定後即可收到預約相關通知。");
            return;
        }

        if (!"message".equals(event.getType()) || event.getMessage() == null
                || !"text".equals(event.getMessage().getType())) {
            return;
        }

        String text = event.getMessage().getText() == null ? "" : event.getMessage().getText().trim();
        bindByCode(text, userId, event.getReplyToken());
    }

    private void bindByCode(String code, String lineUserId, String replyToken) {
        if (code.isEmpty() || lineUserId == null) {
            return;
        }
        Parent parent = parentRepository.findByBindingCodeAndBindingCodeExpireAtAfter(code, LocalDateTime.now())
                .orElse(null);

        if (parent == null) {
            lineMessagingService.reply(replyToken, "查無此綁定驗證碼，請確認是否輸入正確，或聯絡館方重新取得驗證碼。");
            return;
        }

        parent.setLineUserId(lineUserId);
        parent.setBindingCode(null);
        parent.setBindingCodeExpireAt(null);
        parentRepository.save(parent);

        AuditLog log = new AuditLog();
        log.setOperator("LINE_BOT");
        log.setAction("BIND_LINE_ACCOUNT");
        log.setTargetType("Parent");
        log.setTargetId(parent.getId());
        log.setDetail("家長 " + parent.getName() + " 完成 LINE 帳號綁定");
        auditLogRepository.save(log);

        lineMessagingService.reply(replyToken, "綁定成功！您好，" + parent.getName() + "，之後預約相關通知都會發送到這裡。");
    }
}
