package com.test.testApi.dto.req;

import com.test.testApi.entity.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentReq {

    @NotNull(message = "學員 ID 不能為空")
    private Long studentId;

    @NotNull(message = "方案 ID 不能為空")
    private Long planId;

    @NotNull(message = "金額不能為空")
    private BigDecimal amount;

    @NotNull(message = "付款方式不能為空")
    private PaymentMethod method;

    private String invoiceNo;

    @NotNull(message = "付款日期不能為空")
    private LocalDate paymentDate;

    private String note;

    // 轉帳匯款末五碼，現金付款可不填
    private String transferLast5;
}
