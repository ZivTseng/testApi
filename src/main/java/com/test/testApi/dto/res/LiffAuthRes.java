package com.test.testApi.dto.res;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LiffAuthRes {
    // false 代表這個 LINE 帳號還沒綁定任何家長資料，前端要請家長輸入電話完成綁定
    private boolean linked;
    private String token;
    private String parentName;

    public static LiffAuthRes notLinked() {
        return new LiffAuthRes(false, null, null);
    }

    public static LiffAuthRes linked(String token, String parentName) {
        return new LiffAuthRes(true, token, parentName);
    }
}
