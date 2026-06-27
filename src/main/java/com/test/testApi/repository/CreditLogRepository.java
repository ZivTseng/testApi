package com.test.testApi.repository;

import com.test.testApi.entity.CreditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditLogRepository extends JpaRepository<CreditLog, Long> {
    List<CreditLog> findByStudentPlan_Id(Long studentPlanId);
}
