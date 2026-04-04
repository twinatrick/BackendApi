package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.RoleRepository;
import com.example.backedapi.Repository.UserRepository;
import com.example.backedapi.Repository.UserRoleRepository;
import com.example.backedapi.dataaccess.IUserRoleDataAccess;
import com.example.backedapi.Enity.Role;
import com.example.backedapi.Enity.User;
import com.example.backedapi.Enity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for UserRoleDataAccessImpl.
 * Uses in-memory H2 database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserRoleDataAccessImplTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private IUserRoleDataAccess userRoleDataAccess;

    @BeforeEach
    void setUp() {
        userRoleDataAccess = new UserRoleDataAccessImpl(userRoleRepository);
        userRoleRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    @DisplayName("Should delete user-role associations")
    void testDeleteAllByUserInAndRoleIn() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setDisabled(false);
        userRepository.save(user1);

        Role role1 = new Role();
        role1.setName("ROLE_ADMIN");
        roleRepository.save(role1);

        UserRole userRole = new UserRole();
        userRole.setUser(user1);
        userRole.setRole(role1);
        userRoleRepository.save(userRole);

        // Verify association exists
        long countBefore = userRoleRepository.count();
        assertEquals(1, countBefore);

        // Act
        userRoleDataAccess.deleteAllByUserInAndRoleIn(List.of(user1), List.of(role1));

        // Assert
        long countAfter = userRoleRepository.count();
        assertEquals(0, countAfter);
    }

    @Test
    @DisplayName("Should delete multiple user-role associations")
    void testDeleteAllByUserInAndRoleIn_Multiple() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setDisabled(false);
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setDisabled(false);
        userRepository.save(user2);

        Role role1 = new Role();
        role1.setName("ROLE_ADMIN");
        roleRepository.save(role1);

        Role role2 = new Role();
        role2.setName("ROLE_USER");
        roleRepository.save(role2);

        UserRole userRole1 = new UserRole();
        userRole1.setUser(user1);
        userRole1.setRole(role1);
        userRoleRepository.save(userRole1);

        UserRole userRole2 = new UserRole();
        userRole2.setUser(user2);
        userRole2.setRole(role2);
        userRoleRepository.save(userRole2);

        // Act
        userRoleDataAccess.deleteAllByUserInAndRoleIn(
                List.of(user1, user2),
                List.of(role1, role2)
        );

        // Assert
        long countAfter = userRoleRepository.count();
        assertEquals(0, countAfter);
    }

    @Test
    @DisplayName("Should not delete when no matching associations exist")
    void testDeleteAllByUserInAndRoleIn_NoMatch() {
        // Arrange
        User user1 = new User();
        user1.setEmail("user1@example.com");
        user1.setPassword("password");
        user1.setDisabled(false);
        userRepository.save(user1);

        Role role1 = new Role();
        role1.setName("ROLE_ADMIN");
        roleRepository.save(role1);

        UserRole userRole = new UserRole();
        userRole.setUser(user1);
        userRole.setRole(role1);
        userRoleRepository.save(userRole);

        // Create different user and role
        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setPassword("password");
        user2.setDisabled(false);
        userRepository.save(user2);

        Role role2 = new Role();
        role2.setName("ROLE_USER");
        roleRepository.save(role2);

        // Act - try to delete with non-matching user/role
        userRoleDataAccess.deleteAllByUserInAndRoleIn(List.of(user2), List.of(role2));

        // Assert - original association should still exist
        long countAfter = userRoleRepository.count();
        assertEquals(1, countAfter);
    }

    @Test
    @DisplayName("Should handle empty lists gracefully")
    void testDeleteAllByUserInAndRoleIn_EmptyLists() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() ->
                userRoleDataAccess.deleteAllByUserInAndRoleIn(List.of(), List.of())
        );
    }
}
