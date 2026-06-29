package com.test.testApi.controller;

import com.test.testApi.dto.req.CourseSessionReq;
import com.test.testApi.dto.res.CourseSessionRes;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.entity.AuditLog;
import com.test.testApi.entity.Course;
import com.test.testApi.entity.CourseSession;
import com.test.testApi.entity.Reservation;
import com.test.testApi.entity.Waitlist;
import com.test.testApi.entity.enums.ReservationStatus;
import com.test.testApi.entity.enums.WaitlistStatus;
import com.test.testApi.repository.AuditLogRepository;
import com.test.testApi.repository.CourseRepository;
import com.test.testApi.repository.CourseSessionRepository;
import com.test.testApi.repository.ReservationRepository;
import com.test.testApi.repository.WaitlistRepository;
import com.test.testApi.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/course-sessions")
@RequiredArgsConstructor
public class CourseSessionController {

    private final CourseSessionRepository courseSessionRepository;
    private final CourseRepository courseRepository;
    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final ReservationService reservationService;
    private final AuditLogRepository auditLogRepository;

    private static final List<WaitlistStatus> ACTIVE_WAITLIST_STATUSES =
            List.of(WaitlistStatus.WAITING, WaitlistStatus.NOTIFIED);

    @GetMapping
    public List<CourseSessionRes> list() {
        return courseSessionRepository.findAll(Sort.by("sessionDate").ascending().and(Sort.by("startTime").ascending()))
                .stream().map(this::toRes).toList();
    }

    @GetMapping("/{id}")
    public CourseSessionRes get(@PathVariable Long id) {
        return toRes(findOrThrow(id));
    }

    @PostMapping
    public CourseSessionRes create(@Valid @RequestBody CourseSessionReq req) {
        CourseSession session = new CourseSession();
        applyReq(session, req);
        return toRes(courseSessionRepository.save(session));
    }

