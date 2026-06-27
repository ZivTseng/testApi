package com.test.testApi.dto.req;

import com.test.testApi.entity.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceReq {

    // 僅接受 ATTENDED 或 ABSENT
    @NotNull(message = "點名狀態不能為空")
    private ReservationStatus status;
}
