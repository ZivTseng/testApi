package com.test.testApi.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

// 堂數方案模板，例如「10 堂方案」「20 堂方案」
@Entity
@Table(name = "plans")
@Data
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "total_sessions", nullable = false)
    private Integer totalSessions;

    // 方案使用期限（天數），例如 10 堂 3 個月 -> 90
    @Column(name = "validity_days", nullable = false)
    private Integer validityDays;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;
}
