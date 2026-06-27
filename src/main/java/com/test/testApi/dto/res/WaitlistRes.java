package com.test.testApi.dto.res;

import com.test.testApi.entity.Waitlist;
import com.test.testApi.entity.enums.WaitlistStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class WaitlistRes {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long sessionId;
    private String courseName;
    private LocalDate sessionDate;
    private Integer queueNo;
    private WaitlistStatus status;
    private LocalDateTime notifiedAt;
    private LocalDateTime expireAt;

    public static WaitlistRes from(Waitlist w) {
        return new WaitlistRes(
                w.getId(),
                w.getStudent().getId(),
                w.getStudent().getName(),
                w.getSession().getId(),
                w.getSession().getCourse().getName(),
                w.getSession().getSessionDate(),
                w.getQueueNo(),
                w.getStatus(),
                w.getNotifiedAt(),
                w.getExpireAt()
        );
    }
}
