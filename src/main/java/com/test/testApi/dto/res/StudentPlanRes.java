package com.test.testApi.dto.res;

import com.test.testApi.entity.StudentPlan;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class StudentPlanRes {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long planId;
    private String planName;
    private Integer remainingSessions;
    private LocalDate purchaseDate;
    private LocalDate expireDate;

    public static StudentPlanRes from(StudentPlan sp) {
        return new StudentPlanRes(
                sp.getId(),
                sp.getStudent().getId(),
                sp.getStudent().getName(),
                sp.getPlan().getId(),
                sp.getPlan().getName(),
                sp.getRemainingSessions(),
                sp.getPurchaseDate(),
                sp.getExpireDate()
        );
    }
}
