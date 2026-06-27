package com.test.testApi.dto.res;

import com.test.testApi.entity.Parent;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ParentRes {
    private Long id;
    private String name;
    private String phone;
    private String lineUserId;
    private List<Long> studentIds;

    public static ParentRes from(Parent parent) {
        return new ParentRes(
                parent.getId(),
                parent.getName(),
                parent.getPhone(),
                parent.getLineUserId(),
                parent.getStudents().stream().map(s -> s.getId()).toList()
        );
    }
}
