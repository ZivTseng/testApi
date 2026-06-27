package com.test.testApi.controller;

import com.test.testApi.dto.req.StudentReq;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.dto.res.StudentRes;
import com.test.testApi.entity.Parent;
import com.test.testApi.entity.Student;
import com.test.testApi.repository.ParentRepository;
import com.test.testApi.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;

    @GetMapping
    public List<StudentRes> list() {
        return studentRepository.findAll().stream().map(StudentRes::from).toList();
    }

    @GetMapping("/{id}")
    public StudentRes get(@PathVariable Long id) {
        return StudentRes.from(findOrThrow(id));
    }

    @PostMapping
    public StudentRes create(@Valid @RequestBody StudentReq req) {
        Student student = new Student();
        applyReq(student, req);
        return StudentRes.from(studentRepository.save(student));
    }

    @PutMapping("/{id}")
    public StudentRes update(@PathVariable Long id, @Valid @RequestBody StudentReq req) {
        Student student = findOrThrow(id);
        applyReq(student, req);
        return StudentRes.from(studentRepository.save(student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageRes> delete(@PathVariable Long id) {
        studentRepository.delete(findOrThrow(id));
        return ResponseEntity.ok(new MessageRes("學員刪除成功"));
    }

    private void applyReq(Student student, StudentReq req) {
        student.setStudentNo(req.getStudentNo());
        student.setName(req.getName());
        student.setGender(req.getGender());
        student.setBirthday(req.getBirthday());
        student.setNote(req.getNote());

        if (req.getParentIds() != null) {
            Set<Parent> parents = new HashSet<>(parentRepository.findAllById(req.getParentIds()));
            student.setParents(parents);
        }
    }

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到學員，ID: " + id));
    }
}
