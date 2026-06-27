package com.test.testApi.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // 從 properties 讀取我們設定的密鑰與過期時間
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // 將字串密鑰轉換成 HMAC-SHA 加密演算法需要的 Key 物件
    private Key key() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // 1. 發證機：登入成功後，根據使用者的 Email 產生一張 JWT Token
    public String generateJwtToken(String email) {
        return Jwts.builder()
                .setSubject(email) // 把 Email 當作 Token 的主體 (Subject)
                .setIssuedAt(new Date()) // 簽發時間
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // 到期時間
                .signWith(key(), SignatureAlgorithm.HS256) // 使用 HS256 加密簽名
                .compact();
    }

    // 2. 驗票機 (讀取)：從前端傳來的 Token 中解碼出 Email
    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key())
                .build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // 家長端 (LIFF) 專用：用家長 ID 簽發一張帶有 type=PARENT 標記的 Token，跟後台管理員 Token 區分開來
    public String generateParentJwtToken(Long parentId) {
        return Jwts.builder()
                .setSubject(String.valueOf(parentId))
                .claim("type", "PARENT")
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isParentToken(String token) {
        Object type = Jwts.parserBuilder().setSigningKey(key())
                .build()
                .parseClaimsJws(token).getBody().get("type");
        return "PARENT".equals(type);
    }

    public Long getParentIdFromJwtToken(String token) {
        String subject = Jwts.parserBuilder().setSigningKey(key())
                .build()
                .parseClaimsJws(token).getBody().getSubject();
        return Long.valueOf(subject);
    }

    // 3. 驗票機 (驗證)：檢查這張 Token 是不是偽造的、有沒有過期
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (Exception e) {
            // 實務上這裡會把詳細錯誤 (如 TokenExpiredException) 印在 Log 裡
            System.err.println("JWT 驗證失敗: " + e.getMessage());
        }
        return false;
    }
}