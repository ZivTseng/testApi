package com.test.testApi.repository;

import com.test.testApi.entity.StudentPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StudentPlanRepository extends JpaRepository<StudentPlan, Long> {
    List<StudentPlan> findByStudent_Id(Long studentId);
    List<StudentPlan> findByRemainingSessionsLessThanEqualAndExpireDateGreaterThanEqual(Integer threshold, LocalDate today);
}
