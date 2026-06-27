package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SystemParameterReq {

    @NotBlank(message = "參數值不能為空")
    private String paramValue;
}
