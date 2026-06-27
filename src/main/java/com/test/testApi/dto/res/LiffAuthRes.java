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
    // true 代表這筆資料還在等館方電話聯絡/收款確認，前端應該擋住預約功能、只顯示等待畫面
    private boolean pendingReview;

    public static LiffAuthRes notLinked() {
        return new LiffAuthRes(false, null, null, false);
    }

    public static LiffAuthRes linked(String token, String parentName, boolean pendingReview) {
        return new LiffAuthRes(true, token, parentName, pendingReview);
    }
}
