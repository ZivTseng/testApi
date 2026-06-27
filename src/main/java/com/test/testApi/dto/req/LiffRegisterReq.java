package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LiffRegisterReq {
    @NotBlank
    private String idToken;
    @NotBlank
    private String parentName;
    @NotBlank
    private String phone;
    @NotBlank
    private String childName;
}
