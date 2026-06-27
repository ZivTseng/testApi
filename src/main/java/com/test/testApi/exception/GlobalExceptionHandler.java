package com.test.testApi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 處理資料庫約束衝突（例如學號重複、刪除時仍有關聯資料），轉成易讀訊息而非原始 SQL 錯誤
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Map<String, Object> body = new HashMap<>();
        String rootMessage = e.getMostSpecificCause().getMessage();
        String friendlyMessage;
        if (rootMessage != null && rootMessage.contains("Duplicate entry")) {
            friendlyMessage = "資料重複，請確認輸入的唯一值（例如學號、帳號）是否已被使用";
        } else if (rootMessage != null && rootMessage.contains("foreign key constraint fails")) {
            friendlyMessage = "此資料已被其他紀錄關聯使用（例如已有預約、候補或繳費紀錄），請先處理相關紀錄後再刪除";
        } else {
            friendlyMessage = "資料寫入失敗，請確認輸入內容是否正確";
        }

        body.put("message", friendlyMessage);
        body.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // 處理找不到資源的錯誤 (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", e.getMessage());
        body.put("status", HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 處理 Bean Validation 驗證失敗 (例如 Email 格式不對)
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {

        Map<String, Object> body = new HashMap<>();
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        body.put("message", errorMessage);
        body.put("status", HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}