package com.test.testApi.dto.res;

import com.test.testApi.entity.SystemParameter;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemParameterRes {
    private Long id;
    private String paramKey;
    private String paramValue;
    private String description;

    public static SystemParameterRes from(SystemParameter p) {
        return new SystemParameterRes(p.getId(), p.getParamKey(), p.getParamValue(), p.getDescription());
    }
}
