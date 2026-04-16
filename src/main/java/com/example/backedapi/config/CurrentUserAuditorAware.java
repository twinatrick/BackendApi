package com.example.backedapi.config;

import com.example.backedapi.Enity.User;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.ScopeNotActiveException;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class CurrentUserAuditorAware implements AuditorAware<String> {

    private final ObjectProvider<User> currentUserProvider;

    public CurrentUserAuditorAware(ObjectProvider<User> currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        User currentUser;
        try {
            currentUser = currentUserProvider.getIfAvailable();
        } catch (ScopeNotActiveException ex) {
            return Optional.empty();
        }
        if (currentUser == null) {
            return Optional.empty();
        }
        UUID userId;
        try {
            userId = currentUser.getId();
        } catch (ScopeNotActiveException | IllegalStateException ex) {
            return Optional.empty();
        }
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.of(userId.toString());
    }
}
