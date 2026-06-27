package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditAdjustReq {

    // 正數為補回、負數為扣除
    @NotNull(message = "調整堂數不能為空")
    private Integer delta;

    @NotBlank(message = "請填寫調整原因")
    private String reason;
}
