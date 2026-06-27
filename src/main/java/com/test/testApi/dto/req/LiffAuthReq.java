package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LiffAuthReq {
    @NotBlank
    private String idToken;
}
