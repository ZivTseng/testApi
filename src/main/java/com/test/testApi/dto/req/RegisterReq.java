package com.test.testApi.dto.req; // 請替換成你的 package

import lombok.Data;

@Data
public class RegisterReq {
    private String username;
    private String email;
    private String password;
}