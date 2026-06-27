package com.test.testApi.controller;

import com.test.testApi.dto.res.ReservationRes;
import com.test.testApi.dto.res.WaitlistRes;
import com.test.testApi.repository.WaitlistRepository;
import com.test.testApi.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
@RequiredArgsConstructor
public class WaitlistController {

    private final ReservationService reservationService;
    private final WaitlistRepository waitlistRepository;

    @GetMapping("/session/{sessionId}")
    public List<WaitlistRes> bySession(@PathVariable Long sessionId) {
        return waitlistRepository.findBySession_IdOrderByQueueNoAsc(sessionId)
                .stream().map(WaitlistRes::from).toList();
    }

    @GetMapping("/student/{studentId}")
    public List<WaitlistRes> byStudent(@PathVariable Long studentId) {
        return waitlistRepository.findByStudent_Id(studentId)
                .stream().map(WaitlistRes::from).toList();
    }

    @PostMapping("/{id}/confirm")
    public ReservationRes confirm(@PathVariable Long id, Authentication authentication) {
        return reservationService.confirmWaitlist(id, operatorOf(authentication));
    }

    private String operatorOf(Authentication authentication) {
        return authentication != null ? authentication.getName() : "SYSTEM";
    }
}
