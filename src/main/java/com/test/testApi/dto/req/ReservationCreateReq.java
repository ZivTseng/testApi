package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationCreateReq {

    @NotNull(message = "學員 ID 不能為空")
    private Long studentId;

    @NotNull(message = "場次 ID 不能為空")
    private Long sessionId;

    // 是否為體驗課，體驗課不扣堂數
    private boolean trial = false;
}
