package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentPlanCreateReq {

    @NotNull(message = "學員 ID 不能為空")
    private Long studentId;

    @NotNull(message = "方案 ID 不能為空")
    private Long planId;

    // 未填則預設為今天
    private LocalDate purchaseDate;
}
