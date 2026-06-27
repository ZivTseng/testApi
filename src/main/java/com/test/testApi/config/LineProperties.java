package com.test.testApi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "line")
@Data
public class LineProperties {
    private String channelSecret;
    private String channelAccessToken;
    // Messaging API channel 的 Channel ID，驗證 LIFF ID Token 時要用來確認 token 的 audience 是發給這個 channel
    private String channelId;
}
