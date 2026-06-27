package com.test.testApi.controller;

import com.test.testApi.dto.req.PaymentReq;
import com.test.testApi.dto.res.PaymentRes;
import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.Payment;
import com.test.testApi.entity.Plan;
import com.test.testApi.entity.Student;
import com.test.testApi.entity.enums.PaymentStatus;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.PaymentRepository;
import com.test.testApi.repository.PlanRepository;
import com.test.testApi.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final PlanRepository planRepository;
    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public List<PaymentRes> list() {
        return paymentRepository.findAll().stream().map(PaymentRes::from).toList();
    }

    @GetMapping("/student/{studentId}")
    public List<PaymentRes> byStudent(@PathVariable Long studentId) {
        return paymentRepository.findByStudent_Id(studentId).stream().map(PaymentRes::from).toList();
    }

    @PostMapping
    public PaymentRes create(@Valid @RequestBody PaymentReq req, Authentication authentication) {
        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new RuntimeException("找不到學員，ID: " + req.getStudentId()));
        Plan plan = planRepository.findById(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("找不到方案，ID: " + req.getPlanId()));

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setPlan(plan);
        payment.setAmount(req.getAmount());
        payment.setMethod(req.getMethod());
        payment.setInvoiceNo(req.getInvoiceNo());
        payment.setOperator(operatorOf(authentication));
        payment.setPaymentDate(req.getPaymentDate());
        payment.setNote(req.getNote());
        payment.setTransferLast5(req.getTransferLast5());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        saveAuditLog(operatorOf(authentication), "CREATE_PAYMENT", payment.getId(),
                "建立繳費紀錄，學員 " + student.getName() + "，金額 " + req.getAmount());

        return PaymentRes.from(payment);
    }

    @PutMapping("/{id}")
    public PaymentRes update(@PathVariable Long id, @Valid @RequestBody PaymentReq req, Authentication authentication) {
        Payment payment = findOrThrow(id);
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new RuntimeException("此繳費紀錄已確認收款，無法再修改，請先確認金額/方式是否正確");
        }

        Student student = studentRepository.findById(req.getStudentId())
                .orElseThrow(() -> new RuntimeException("找不到學員，ID: " + req.getStudentId()));
        Plan plan = planRepository.findById(req.getPlanId())
                .orElseThrow(() -> new RuntimeException("找不到方案，ID: " + req.getPlanId()));

        payment.setStudent(student);
        payment.setPlan(plan);
        payment.setAmount(req.getAmount());
        payment.setMethod(req.getMethod());
        payment.setInvoiceNo(req.getInvoiceNo());
        payment.setPaymentDate(req.getPaymentDate());
        payment.setNote(req.getNote());
        payment.setTransferLast5(req.getTransferLast5());
        paymentRepository.save(payment);

        saveAuditLog(operatorOf(authentication), "UPDATE_PAYMENT", payment.getId(),
                "修改繳費紀錄，學員 " + student.getName() + "，金額 " + req.getAmount() + "，方式 " + req.getMethod());

        return PaymentRes.from(payment);
    }

    @PostMapping("/{id}/confirm")
    public PaymentRes confirm(@PathVariable Long id, Authentication authentication) {
        Payment payment = findOrThrow(id);
        if (payment.getStatus() == PaymentStatus.CONFIRMED) {
            throw new RuntimeException("此繳費紀錄已確認，無需重複確認");
        }
        payment.setStatus(PaymentStatus.CONFIRMED);
        paymentRepository.save(payment);

        saveAuditLog(operatorOf(authentication), "CONFIRM_PAYMENT", payment.getId(),
                "確認收款，學員 " + payment.getStudent().getName() + "，金額 " + payment.getAmount());

        return PaymentRes.from(payment);
    }

    private Payment findOrThrow(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到繳費紀錄，ID: " + id));
    }

    private void saveAuditLog(String operator, String action, Long targetId, String detail) {
        AuditLog log = new AuditLog();
        log.setOperator(operator);
        log.setAction(action);
        log.setTargetType("Payment");
        log.setTargetId(targetId);
        log.setDetail(detail);
        auditLogRepository.save(log);
    }

    private String operatorOf(Authentication authentication) {
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
