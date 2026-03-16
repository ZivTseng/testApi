package com.test.testApi.dto.res;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
public class ArticleRes {
    private Long id;
    private String title;
    private String content;
    private String status;
    private Set<String> tags;
    private String authorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "GMT+8")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}