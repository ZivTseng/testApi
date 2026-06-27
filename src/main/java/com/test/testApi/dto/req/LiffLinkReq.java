package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LiffLinkReq {
    @NotBlank
    private String idToken;
    @NotBlank
    private String phone;
}
