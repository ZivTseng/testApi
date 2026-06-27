package com.test.testApi.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ReportSummaryRes {
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalReservations;
    private long attendedCount;
    private long absentCount;
    private long cancelledCount;
    private long confirmedCount;
    private long trialCount;
    private double attendanceRate;
    private long creditsConsumed;
    private BigDecimal confirmedRevenue;
}
