package com.test.testApi.controller;

import com.test.testApi.dto.req.CourseReq;
import com.test.testApi.dto.res.CourseRes;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.entity.Course;
import com.test.testApi.repository.CourseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseRepository courseRepository;

    @GetMapping
    public List<CourseRes> list() {
        return courseRepository.findAll().stream().map(CourseRes::from).toList();
    }

    @GetMapping("/{id}")
    public CourseRes get(@PathVariable Long id) {
        return CourseRes.from(findOrThrow(id));
    }

    @PostMapping
    public CourseRes create(@Valid @RequestBody CourseReq req) {
        Course course = new Course();
        course.setName(req.getName());
        course.setDescription(req.getDescription());
        return CourseRes.from(courseRepository.save(course));
    }

    @PutMapping("/{id}")
    public CourseRes update(@PathVariable Long id, @Valid @RequestBody CourseReq req) {
        Course course = findOrThrow(id);
        course.setName(req.getName());
        course.setDescription(req.getDescription());
        return CourseRes.from(courseRepository.save(course));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageRes> delete(@PathVariable Long id) {
        courseRepository.delete(findOrThrow(id));
        return ResponseEntity.ok(new MessageRes("課程刪除成功"));
    }

    private Course findOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到課程，ID: " + id));
    }
}