    @PutMapping("/{id}")
    public CourseSessionRes update(@PathVariable Long id, @Valid @RequestBody CourseSessionReq req) {
        CourseSession session = findOrThrow(id);
        applyReq(session, req);
        return toRes(courseSessionRepository.save(session));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<MessageRes> delete(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean force,
            Authentication authentication) {
        CourseSession session = findOrThrow(id);

        // session_id 是必填外鍵，只要還有任何一筆預約/候補紀錄（即使是歷史的已出席/已取消），都會卡住刪除
        List<Reservation> allReservations = reservationRepository.findBySession_Id(id);
        List<Waitlist> allWaitlists = waitlistRepository.findBySession_IdOrderByQueueNoAsc(id);

        long activeReservationCount = allReservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();
        long activeWaitlistCount = allWaitlists.stream()
                .filter(w -> ACTIVE_WAITLIST_STATUSES.contains(w.getStatus())).count();

        if (!force && (!allReservations.isEmpty() || !allWaitlists.isEmpty())) {
            throw new RuntimeException(
                    "此場次共有 " + allReservations.size() + " 筆預約紀錄（其中 " + activeReservationCount
                            + " 筆已確認）、" + allWaitlists.size() + " 筆候補紀錄（其中 " + activeWaitlistCount
                            + " 筆尚在候補中），請改用強制刪除（已確認的預約將全額退還堂數）");
        }

        String operator = authentication != null ? authentication.getName() : "SYSTEM";

        if (force) {
            for (Reservation reservation : allReservations) {
                reservationService.forceCancelForSessionDeletion(reservation, operator);
            }
            for (Waitlist waitlist : allWaitlists) {
                waitlistRepository.delete(waitlist);
            }
            if (!allReservations.isEmpty() || !allWaitlists.isEmpty()) {
                AuditLog log = new AuditLog();
                log.setOperator(operator);
                log.setAction("FORCE_DELETE_SESSION");
                log.setTargetType("CourseSession");
                log.setTargetId(id);
                log.setDetail("強制刪除場次，連同移除 " + allReservations.size() + " 筆預約紀錄、"
                        + allWaitlists.size() + " 筆候補紀錄（其中 " + activeReservationCount + " 筆已確認預約已全額退還堂數）");
                auditLogRepository.save(log);
            }
        }

        courseSessionRepository.delete(session);
        return ResponseEntity.ok(new MessageRes("課程場次刪除成功"));
    }

    // 清理重複場次：同課程、同日期、同時段、同名額的場次只保留一筆（保留預約數最多的那筆），其餘強制刪除
    // （之前批量建立重複場次的儲存按鈕沒有防止重複送出，連點過幾次就會把整批場次重複建立好幾倍）
    @PostMapping("/cleanup-duplicates")
    @Transactional
    public MessageRes cleanupDuplicates(Authentication authentication) {
        String operator = authentication != null ? authentication.getName() : "SYSTEM";

        record Key(Long courseId, java.time.LocalDate sessionDate, java.time.LocalTime startTime,
                   java.time.LocalTime endTime, Integer capacity) {
        }

        List<CourseSession> all = courseSessionRepository.findAll();
        Map<Key, List<CourseSession>> groups = all.stream().collect(Collectors.groupingBy(s ->
                new Key(s.getCourse().getId(), s.getSessionDate(), s.getStartTime(), s.getEndTime(), s.getCapacity())));

        int deletedCount = 0;
        for (List<CourseSession> group : groups.values()) {
            if (group.size() <= 1) continue;

            group.sort(Comparator.comparingLong((CourseSession s) ->
                            reservationRepository.countBySession_IdAndStatusIn(s.getId(), OCCUPYING_STATUSES))
                    .reversed()
                    .thenComparing(CourseSession::getId));

            for (CourseSession dup : group.subList(1, group.size())) {
                List<Reservation> reservations = reservationRepository.findBySession_Id(dup.getId());
                List<Waitlist> waitlists = waitlistRepository.findBySession_IdOrderByQueueNoAsc(dup.getId());
                for (Reservation reservation : reservations) {
                    reservationService.forceCancelForSessionDeletion(reservation, operator);
                }
                for (Waitlist waitlist : waitlists) {
                    waitlistRepository.delete(waitlist);
                }
                courseSessionRepository.delete(dup);
                deletedCount++;
            }
        }

        AuditLog log = new AuditLog();
        log.setOperator(operator);
        log.setAction("CLEANUP_DUPLICATE_SESSIONS");
        log.setTargetType("CourseSession");
        log.setDetail("清理重複場次，共刪除 " + deletedCount + " 筆");
        auditLogRepository.save(log);

        return new MessageRes("清理完成，共刪除 " + deletedCount + " 筆重複場次");
    }

    private void applyReq(CourseSession session, CourseSessionReq req) {
        Course course = courseRepository.findById(req.getCourseId())
                .orElseThrow(() -> new RuntimeException("找不到課程，ID: " + req.getCourseId()));
        session.setCourse(course);
        session.setSessionDate(req.getSessionDate());
        session.setStartTime(req.getStartTime());
        session.setEndTime(req.getEndTime());
        session.setCapacity(req.getCapacity());
    }

    // 已點名（出席/缺席）的預約仍佔用名額，避免點名後名額被誤判為釋出
    private static final List<ReservationStatus> OCCUPYING_STATUSES =
            List.of(ReservationStatus.CONFIRMED, ReservationStatus.ATTENDED, ReservationStatus.ABSENT);

    private CourseSessionRes toRes(CourseSession session) {
        long reservedCount = reservationRepository.countBySession_IdAndStatusIn(session.getId(), OCCUPYING_STATUSES);
        return CourseSessionRes.from(session, reservedCount);
    }

    private CourseSession findOrThrow(Long id) {
        return courseSessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到課程場次，ID: " + id));
    }
}
