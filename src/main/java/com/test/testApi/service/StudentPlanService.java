package com.test.testApi.service;

import com.test.testApi.dto.res.StudentPlanRes;
import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.CreditLog;
import com.test.testApi.entity.Plan;
import com.test.testApi.entity.Student;
import com.test.testApi.entity.StudentPlan;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.CreditLogRepository;
import com.test.testApi.repository.PlanRepository;
import com.test.testApi.repository.StudentPlanRepository;
import com.test.testApi.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StudentPlanService {

    private final StudentPlanRepository studentPlanRepository;
    private final StudentRepository studentRepository;
    private final PlanRepository planRepository;
    private final CreditLogRepository creditLogRepository;
    private final AuditLogRepository auditLogRepository;

    /** 幫學員開通方案：依方案模板的堂數與效期建立儲值帳户 */
    @Transactional
    public StudentPlanRes open(Long studentId, Long planId, LocalDate purchaseDate, String operator) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("找不到學員，ID: " + studentId));
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("找不到方案，ID: " + planId));

        LocalDate purchase = purchaseDate != null ? purchaseDate : LocalDate.now();

        StudentPlan studentPlan = new StudentPlan();
        studentPlan.setStudent(student);
        studentPlan.setPlan(plan);
        studentPlan.setRemainingSessions(plan.getTotalSessions());
        studentPlan.setPurchaseDate(purchase);
        studentPlan.setExpireDate(purchase.plusDays(plan.getValidityDays()));
        studentPlanRepository.save(studentPlan);

        saveCreditLog(studentPlan, plan.getTotalSessions(), "開通方案", operator);
        saveAuditLog(operator, "OPEN_PLAN", "StudentPlan", studentPlan.getId(),
                "學員 " + student.getName() + " 開通方案「" + plan.getName() + "」，共 " + plan.getTotalSessions() + " 堂");

        return StudentPlanRes.from(studentPlan);
    }

    /** 手動調整剩餘堂數，例如補課、行政補回，必須留下原因與操作人 */
    @Transactional
    public StudentPlanRes adjustCredit(Long studentPlanId, int delta, String reason, String operator) {
        StudentPlan studentPlan = studentPlanRepository.findById(studentPlanId)
                .orElseThrow(() -> new RuntimeException("找不到儲值帳户，ID: " + studentPlanId));

        int newRemaining = studentPlan.getRemainingSessions() + delta;
        if (newRemaining < 0) {
            throw new RuntimeException("調整後堂數不能小於 0，目前剩餘 " + studentPlan.getRemainingSessions());
        }
        studentPlan.setRemainingSessions(newRemaining);
        studentPlanRepository.save(studentPlan);

        saveCreditLog(studentPlan, delta, reason, operator);
        saveAuditLog(operator, "ADJUST_CREDIT", "StudentPlan", studentPlan.getId(),
                "手動調整堂數 " + (delta >= 0 ? "+" : "") + delta + "，原因：" + reason);

        return StudentPlanRes.from(studentPlan);
    }

    private void saveCreditLog(StudentPlan plan, int delta, String reason, String operator) {
        CreditLog log = new CreditLog();
        log.setStudentPlan(plan);
        log.setDelta(delta);
        log.setReason(reason);
        log.setOperator(operator);
        creditLogRepository.save(log);
    }

    private void saveAuditLog(String operator, String action, String targetType, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setOperator(operator);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }
}
