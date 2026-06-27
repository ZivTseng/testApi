package com.test.testApi.dto.res;

import com.test.testApi.entity.Payment;
import com.test.testApi.entity.enums.PaymentMethod;
import com.test.testApi.entity.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PaymentRes {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long planId;
    private String planName;
    private BigDecimal amount;
    private PaymentMethod method;
    private String invoiceNo;
    private String operator;
    private LocalDate paymentDate;
    private String note;
    private String transferLast5;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public static PaymentRes from(Payment p) {
        return new PaymentRes(
                p.getId(),
                p.getStudent().getId(),
                p.getStudent().getName(),
                p.getPlan().getId(),
                p.getPlan().getName(),
                p.getAmount(),
                p.getMethod(),
                p.getInvoiceNo(),
                p.getOperator(),
                p.getPaymentDate(),
                p.getNote(),
                p.getTransferLast5(),
                p.getStatus(),
                p.getCreatedAt()
        );
    }
}
