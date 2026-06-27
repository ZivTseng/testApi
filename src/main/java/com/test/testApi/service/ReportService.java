package com.test.testApi.service;

import com.test.testApi.dto.res.DailyTrendRes;
import com.test.testApi.dto.res.ReportSummaryRes;
import com.test.testApi.entity.Payment;
import com.test.testApi.entity.Reservation;
import com.test.testApi.entity.enums.PaymentStatus;
import com.test.testApi.entity.enums.ReservationStatus;
import com.test.testApi.repository.PaymentRepository;
import com.test.testApi.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    /** 依場次日期區間統計出席率、堂數消耗與收款金額，供營運報表使用 */
    public ReportSummaryRes summary(LocalDate from, LocalDate to) {
        List<Reservation> reservations = reservationRepository.findBySession_SessionDateBetween(from, to);

        long attended = countByStatus(reservations, ReservationStatus.ATTENDED);
        long absent = countByStatus(reservations, ReservationStatus.ABSENT);
        long cancelled = countByStatus(reservations, ReservationStatus.CANCELLED);
        long confirmed = countByStatus(reservations, ReservationStatus.CONFIRMED);
        long trial = reservations.stream().filter(Reservation::isTrial).count();

        long creditsConsumed = reservations.stream()
                .filter(r -> !r.isTrial())
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED
                        || r.getStatus() == ReservationStatus.ATTENDED
                        || r.getStatus() == ReservationStatus.ABSENT)
                .count();

        double attendanceRate = (attended + absent) > 0 ? attended * 100.0 / (attended + absent) : 0;

        BigDecimal revenue = paymentRepository.findByPaymentDateBetweenAndStatus(from, to, PaymentStatus.CONFIRMED)
                .stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ReportSummaryRes(from, to, reservations.size(), attended, absent, cancelled, confirmed,
                trial, attendanceRate, creditsConsumed, revenue);
    }

    /** 依日期逐天統計預約量與出席量，供營運總覽的趨勢圖使用，區間內沒有資料的日期會補 0 */
    public List<DailyTrendRes> dailyTrend(LocalDate from, LocalDate to) {
        List<Reservation> reservations = reservationRepository.findBySession_SessionDateBetween(from, to).stream()
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .toList();

        Map<LocalDate, List<Reservation>> byDate = reservations.stream()
                .collect(Collectors.groupingBy(r -> r.getSession().getSessionDate()));

        List<DailyTrendRes> trend = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            List<Reservation> dayReservations = byDate.getOrDefault(date, List.of());
            long attended = countByStatus(dayReservations, ReservationStatus.ATTENDED);
            trend.add(new DailyTrendRes(date, dayReservations.size(), attended));
        }
        return trend;
    }

    private long countByStatus(List<Reservation> reservations, ReservationStatus status) {
        return reservations.stream().filter(r -> r.getStatus() == status).count();
    }
}
