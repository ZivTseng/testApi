package com.test.testApi.dto.res;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginRes {
    private String token;
    private String username;
}
