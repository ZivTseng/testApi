package com.test.testApi.controller;

import com.test.testApi.dto.req.LoginReq;
import com.test.testApi.dto.req.RegisterReq;
import com.test.testApi.dto.res.LoginRes;
import com.test.testApi.dto.res.MessageRes;
import com.test.testApi.entity.User;
import com.test.testApi.repository.UserRepository;
import com.test.testApi.security.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginReq loginReq) {
        log.info("收到登入請求，嘗試登入的 Email: {}", loginReq.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginReq.getEmail(), loginReq.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtUtils.generateJwtToken(userDetails.getUsername());

            log.info("登入成功！發放 Token 給 Email: {}", userDetails.getUsername());

            return ResponseEntity.ok(new LoginRes(jwt, userDetails.getUsername()));

        } catch (Exception e) {
            log.error("登入失敗，原因: {}", e.getMessage());
            throw e;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterReq req) {

        // 使用 req.getEmail() 取值
        if (userRepository.existsByEmail(req.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    // 回傳 MessageRes
                    .body(new MessageRes("錯誤：該 Email 已被註冊！"));
        }

        User user = new User();
        user.setUserName(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));

        userRepository.save(user);

        // 回傳 MessageRes
        return ResponseEntity.ok(new MessageRes("註冊成功！"));
    }
}