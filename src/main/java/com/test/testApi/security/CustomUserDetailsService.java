package com.test.testApi.security;

import com.test.testApi.entity.AdminUser;
import com.test.testApi.repository.AdminUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到該帳號: " + username));

        // 權限先給空的 ArrayList，後續可依 role 補上對應的 GrantedAuthority
        return new org.springframework.security.core.userdetails.User(
                adminUser.getUsername(),
                adminUser.getPassword(),
                new ArrayList<>()
        );
    }
}
