package com.test.testApi.dto.res;

import com.test.testApi.entity.Plan;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PlanRes {
    private Long id;
    private String name;
    private Integer totalSessions;
    private Integer validityDays;
    private BigDecimal price;

    public static PlanRes from(Plan plan) {
        return new PlanRes(plan.getId(), plan.getName(), plan.getTotalSessions(), plan.getValidityDays(), plan.getPrice());
    }
}
