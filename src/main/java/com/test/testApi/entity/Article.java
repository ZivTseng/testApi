package com.test.testApi.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*; // 如果報錯，請改成 javax.persistence.*
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "articles")
@Data
@EntityListeners(AuditingEntityListener.class) // 啟用自動填寫時間戳記
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // 文章內容通常很長，所以指定資料庫型別為 TEXT
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 發佈狀態：'draft' (草稿) 或 'published' (已發佈)
    @Column(nullable = false)
    private String status;

    // 實作「標籤(複選)」：JPA 會自動幫我們建一張 article_tags 的中介表來存標籤
    @ElementCollection
    @CollectionTable(name = "article_tags", joinColumns = @JoinColumn(name = "article_id"))
    @Column(name = "tag")
    private Set<String> tags;

    // 多對一關聯：多篇文章對應一個作者。設定 LAZY 延遲載入以提升效能
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 建立時間，由 Spring Data JPA 自動寫入且不允許更新
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 最後更新時間，由 Spring Data JPA 自動更新
    @LastModifiedDate
    private LocalDateTime updatedAt;
}