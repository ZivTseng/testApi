package com.test.testApi.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*; // 如果報錯，請改成 javax.persistence.*

@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 帳號 (Email)，設定為不可為空且必須唯一
    @Column(nullable = false, unique = true)
    private String email;

    // 密碼
    @Column(nullable = false)
    private String password;

    // 姓名
    @Column(nullable = false)
    private String userName;
}