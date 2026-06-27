package com.test.testApi;

import com.test.testApi.entity.AdminUser;
import com.test.testApi.entity.SystemParameter;
import com.test.testApi.entity.enums.AdminRole;
import com.test.testApi.repository.AdminUserRepository;
import com.test.testApi.repository.SystemParameterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaAuditing // 啟動自動時間記錄
@EnableScheduling // 啟動候補逾期排程
public class TestApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApiApplication.class, args);
    }

    // 在專案啟動時，自動執行這裡的程式碼
    @Bean
    CommandLineRunner run(AdminUserRepository adminUserRepository,
                           PasswordEncoder passwordEncoder,
                           SystemParameterRepository systemParameterRepository) {
        return args -> {
            // 如果資料庫裡還沒有 admin 這個帳號，就幫我們建一個
            if (!adminUserRepository.existsByUsername("admin")) {
                AdminUser admin = new AdminUser();
                admin.setUsername("admin");
                // 這裡會用 BCrypt 把 "123456" 加密後存進資料庫
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setName("管理員");
                admin.setRole(AdminRole.BOSS);
                adminUserRepository.save(admin);
                System.out.println("✅ 測試帳號建立成功！帳號: admin / 密碼: 123456");
            }

            seedParam(systemParameterRepository, "cancellation_deadline_hours", "24", "取消預約須提前幾小時，逾期取消不退堂數");
            seedParam(systemParameterRepository, "waitlist_hold_minutes", "30", "候補轉正後，家長需在幾分鐘內確認，逾時通知下一位");
        };
    }

    private void seedParam(SystemParameterRepository repo, String key, String value, String description) {
        if (repo.findByParamKey(key).isEmpty()) {
            SystemParameter param = new SystemParameter();
            param.setParamKey(key);
            param.setParamValue(value);
            param.setDescription(description);
            repo.save(param);
        }
    }
}
