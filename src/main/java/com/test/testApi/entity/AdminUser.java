package com.test.testApi.entity;

import com.test.testApi.entity.enums.AdminRole;
import jakarta.persistence.*; // 如果報錯，請改成 javax.persistence.*
import lombok.Data;

@Entity
@Table(name = "admin_users")
@Data
public class AdminUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 後台登入帳號
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRole role;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;
}
