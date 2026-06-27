package com.test.testApi.repository;

import com.test.testApi.entity.Reservation;
import com.test.testApi.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByStudent_Id(Long studentId);
    List<Reservation> findBySession_Id(Long sessionId);
    List<Reservation> findBySession_IdAndStatus(Long sessionId, ReservationStatus status);
    List<Reservation> findBySession_IdAndStatusIn(Long sessionId, List<ReservationStatus> statuses);
    long countBySession_IdAndStatus(Long sessionId, ReservationStatus status);
    long countBySession_IdAndStatusIn(Long sessionId, List<ReservationStatus> statuses);
    List<Reservation> findBySession_SessionDateBetween(LocalDate from, LocalDate to);
}
