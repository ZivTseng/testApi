package com.test.testApi.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ParentReq {

    @NotBlank(message = "家長姓名不能為空")
    private String name;

    private String phone;

    private String lineUserId;

    // 綁定的孩子 ID 清單，支援一位家長多個孩子
    private List<Long> studentIds;
}
