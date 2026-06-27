package com.test.testApi.entity;

import com.test.testApi.entity.enums.PaymentMethod;
import com.test.testApi.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(nullable = false)
    private String operator;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    // 轉帳匯款末五碼，現金付款時為 null
    @Column(name = "transfer_last5")
    private String transferLast5;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
