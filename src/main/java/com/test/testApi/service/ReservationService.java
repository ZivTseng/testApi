package com.test.testApi.service;

import com.test.testApi.dto.res.BookingResultRes;
import com.test.testApi.dto.res.ReservationRes;
import com.test.testApi.dto.res.WaitlistRes;
import com.test.testApi.entity.*;
import com.test.testApi.entity.enums.ReservationStatus;
import com.test.testApi.entity.enums.WaitlistStatus;
import com.test.testApi.line.LineMessagingService;
import com.test.testApi.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WaitlistRepository waitlistRepository;
    private final CourseSessionRepository courseSessionRepository;
    private final StudentRepository studentRepository;
    private final StudentPlanRepository studentPlanRepository;
    private final CreditLogRepository creditLogRepository;
    private final AuditLogRepository auditLogRepository;
    private final SystemParameterService systemParameterService;
    private final LineMessagingService lineMessagingService;

    // 已點名（出席/缺席）的預約仍佔用名額，避免點名後名額被誤判為釋出
    private static final List<ReservationStatus> OCCUPYING_STATUSES =
            List.of(ReservationStatus.CONFIRMED, ReservationStatus.ATTENDED, ReservationStatus.ABSENT);

    @Transactional
    public BookingResultRes book(Long studentId, Long sessionId, boolean trial, String operator) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("找不到學員，ID: " + studentId));
        CourseSession session = courseSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("找不到課程場次，ID: " + sessionId));

        long occupiedCount = reservationRepository.countBySession_IdAndStatusIn(sessionId, OCCUPYING_STATUSES);

        if (occupiedCount < session.getCapacity()) {
            StudentPlan plan = null;
            if (!trial) {
                plan = findUsablePlan(studentId)
                        .orElseThrow(() -> new RuntimeException("學員已無可用堂數，請先儲值方案"));
                plan.setRemainingSessions(plan.getRemainingSessions() - 1);
                studentPlanRepository.save(plan);
                saveCreditLog(plan, -1, "預約扣除堂數", operator);
            }

            Reservation reservation = new Reservation();
            reservation.setStudent(student);
            reservation.setSession(session);
            reservation.setStudentPlan(plan);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setTrial(trial);
            reservationRepository.save(reservation);

            saveAuditLog(operator, "CREATE_RESERVATION", "Reservation", reservation.getId(),
                    "學員 " + student.getName() + " 預約場次 #" + sessionId + (trial ? "（體驗課）" : ""));

            return BookingResultRes.reserved(ReservationRes.from(reservation));
        }

        int nextQueueNo = (int) waitlistRepository.countBySession_Id(sessionId) + 1;
        Waitlist waitlist = new Waitlist();
        waitlist.setStudent(student);
        waitlist.setSession(session);
        waitlist.setQueueNo(nextQueueNo);
        waitlist.setStatus(WaitlistStatus.WAITING);
        waitlistRepository.save(waitlist);

        saveAuditLog(operator, "JOIN_WAITLIST", "Waitlist", waitlist.getId(),
                "學員 " + student.getName() + " 加入場次 #" + sessionId + " 候補，順位 " + nextQueueNo);

        return BookingResultRes.waitlisted(WaitlistRes.from(waitlist));
    }

    @Transactional
    public ReservationRes cancel(Long reservationId, String operator) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("找不到預約，ID: " + reservationId));

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new RuntimeException("此預約已取消或已結束，無法再次取消");
        }

        int deadlineHours = systemParameterService.getInt("cancellation_deadline_hours", 24);
        LocalDateTime sessionStart = LocalDateTime.of(
                reservation.getSession().getSessionDate(),
                reservation.getSession().getStartTime());
        boolean withinDeadline = LocalDateTime.now().isBefore(sessionStart.minusHours(deadlineHours));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        String refundNote = "未退還堂數（已超過取消期限）";
        if (!reservation.isTrial() && reservation.getStudentPlan() != null && withinDeadline) {
            StudentPlan plan = reservation.getStudentPlan();
            plan.setRemainingSessions(plan.getRemainingSessions() + 1);
            studentPlanRepository.save(plan);
            saveCreditLog(plan, 1, "取消預約退還堂數", operator);
            refundNote = "已退還 1 堂課";
        }

        saveAuditLog(operator, "CANCEL_RESERVATION", "Reservation", reservation.getId(),
                "取消預約，" + refundNote);

        promoteNextWaitlist(reservation.getSession().getId(), operator);

        return ReservationRes.from(reservation);
    }

    @Transactional
    public ReservationRes confirmWaitlist(Long waitlistId, String operator) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new RuntimeException("找不到候補紀錄，ID: " + waitlistId));

        if (waitlist.getStatus() != WaitlistStatus.NOTIFIED) {
            throw new RuntimeException("此候補尚未通知轉正或已逾期，無法確認");
        }

        StudentPlan plan = findUsablePlan(waitlist.getStudent().getId())
                .orElseThrow(() -> new RuntimeException("學員已無可用堂數，請先儲值方案"));
        plan.setRemainingSessions(plan.getRemainingSessions() - 1);
        studentPlanRepository.save(plan);
        saveCreditLog(plan, -1, "候補轉正扣除堂數", operator);

        Reservation reservation = new Reservation();
        reservation.setStudent(waitlist.getStudent());
        reservation.setSession(waitlist.getSession());
        reservation.setStudentPlan(plan);
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setTrial(false);
        reservationRepository.save(reservation);

        waitlist.setStatus(WaitlistStatus.CONFIRMED);
        waitlistRepository.save(waitlist);

        saveAuditLog(operator, "CONFIRM_WAITLIST", "Waitlist", waitlist.getId(),
                "候補轉正成功，建立預約 #" + reservation.getId());

        return ReservationRes.from(reservation);
    }

    /** 點名：將預約標記為出席或缺席，作為招生/出席率報表的資料來源 */
    @Transactional
    public ReservationRes markAttendance(Long reservationId, ReservationStatus attendanceStatus, String operator) {
        if (attendanceStatus != ReservationStatus.ATTENDED && attendanceStatus != ReservationStatus.ABSENT) {
            throw new RuntimeException("點名狀態只能是 ATTENDED 或 ABSENT");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("找不到預約，ID: " + reservationId));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new RuntimeException("此預約已取消，無法點名");
        }

        reservation.setStatus(attendanceStatus);
        reservationRepository.save(reservation);

        saveAuditLog(operator, "MARK_ATTENDANCE", "Reservation", reservation.getId(),
                "學員 " + reservation.getStudent().getName() + " 點名結果：" + attendanceStatus);

        return ReservationRes.from(reservation);
    }

    /**
     * 因場次被刪除而強制移除預約：若原本是已確認的預約，一律全額退還堂數（館方決定取消，不適用取消期限規則）。
     * 預約紀錄本身會被刪除而非僅標記取消，因為 session_id 為必填外鍵，場次刪除後不能留下指向它的紀錄；
     * 實際發生過什麼事已經寫進稽核紀錄，刪除資料列本身不影響可追溯性。
     */
    @Transactional
    public void forceCancelForSessionDeletion(Reservation reservation, String operator) {
        if (reservation.getStatus() == ReservationStatus.CONFIRMED
                && !reservation.isTrial()
                && reservation.getStudentPlan() != null) {
            StudentPlan plan = reservation.getStudentPlan();
            plan.setRemainingSessions(plan.getRemainingSessions() + 1);
            studentPlanRepository.save(plan);
            saveCreditLog(plan, 1, "場次刪除，全額退還堂數", operator);
        }

        saveAuditLog(operator, "FORCE_DELETE_RESERVATION", "Reservation", reservation.getId(),
                "場次被刪除，移除學員 " + reservation.getStudent().getName() + " 的預約紀錄（原狀態："
                        + reservation.getStatus() + "）");

        reservationRepository.delete(reservation);
    }

    /** 通知候補名單中順位最前的一位轉正，並給予保留期限 */
    @Transactional
    public void promoteNextWaitlist(Long sessionId, String operator) {
        List<Waitlist> waiting = waitlistRepository.findBySession_IdAndStatusOrderByQueueNoAsc(sessionId, WaitlistStatus.WAITING);
        if (waiting.isEmpty()) {
            return;
        }
        Waitlist next = waiting.get(0);
        int holdMinutes = systemParameterService.getInt("waitlist_hold_minutes", 30);
        LocalDateTime now = LocalDateTime.now();
        next.setStatus(WaitlistStatus.NOTIFIED);
        next.setNotifiedAt(now);
        next.setExpireAt(now.plusMinutes(holdMinutes));
        waitlistRepository.save(next);

        saveAuditLog(operator, "NOTIFY_WAITLIST", "Waitlist", next.getId(),
                "通知候補轉正，保留至 " + next.getExpireAt());

        Student student = next.getStudent();
        CourseSession session = next.getSession();
        lineMessagingService.pushToStudentParents(student, String.format(
                "好消息！%s 的候補已轉正，場次：%s %s。請於 %d 分鐘內到後台確認，逾時將自動通知下一位候補。",
                student.getName(), session.getSessionDate(), session.getStartTime(), holdMinutes));
    }

    /** 找出學員目前可用（未過期、有剩餘堂數）的儲值方案，優先使用最快到期者 */
    private Optional<StudentPlan> findUsablePlan(Long studentId) {
        LocalDate today = LocalDate.now();
        return studentPlanRepository.findByStudent_Id(studentId).stream()
                .filter(p -> p.getRemainingSessions() != null && p.getRemainingSessions() > 0)
                .filter(p -> p.getExpireDate() != null && !p.getExpireDate().isBefore(today))
                .min((a, b) -> a.getExpireDate().compareTo(b.getExpireDate()));
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
