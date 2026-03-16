package com.test.testApi;

import com.test.testApi.entity.User;
import com.test.testApi.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing // 啟動自動時間記錄
public class TestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApiApplication.class, args);
    }

    // 在專案啟動時，自動執行這裡的程式碼
    @Bean
    CommandLineRunner run(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 如果資料庫裡還沒有 admin@test.com 這個帳號，就幫我們建一個
            if (!userRepository.existsByEmail("admin@test.com")) {
                User admin = new User();
                admin.setEmail("admin@test.com");
                // 這裡會用 BCrypt 把 "123456" 加密後存進資料庫
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setUserName("管理員");
                userRepository.save(admin);
                System.out.println("✅ 測試帳號建立成功！帳號: admin@test.com / 密碼: 123456");
            }
        };
    }
}