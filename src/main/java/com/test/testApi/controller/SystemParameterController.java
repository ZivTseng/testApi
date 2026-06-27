package com.test.testApi.controller;

import com.test.testApi.dto.req.SystemParameterReq;
import com.test.testApi.dto.res.SystemParameterRes;
import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.SystemParameter;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.SystemParameterRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/system-parameters")
@RequiredArgsConstructor
public class SystemParameterController {

    private final SystemParameterRepository systemParameterRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<SystemParameterRes> list() {
        return systemParameterRepository.findAll().stream().map(SystemParameterRes::from).toList();
    }

    @PutMapping("/{key}")
    public SystemParameterRes update(@PathVariable("key") String key, @Valid @RequestBody SystemParameterReq req, Authentication authentication) {
        SystemParameter param = systemParameterRepository.findByParamKey(key)
                .orElseThrow(() -> new RuntimeException("找不到系統參數，Key: " + key));

        String oldValue = param.getParamValue();
        param.setParamValue(req.getParamValue());
        systemParameterRepository.save(param);

        String operator = authentication != null ? authentication.getName() : "SYSTEM";
        AuditLog log = new AuditLog();
        log.setOperator(operator);
        log.setAction("UPDATE_SYSTEM_PARAMETER");
        log.setTargetType("SystemParameter");
        log.setTargetId(param.getId());
        log.setDetail("參數「" + key + "」由 " + oldValue + " 調整為 " + req.getParamValue());
        auditLogRepository.save(log);

        return SystemParameterRes.from(param);
    }
}
