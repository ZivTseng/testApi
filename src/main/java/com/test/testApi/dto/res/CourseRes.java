package com.test.testApi.dto.res;

import com.test.testApi.entity.Course;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CourseRes {
    private Long id;
    private String name;
    private String description;

    public static CourseRes from(Course course) {
        return new CourseRes(course.getId(), course.getName(), course.getDescription());
    }
}
