package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.UserRepository;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.model.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserDataAccessImpl.
 * Uses in-memory H2 database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserDataAccessImplTest {

    @Autowired
    private UserRepository userRepository;

    private IUserDataAccess userDataAccess;

    @BeforeEach
    void setUp() {
        userDataAccess = new UserDataAccessImpl(userRepository);
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should save user successfully")
    void testSave() {
        // Arrange
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("hashedPassword");
        user.setDisabled(false);

        // Act
        userDataAccess.save(user);

        // Assert
        List<User> users = userRepository.findAll();
        assertEquals(1, users.size());
        assertEquals("test@example.com", users.get(0).getEmail());
    }

    @Test
    @DisplayName("Should find all users")
    void testFindAll() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password1");
        user1.setDisabled(false);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password2");
        user2.setDisabled(false);

        userRepository.save(user1);
        userRepository.save(user2);

        // Act
        List<User> result = userDataAccess.findAll();

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should find users by email")
    void testFindByEmail() {
        // Arrange
        User user = new User();
        user.setEmail("find@example.com");
        user.setPassword("hashedPassword");
        user.setDisabled(false);
        userRepository.save(user);

        // Act
        List<User> result = userDataAccess.findByEmail("find@example.com");

        // Assert
        assertEquals(1, result.size());
        assertEquals("find@example.com", result.get(0).getEmail());
    }

    @Test
    @DisplayName("Should return empty list when email not found")
    void testFindByEmail_NotFound() {
        // Act
        List<User> result = userDataAccess.findByEmail("nonexistent@example.com");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should update existing user")
    void testSave_Update() {
        // Arrange
        User user = new User();
        user.setEmail("update@example.com");
        user.setPassword("oldPassword");
        user.setDisabled(false);
        userRepository.save(user);

        // Get saved user
        List<User> savedUsers = userRepository.findByEmail("update@example.com");
        User savedUser = savedUsers.get(0);
        UUID userKey = savedUser.getKey();

        // Modify user
        savedUser.setPassword("newPassword");

        // Act
        userDataAccess.save(savedUser);

        // Assert
        List<User> updatedUsers = userRepository.findByEmail("update@example.com");
        assertEquals(1, updatedUsers.size());
        assertEquals("newPassword", updatedUsers.get(0).getPassword());
        assertEquals(userKey, updatedUsers.get(0).getKey()); // Same key
    }
}
