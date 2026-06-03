package com.example.BackendApi.Config;

import com.example.BackendApi.Entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUserAuditorAware implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        RequestAttributes attributes;
        try {
            attributes = RequestContextHolder.getRequestAttributes();
        } catch (IllegalStateException ex) {
            return Optional.empty();
        }
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return Optional.empty();
        }
        HttpServletRequest request = servletAttributes.getRequest();

        Object userAttribute = request.getAttribute("user");
        if (!(userAttribute instanceof User currentUser)) {
            return Optional.empty();
        }

        UUID userId = currentUser.getId();
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(userId.toString());
    }
}
