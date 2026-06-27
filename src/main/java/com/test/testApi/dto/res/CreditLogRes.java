package com.test.testApi.dto.res;

import com.test.testApi.entity.CreditLog;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreditLogRes {
    private Long id;
    private Integer delta;
    private String reason;
    private String operator;
    private LocalDateTime createdAt;

    public static CreditLogRes from(CreditLog log) {
        return new CreditLogRes(log.getId(), log.getDelta(), log.getReason(), log.getOperator(), log.getCreatedAt());
    }
}
