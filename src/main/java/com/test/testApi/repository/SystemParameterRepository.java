package com.test.testApi.repository;

import com.test.testApi.entity.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, Long> {
    Optional<SystemParameter> findByParamKey(String paramKey);
}
