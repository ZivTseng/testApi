package com.test.testApi.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class DailyTrendRes {
    private LocalDate date;
    private long reservationCount;
    private long attendedCount;
}
