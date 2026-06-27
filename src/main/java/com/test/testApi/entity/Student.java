package com.test.testApi.entity;

import com.test.testApi.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

// 注意：不可用 @Data，因為雙向 @ManyToMany 的 collection 若被含入 equals/hashCode/toString，
// Hibernate 組裝 Set 時會互相觸發 lazy load 造成無窮遞迴查詢
@Entity
@Table(name = "students")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@ToString(exclude = "parents")
@EntityListeners(AuditingEntityListener.class)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_no", nullable = false, unique = true)
    private String studentNo;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate birthday;

    @Column(columnDefinition = "TEXT")
    private String note;

    // 預留多館區欄位，本階段固定為 1
    @Column(name = "branch_id", nullable = false, columnDefinition = "BIGINT DEFAULT 1")
    private Long branchId = 1L;

    // 支援一位學員多位家長、一位家長多位學員
    @ManyToMany
    @JoinTable(
            name = "parent_student",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    private Set<Parent> parents = new HashSet<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
