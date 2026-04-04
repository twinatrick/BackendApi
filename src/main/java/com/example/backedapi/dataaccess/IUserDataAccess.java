package com.example.backedapi.dataaccess;

import com.example.backedapi.Enity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access interface for User entity operations.
 * Abstracts UserRepository operations for service layer.
 */
public interface IUserDataAccess {

    /**
     * Save a user entity.
     *
     * @param user the user to save
     */
    void save(User user);

    /**
     * Find all users.
     *
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Find users by email address.
     *
     * @param email the email to search for
     * @return list of users with matching email
     */
    List<User> findByEmail(String email);

    /**
     * Find a user by their key.
     *
     * @param key the user UUID
     * @return optional containing the user if found
     */
    Optional<User> findById(UUID key);

    /**
     * Find all users by their keys.
     *
     * @param keys list of user UUIDs
     * @return list of users matching the keys
     */
    List<User> findAllById(List<UUID> keys);
}
