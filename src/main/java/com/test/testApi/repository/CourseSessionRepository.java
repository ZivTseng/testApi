package com.test.testApi.repository;

import com.test.testApi.entity.CourseSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CourseSessionRepository extends JpaRepository<CourseSession, Long> {
    List<CourseSession> findBySessionDateBetween(LocalDate start, LocalDate end);
}
