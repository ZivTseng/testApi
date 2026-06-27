package com.test.testApi.entity;

import com.test.testApi.entity.enums.WaitlistStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "waitlists")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private CourseSession session;

    // 候補順位，數字越小越優先
    @Column(name = "queue_no", nullable = false)
    private Integer queueNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WaitlistStatus status;

    // 通知家長轉正的時間
    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    // 名額保留期限，逾時未確認則通知下一位候補（保留時間由系統參數設定）
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
