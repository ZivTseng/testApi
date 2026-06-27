package com.test.testApi.scheduler;

import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.Waitlist;
import com.test.testApi.entity.enums.WaitlistStatus;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.WaitlistRepository;
import com.test.testApi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 定期檢查候補保留時間是否逾期，逾期則作廢並通知下一位候補
@Component
@RequiredArgsConstructor
public class WaitlistExpiryScheduler {

    private final WaitlistRepository waitlistRepository;
    private final AuditLogRepository auditLogRepository;
    private final ReservationService reservationService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void expireOverdueWaitlists() {
        List<Waitlist> overdue = waitlistRepository.findByStatusAndExpireAtBefore(WaitlistStatus.NOTIFIED, LocalDateTime.now());
        for (Waitlist waitlist : overdue) {
            waitlist.setStatus(WaitlistStatus.EXPIRED);
            waitlistRepository.save(waitlist);

            AuditLog log = new AuditLog();
            log.setOperator("SYSTEM");
            log.setAction("EXPIRE_WAITLIST");
            log.setTargetType("Waitlist");
            log.setTargetId(waitlist.getId());
            log.setDetail("候補保留時間逾期，自動作廢並通知下一位候補");
            auditLogRepository.save(log);

            reservationService.promoteNextWaitlist(waitlist.getSession().getId(), "SYSTEM");
        }
    }
}
