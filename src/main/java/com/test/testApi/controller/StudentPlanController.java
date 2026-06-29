package com.test.testApi.controller;

import com.test.testApi.dto.req.CreditAdjustReq;
import com.test.testApi.dto.req.StudentPlanCreateReq;
import com.test.testApi.dto.res.CreditLogRes;
import com.test.testApi.dto.res.StudentPlanRes;
import com.test.testApi.repository.CreditLogRepository;
import com.test.testApi.repository.StudentPlanRepository;
import com.test.testApi.service.StudentPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/student-plans")
@RequiredArgsConstructor
public class StudentPlanController {

    private final StudentPlanService studentPlanService;
    private final StudentPlanRepository studentPlanRepository;
    private final CreditLogRepository creditLogRepository;

    @PostMapping
    public StudentPlanRes open(@Valid @RequestBody StudentPlanCreateReq req, Authentication authentication) {
        return studentPlanService.open(req.getStudentId(), req.getPlanId(), req.getPurchaseDate(), operatorOf(authentication));
    }

    // 供學員列表頁一次帶出所有人的儲值狀態，避免逐筆呼叫 /student/{id}
    @GetMapping
    public List<StudentPlanRes> list() {
        return studentPlanRepository.findAll().stream().map(StudentPlanRes::from).toList();
    }

    @GetMapping("/student/{studentId}")
    public List<StudentPlanRes> byStudent(@PathVariable Long studentId) {
        return studentPlanRepository.findByStudent_Id(studentId).stream().map(StudentPlanRes::from).toList();
    }

    @PostMapping("/{id}/adjust")
    public StudentPlanRes adjust(@PathVariable Long id, @Valid @RequestBody CreditAdjustReq req, Authentication authentication) {
        return studentPlanService.adjustCredit(id, req.getDelta(), req.getReason(), operatorOf(authentication));
    }

    @GetMapping("/{id}/credit-logs")
    public List<CreditLogRes> creditLogs(@PathVariable Long id) {
        return creditLogRepository.findByStudentPlan_Id(id).stream().map(CreditLogRes::from).toList();
    }

    // 查詢堂數即將用完（但尚未過期）的儲值帳户，供營運總覽提醒續約
    @GetMapping("/low-balance")
    public List<StudentPlanRes> lowBalance(@RequestParam(defaultValue = "2") Integer threshold) {
        return studentPlanRepository
                .findByRemainingSessionsLessThanEqualAndExpireDateGreaterThanEqual(threshold, LocalDate.now())
                .stream().map(StudentPlanRes::from).toList();
    }

    private String operatorOf(Authentication authentication) {
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
