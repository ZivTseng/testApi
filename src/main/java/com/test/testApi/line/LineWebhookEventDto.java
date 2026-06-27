package com.test.testApi.line;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class LineWebhookEventDto {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Root {
        private List<LineWebhookEventDto> events;
    }

    private String type; // message, follow, unfollow ...
    private String replyToken;
    private Source source;
    private Message message;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Source {
        private String userId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String type; // text, sticker ...
        private String text;
    }
}
