package com.test.testApi.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank; // 如果你是 Spring Boot 2.x，請改成 javax.validation.*
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginReq {

    @Schema(description = "後台登入帳號", example = "admin")
    @NotBlank(message = "帳號不能為空")
    private String username;

    @Schema(description = "使用者密碼", example = "123456")
    @NotBlank(message = "密碼不能為空")
    private String password;
}
