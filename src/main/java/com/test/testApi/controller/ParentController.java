package com.test.testApi.controller;

import com.test.testApi.dto.req.ParentReq;
import com.test.testApi.dto.res.BindingCodeRes;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.dto.res.ParentRes;
import com.test.testApi.entity.Parent;
import com.test.testApi.entity.Student;
import com.test.testApi.repository.ParentRepository;
import com.test.testApi.repository.StudentRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/parents")
@RequiredArgsConstructor
public class ParentController {

    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;

    @GetMapping
    public List<ParentRes> list() {
        return parentRepository.findAll(Sort.by("name")).stream().map(ParentRes::from).toList();
    }

    @GetMapping("/{id}")
    public ParentRes get(@PathVariable Long id) {
        return ParentRes.from(findOrThrow(id));
    }

    @PostMapping
    @Transactional
    public ParentRes create(@Valid @RequestBody ParentReq req) {
        Parent parent = new Parent();
        applyReq(parent, req);
        parentRepository.save(parent);
        syncStudents(parent, req.getStudentIds());
        return toResWithStudentIds(parent, req.getStudentIds());
    }

    @PutMapping("/{id}")
    @Transactional
    public ParentRes update(@PathVariable Long id, @Valid @RequestBody ParentReq req) {
        Parent parent = findOrThrow(id);
        applyReq(parent, req);
        parentRepository.save(parent);
        syncStudents(parent, req.getStudentIds());
        return toResWithStudentIds(parent, req.getStudentIds());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<MessageRes> delete(@PathVariable Long id) {
        Parent parent = findOrThrow(id);

        // Student 是多對多關係的擁有端，刪除家長前要先把這個家長從所有孩子的 parents 集合中移除，
        // 否則 parent_student join table 還留著參照這個家長的列，刪除 Parent 會被外鍵限制擋住
        for (Student student : studentRepository.findByParents_Id(id)) {
            student.getParents().remove(parent);
            studentRepository.save(student);
        }

        parentRepository.delete(parent);
        return ResponseEntity.ok(new MessageRes("家長刪除成功"));
    }

    // 產生一次性綁定驗證碼，由館方告知家長後，家長在 LINE 對話中輸入此碼即可完成綁定
    @PostMapping("/{id}/binding-code")
    public BindingCodeRes generateBindingCode(@PathVariable Long id) {
        Parent parent = findOrThrow(id);
        String code = String.format("%06d", new SecureRandom().nextInt(1_000_000));
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(15);

        parent.setBindingCode(code);
        parent.setBindingCodeExpireAt(expireAt);
        parentRepository.save(parent);

        return new BindingCodeRes(code, expireAt);
    }

    // 館方電話聯絡/收款確認完成後，標記這筆自助報名資料為已確認
    @PostMapping("/{id}/confirm-review")
    public ParentRes confirmReview(@PathVariable Long id) {
        Parent parent = findOrThrow(id);
        parent.setPendingReview(false);
        parentRepository.save(parent);
        return ParentRes.from(parent);
    }

    private void applyReq(Parent parent, ParentReq req) {
        parent.setName(req.getName());
        parent.setPhone(req.getPhone());
        // 表單沒填寫時前端會送空字串，這裡正規化成 null，否則「尚未綁定 LINE」的判斷（lineUserId IS NULL）會失效
        parent.setLineUserId(
                req.getLineUserId() == null || req.getLineUserId().isBlank() ? null : req.getLineUserId());
    }

    /** Student 是這個多對多關係的擁有端，所以從家長端調整孩子清單時，要逐一更新 Student.parents 集合 */
    private void syncStudents(Parent parent, List<Long> studentIds) {
        Set<Student> targetStudents = studentIds == null
                ? new HashSet<>()
                : new HashSet<>(studentRepository.findAllById(studentIds));

        for (Student current : studentRepository.findByParents_Id(parent.getId())) {
            if (!targetStudents.contains(current)) {
                current.getParents().remove(parent);
                studentRepository.save(current);
            }
        }

        for (Student target : targetStudents) {
            target.getParents().add(parent);
            studentRepository.save(target);
        }
    }

    // Parent.students 是 mappedBy 端，在同一個 Hibernate session 中於 sync 後立刻讀取容易讀到尚未刷新的舊集合，
    // 因此直接用剛同步完成的目標清單組回應，確保 API 回傳值與實際寫入結果一致
    private ParentRes toResWithStudentIds(Parent parent, List<Long> studentIds) {
        return new ParentRes(parent.getId(), parent.getName(), parent.getPhone(), parent.getLineUserId(),
                studentIds == null ? List.of() : studentIds, parent.isPendingReview());
    }

    private Parent findOrThrow(Long id) {
        return parentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到家長，ID: " + id));
    }
}
