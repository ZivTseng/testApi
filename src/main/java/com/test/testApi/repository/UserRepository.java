package com.test.testApi.repository;

import com.test.testApi.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 根據 Email 找使用者
    Optional<User> findByEmail(String email);
    // 檢查 Email 是否已存在
    boolean existsByEmail(String email);
}