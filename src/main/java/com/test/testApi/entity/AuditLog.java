package com.test.testApi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 通用稽核紀錄：堂數異動、預約異動、候補調整、收款確認、點名紀錄等重要操作皆需留存
@Entity
@Table(name = "audit_logs")
@Data
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String operator;

    @Column(nullable = false)
    private String action;

    // 被異動的對象類型，例如 Reservation、StudentPlan、Payment
    @Column(name = "target_type", nullable = false)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
