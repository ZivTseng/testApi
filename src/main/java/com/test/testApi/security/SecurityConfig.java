package com.test.testApi.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Autowired
    private LiffAuthTokenFilter liffAuthTokenFilter;

    // 定義密碼加密器 (使用 BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 暴露 AuthenticationManager，我們後面寫登入 API 時會用到
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // 定義安全防護規則 (核心！)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 1. 關閉 CSRF 防護 (因為我們用 JWT，不需要這東西)
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // 2. 設定 Session 策略為 Stateless (無狀態)，因為 JWT 每次請求都會帶 Token
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 3. 設定 API 網址的權限規則
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers(
                                        "/api/auth/**",
                                        "/api/line/**",        // LINE 平台呼叫 Webhook 時不會帶 JWT，需放行
                                        "/api/liff/auth",      // 家長首次用 LIFF 登入時還沒有家長 Token
                                        "/api/liff/link",      // 家長用電話綁定既有資料時還沒有家長 Token
                                        "/v3/api-docs/**",     // 放行 Swagger 資料
                                        "/swagger-ui/**",      // 放行 Swagger UI 介面
                                        "/swagger-ui.html"
                                ).permitAll()
                                .requestMatchers("/api/liff/**").hasRole("PARENT")
                                .anyRequest().authenticated()
                );

        // 4. 把我們的警衛安插在 Spring Security 預設的帳密檢查器之前：後台管理員 Token 跟家長 LIFF Token 各自獨立驗證
        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(liffAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允許來自前端 Vite 的請求
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        // 允許所有 HTTP 方法 (GET, POST, PUT, DELETE, OPTIONS)
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允許所有 Header
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // 允許帶上 Cookie (如果有需要)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}