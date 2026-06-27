package com.test.testApi.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// 堂數異動紀錄：新增、扣除、補回，皆須留下原因與操作人
@Entity
@Table(name = "credit_logs")
@Data
@EntityListeners(AuditingEntityListener.class)
public class CreditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_plan_id", nullable = false)
    private StudentPlan studentPlan;

    // 正數為新增/補回，負數為扣除
    @Column(nullable = false)
    private Integer delta;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String operator;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
