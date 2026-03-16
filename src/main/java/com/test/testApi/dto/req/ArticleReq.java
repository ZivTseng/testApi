package com.test.testApi.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
public class ArticleReq {

    @Schema(description = "文章標題", example = "Spring Boot 學習心得")
    @NotBlank(message = "標題不能為空")
    private String title;

    @Schema(description = "文章內容", example = "這是一篇關於如何快速上手 Spring Boot 的文章內容...")
    @NotBlank(message = "內容不能為空")
    private String content;

    @Schema(description = "文章狀態", example = "published")
    @NotBlank(message = "狀態不能為空")
    private String status;

    @Schema(description = "文章標籤", example = "[\"Java\", \"Spring Boot\", \"Backend\"]")
    private Set<String> tags;
}