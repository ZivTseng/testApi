package com.test.testApi.entity;

import jakarta.persistence.*;
import lombok.Data;

// 可由後台調整的營運規則，例如取消期限、候補保留時間，不寫死於程式
@Entity
@Table(name = "system_parameters")
@Data
public class SystemParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_key", nullable = false, unique = true)
    private String paramKey;

    @Column(name = "param_value", nullable = false)
    private String paramValue;

    private String description;
}
