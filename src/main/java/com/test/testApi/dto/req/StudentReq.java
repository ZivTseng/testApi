package com.test.testApi.dto.req;

import com.test.testApi.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class StudentReq {

    @NotBlank(message = "學號不能為空")
    private String studentNo;

    @NotBlank(message = "學員姓名不能為空")
    private String name;

    private Gender gender;

    private LocalDate birthday;

    private String note;

    // 綁定的家長 ID 清單，支援一位學員多位家長
    private List<Long> parentIds;
}
