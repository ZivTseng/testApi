package com.test.testApi.controller;

import com.test.testApi.dto.req.PlanReq;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.dto.res.PlanRes;
import com.test.testApi.entity.Plan;
import com.test.testApi.repository.PlanRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanRepository planRepository;

    @GetMapping
    public List<PlanRes> list() {
        return planRepository.findAll().stream().map(PlanRes::from).toList();
    }

    @GetMapping("/{id}")
    public PlanRes get(@PathVariable Long id) {
        return PlanRes.from(findOrThrow(id));
    }

    @PostMapping
    public PlanRes create(@Valid @RequestBody PlanReq req) {
        Plan plan = new Plan();
        applyReq(plan, req);
        return PlanRes.from(planRepository.save(plan));
    }

    @PutMapping("/{id}")
    public PlanRes update(@PathVariable Long id, @Valid @RequestBody PlanReq req) {
        Plan plan = findOrThrow(id);
        applyReq(plan, req);
        return PlanRes.from(planRepository.save(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageRes> delete(@PathVariable Long id) {
        planRepository.delete(findOrThrow(id));
        return ResponseEntity.ok(new MessageRes("方案刪除成功"));
    }

    private void applyReq(Plan plan, PlanReq req) {
        plan.setName(req.getName());
        plan.setTotalSessions(req.getTotalSessions());
        plan.setValidityDays(req.getValidityDays());
        plan.setPrice(req.getPrice());
    }

    private Plan findOrThrow(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到方案，ID: " + id));
    }
}
