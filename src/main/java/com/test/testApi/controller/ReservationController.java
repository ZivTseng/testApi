package com.test.testApi.controller;

import com.test.testApi.dto.req.AttendanceReq;
import com.test.testApi.dto.req.ReservationCreateReq;
import com.test.testApi.dto.res.BookingResultRes;
import com.test.testApi.dto.res.ReservationRes;
import com.test.testApi.entity.enums.ReservationStatus;
import com.test.testApi.repository.ReservationRepository;
import com.test.testApi.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    @PostMapping
    public BookingResultRes book(@Valid @RequestBody ReservationCreateReq req, Authentication authentication) {
        return reservationService.book(req.getStudentId(), req.getSessionId(), req.isTrial(), operatorOf(authentication));
    }

    @PostMapping("/{id}/cancel")
    public ReservationRes cancel(@PathVariable Long id, Authentication authentication) {
        return reservationService.cancel(id, operatorOf(authentication));
    }

    @GetMapping("/student/{studentId}")
    public List<ReservationRes> byStudent(@PathVariable Long studentId) {
        return reservationRepository.findByStudent_Id(studentId).stream().map(ReservationRes::from).toList();
    }

    @GetMapping("/session/{sessionId}")
    public List<ReservationRes> bySession(@PathVariable Long sessionId) {
        return reservationRepository.findBySession_IdAndStatus(sessionId, ReservationStatus.CONFIRMED)
                .stream().map(ReservationRes::from).toList();
    }

    // 點名名冊：包含已確認、已出席、已缺席的預約（不含已取消），供點名介面使用
    @GetMapping("/session/{sessionId}/roster")
    public List<ReservationRes> roster(@PathVariable Long sessionId) {
        return reservationRepository.findBySession_IdAndStatusIn(sessionId,
                        List.of(ReservationStatus.CONFIRMED, ReservationStatus.ATTENDED, ReservationStatus.ABSENT))
                .stream().map(ReservationRes::from).toList();
    }

    @PostMapping("/{id}/attendance")
    public ReservationRes markAttendance(@PathVariable Long id, @Valid @RequestBody AttendanceReq req, Authentication authentication) {
        return reservationService.markAttendance(id, req.getStatus(), operatorOf(authentication));
    }

    private String operatorOf(Authentication authentication) {
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
