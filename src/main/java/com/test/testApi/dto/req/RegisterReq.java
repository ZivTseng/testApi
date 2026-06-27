package com.test.testApi.dto.req;

import com.test.testApi.entity.enums.AdminRole;
import lombok.Data;

@Data
public class RegisterReq {
    private String username;
    private String password;
    private String name;
    private AdminRole role;
}
