package com.test.testApi.controller;

import com.test.testApi.dto.req.LiffBookReq;
import com.test.testApi.dto.res.BookingResultRes;
import com.test.testApi.dto.res.CourseSessionRes;
import com.test.testApi.dto.res.LiffChildRes;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.dto.res.ReservationRes;
import com.test.testApi.entity.CourseSession;
import com.test.testApi.entity.Parent;
import com.test.testApi.entity.Reservation;
import com.test.testApi.entity.Student;
import com.test.testApi.entity.enums.ReservationStatus;
import com.test.testApi.repository.CourseSessionRepository;
import com.test.testApi.repository.ParentRepository;
import com.test.testApi.repository.ReservationRepository;
import com.test.testApi.repository.StudentRepository;
import com.test.testApi.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** 家長端 (LIFF) 自助服務：瀏覽場次、預約、查看/取消預約。Authentication.getName() 是 LiffAuthTokenFilter 塞進去的家長 ID 字串。 */
@RestController
@RequestMapping("/api/liff")
@RequiredArgsConstructor
public class LiffController {

    private static final List<ReservationStatus> OCCUPYING_STATUSES =
            List.of(ReservationStatus.CONFIRMED, ReservationStatus.ATTENDED, ReservationStatus.ABSENT);

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @GetMapping("/me")
    public List<LiffChildRes> myChildren(Authentication authentication) {
        Parent parent = currentParent(authentication);
        return studentRepository.findByParents_Id(parent.getId()).stream()
                .map(s -> new LiffChildRes(s.getId(), s.getName()))
                .toList();
    }

    // 列出未來 14 天內的課程場次，給家長選擇預約
    @GetMapping("/sessions")
    public List<CourseSessionRes> sessions() {
        LocalDate today = LocalDate.now();
        return courseSessionRepository.findBySessionDateBetween(today, today.plusDays(14)).stream()
                .map(this::toSessionRes)
                .toList();
    }

    @GetMapping("/reservations")
    public List<ReservationRes> myReservations(Authentication authentication) {
        Parent parent = currentParent(authentication);
        List<Long> childIds = studentRepository.findByParents_Id(parent.getId()).stream().map(Student::getId).toList();
        return childIds.stream()
                .flatMap(id -> reservationRepository.findByStudent_Id(id).stream())
                .map(ReservationRes::from)
                .toList();
    }

    @PostMapping("/reservations")
    public BookingResultRes book(@Valid @RequestBody LiffBookReq req, Authentication authentication) {
        Parent parent = currentParent(authentication);
        if (parent.isPendingReview()) {
            throw new RuntimeException("您的報名資料還在等館方確認，請稍候再試或聯絡館方");
        }
        assertOwnChild(parent, req.getStudentId());
        return reservationService.book(req.getStudentId(), req.getSessionId(), false, "PARENT:" + parent.getId());
    }

    @PostMapping("/reservations/{id}/cancel")
    public ResponseEntity<MessageRes> cancel(@PathVariable Long id, Authentication authentication) {
        Parent parent = currentParent(authentication);
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到預約，ID: " + id));
        assertOwnChild(parent, reservation.getStudent().getId());

        reservationService.cancel(id, "PARENT:" + parent.getId());
        return ResponseEntity.ok(new MessageRes("已取消預約"));
    }

    private void assertOwnChild(Parent parent, Long studentId) {
        boolean isOwnChild = studentRepository.findByParents_Id(parent.getId()).stream()
                .anyMatch(s -> s.getId().equals(studentId));
        if (!isOwnChild) {
            throw new RuntimeException("此學員不屬於您的帳號，無法操作");
        }
    }

    private Parent currentParent(Authentication authentication) {
        Long parentId = Long.valueOf(authentication.getName());
        return parentRepository.findById(parentId)
                .orElseThrow(() -> new RuntimeException("找不到家長資料"));
    }

    private CourseSessionRes toSessionRes(CourseSession session) {
        long reservedCount = reservationRepository.countBySession_IdAndStatusIn(session.getId(), OCCUPYING_STATUSES);
        return CourseSessionRes.from(session, reservedCount);
    }
}
