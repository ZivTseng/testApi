package com.test.testApi.repository;

import com.test.testApi.entity.Waitlist;
import com.test.testApi.entity.enums.WaitlistStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {
    List<Waitlist> findBySession_IdAndStatusOrderByQueueNoAsc(Long sessionId, WaitlistStatus status);
    List<Waitlist> findBySession_IdOrderByQueueNoAsc(Long sessionId);
    List<Waitlist> findBySession_IdAndStatusIn(Long sessionId, List<WaitlistStatus> statuses);
    List<Waitlist> findByStudent_Id(Long studentId);
    List<Waitlist> findByStatusAndExpireAtBefore(WaitlistStatus status, LocalDateTime time);
    long countBySession_Id(Long sessionId);
}
