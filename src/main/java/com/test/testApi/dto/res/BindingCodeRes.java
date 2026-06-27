package com.test.testApi.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BindingCodeRes {
    private String code;
    private LocalDateTime expireAt;
}
