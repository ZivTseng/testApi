package com.test.testApi.dto.res;

import com.test.testApi.entity.CourseSession;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class CourseSessionRes {
    private Long id;
    private Long courseId;
    private String courseName;
    private LocalDate sessionDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer capacity;
    private long reservedCount;
    private boolean full;

    public static CourseSessionRes from(CourseSession session, long reservedCount) {
        return new CourseSessionRes(
                session.getId(),
                session.getCourse().getId(),
                session.getCourse().getName(),
                session.getSessionDate(),
                session.getStartTime(),
                session.getEndTime(),
                session.getCapacity(),
                reservedCount,
                reservedCount >= session.getCapacity()
        );
    }
}
