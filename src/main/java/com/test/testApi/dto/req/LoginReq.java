package com.test.testApi.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email; // 如果你是 Spring Boot 2.x，請改成 javax.validation.*
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReq {

    @Schema(description = "使用者信箱", example = "admin@test.com")
    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @Schema(description = "使用者密碼", example = "123456")
    @NotBlank(message = "密碼不能為空")
    private String password;
}