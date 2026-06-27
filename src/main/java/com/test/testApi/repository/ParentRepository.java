package com.test.testApi.repository;

import com.test.testApi.entity.Parent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByLineUserId(String lineUserId);
    Optional<Parent> findByBindingCodeAndBindingCodeExpireAtAfter(String bindingCode, LocalDateTime now);
    Optional<Parent> findFirstByPhoneAndLineUserIdIsNull(String phone);
}
