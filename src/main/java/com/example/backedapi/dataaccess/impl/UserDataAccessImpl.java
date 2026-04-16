package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.UserRepository;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.Enity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of IUserDataAccess.
 * Delegates to Spring Data JPA UserRepository.
 */
@Component
@RequiredArgsConstructor
public class UserDataAccessImpl implements IUserDataAccess {

    private final UserRepository userRepository;

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(UUID key) {
        return userRepository.findById(key);
    }

    @Override
    public List<User> findAllById(List<UUID> keys) {
        return userRepository.findAllById(keys);
    }
}
