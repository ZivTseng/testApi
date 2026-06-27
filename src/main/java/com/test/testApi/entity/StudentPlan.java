package com.test.testApi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

// 學員實際購買的堂數帳戶，記錄剩餘堂數與到期日
@Entity
@Table(name = "student_plans")
@Data
public class StudentPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(name = "remaining_sessions", nullable = false)
    private Integer remainingSessions;

    @Column(name = "purchase_date", nullable = false)
    private LocalDate purchaseDate;

    @Column(name = "expire_date", nullable = false)
    private LocalDate expireDate;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;
}
