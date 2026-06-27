package com.test.testApi.dto.res;

import com.test.testApi.entity.Reservation;
import com.test.testApi.entity.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class ReservationRes {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long sessionId;
    private String courseName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private ReservationStatus status;
    private boolean trial;
    private LocalDateTime createdAt;

    public static ReservationRes from(Reservation r) {
        return new ReservationRes(
                r.getId(),
                r.getStudent().getId(),
                r.getStudent().getName(),
                r.getSession().getId(),
                r.getSession().getCourse().getName(),
                r.getSession().getSessionDate(),
                r.getSession().getStartTime(),
                r.getStatus(),
                r.isTrial(),
                r.getCreatedAt()
        );
    }
}
