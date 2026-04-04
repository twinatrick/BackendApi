package com.example.backedapi.config;

import com.example.backedapi.Enity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.annotation.RequestScope;


@Configuration
public class CurrentUserProvider {

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public User currentUser(HttpServletRequest request) {
        return (User) request.getAttribute("user");
    }

}