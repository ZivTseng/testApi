package com.test.testApi.dto.res;

import com.test.testApi.entity.Student;
import com.test.testApi.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class StudentRes {
    private Long id;
    private String studentNo;
    private String name;
    private Gender gender;
    private LocalDate birthday;
    private String note;
    private List<Long> parentIds;

    public static StudentRes from(Student student) {
        return new StudentRes(
                student.getId(),
                student.getStudentNo(),
                student.getName(),
                student.getGender(),
                student.getBirthday(),
                student.getNote(),
                student.getParents().stream().map(p -> p.getId()).toList()
        );
    }
}
