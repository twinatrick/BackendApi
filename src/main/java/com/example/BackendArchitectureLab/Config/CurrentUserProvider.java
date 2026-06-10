package com.example.BackendArchitectureLab.Config;

import com.example.BackendArchitectureLab.Entity.User;
import com.example.BackendArchitectureLab.Security.CustomUserDetails;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class CurrentUserProvider {

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        return null;
    }

}