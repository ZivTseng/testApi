package com.test.testApi.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// 注意：不可用 @Data，因為雙向 @ManyToMany 的 collection 若被含入 equals/hashCode/toString，
// Hibernate 組裝 Set 時會互相觸發 lazy load 造成無窮遞迴查詢
@Entity
@Table(name = "parents")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "students")
@EntityListeners(AuditingEntityListener.class)
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String phone;

    // LINE 使用者 ID，用於綁定與推播
    @Column(name = "line_user_id", unique = true)
    private String lineUserId;

    // 家長綁定 LINE 帳號用的一次性驗證碼，家長在 LINE 對話中輸入此碼即可完成綁定
    @Column(name = "binding_code")
    private String bindingCode;

    @Column(name = "binding_code_expire_at")
    private LocalDateTime bindingCodeExpireAt;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;

    // 家長透過 LIFF 自助報名建立的資料，館方還沒電話聯絡/收款確認過；館方手動建立的資料一律視為已確認
    @Column(name = "pending_review", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean pendingReview = false;

    @ManyToMany(mappedBy = "parents")
    private Set<Student> students = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
