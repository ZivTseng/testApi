package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlanReq {

    @NotBlank(message = "方案名稱不能為空")
    private String name;

    @NotNull(message = "總堂數不能為空")
    private Integer totalSessions;

    @NotNull(message = "效期天數不能為空")
    private Integer validityDays;

    @NotNull(message = "價格不能為空")
    private BigDecimal price;
}
