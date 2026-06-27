package com.test.testApi.repository;

import com.test.testApi.entity.Payment;
import com.test.testApi.entity.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByStudent_Id(Long studentId);
    List<Payment> findByPaymentDateBetweenAndStatus(LocalDate from, LocalDate to, PaymentStatus status);
}
