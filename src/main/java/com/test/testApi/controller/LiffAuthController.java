package com.test.testApi.controller;

import com.test.testApi.dto.req.LiffAuthReq;
import com.test.testApi.dto.req.LiffLinkReq;
import com.test.testApi.dto.res.LiffAuthRes;
import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.Parent;
import com.test.testApi.line.LineIdTokenVerifier;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.ParentRepository;
import com.test.testApi.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/liff")
@RequiredArgsConstructor
public class LiffAuthController {

    private final LineIdTokenVerifier lineIdTokenVerifier;
    private final ParentRepository parentRepository;
    private final AuditLogRepository auditLogRepository;
    private final JwtUtils jwtUtils;

    // 家長打開 LIFF 頁面時呼叫：用 LIFF 拿到的 ID Token 換取家長端 Token。若這個 LINE 帳號還沒綁過家長資料，回傳 linked=false
    @PostMapping("/auth")
    public LiffAuthRes auth(@Valid @RequestBody LiffAuthReq req) {
        LineIdTokenVerifier.VerifiedProfile profile = lineIdTokenVerifier.verify(req.getIdToken());

        return parentRepository.findByLineUserId(profile.userId())
                .map(parent -> LiffAuthRes.linked(jwtUtils.generateParentJwtToken(parent.getId()), parent.getName()))
                .orElseGet(LiffAuthRes::notLinked);
    }

    // 首次使用：LINE 帳號還沒綁定家長資料時，用館方已登記的電話完成綁定（不需要再額外輸入驗證碼，因為 LIFF 登入本身已經驗證過是這個人）
    @PostMapping("/link")
    public LiffAuthRes link(@Valid @RequestBody LiffLinkReq req) {
        LineIdTokenVerifier.VerifiedProfile profile = lineIdTokenVerifier.verify(req.getIdToken());

        Parent parent = parentRepository.findFirstByPhoneAndLineUserIdIsNull(req.getPhone())
                .orElseThrow(() -> new RuntimeException("查無符合此電話號碼且尚未綁定的家長資料，請聯絡館方確認登記電話"));

        parent.setLineUserId(profile.userId());
        parentRepository.save(parent);

        AuditLog log = new AuditLog();
        log.setOperator("LINE_LIFF");
        log.setAction("BIND_LINE_ACCOUNT");
        log.setTargetType("Parent");
        log.setTargetId(parent.getId());
        log.setDetail("家長 " + parent.getName() + " 透過 LIFF 電話綁定完成 LINE 帳號綁定");
        auditLogRepository.save(log);

        return LiffAuthRes.linked(jwtUtils.generateParentJwtToken(parent.getId()), parent.getName());
    }
}
