package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseReq {

    @NotBlank(message = "課程名稱不能為空")
    private String name;

    private String description;
}
