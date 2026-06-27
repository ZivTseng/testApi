package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LiffBookReq {
    @NotNull
    private Long studentId;
    @NotNull
    private Long sessionId;
}
