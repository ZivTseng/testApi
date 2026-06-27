package com.test.testApi.repository;

import com.test.testApi.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentNo(String studentNo);
    List<Student> findByParents_Id(Long parentId);
}
