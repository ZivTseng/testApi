package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CourseSessionReq {

    @NotNull(message = "課程 ID 不能為空")
    private Long courseId;

    @NotNull(message = "上課日期不能為空")
    private LocalDate sessionDate;

    @NotNull(message = "開始時間不能為空")
    private LocalTime startTime;

    @NotNull(message = "結束時間不能為空")
    private LocalTime endTime;

    @NotNull(message = "名額不能為空")
    private Integer capacity;
}
