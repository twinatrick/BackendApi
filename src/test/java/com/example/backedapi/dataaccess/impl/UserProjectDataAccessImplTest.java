package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.UserProjectRepository;
import com.example.backedapi.Repository.UserRepository;
import com.example.backedapi.Repository.ProjectRepository;
import com.example.backedapi.dataaccess.IUserProjectDataAccess;
import com.example.backedapi.Enity.UserProject;
import com.example.backedapi.Enity.User;
import com.example.backedapi.Enity.Project;
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
 * Integration tests for UserProjectDataAccessImpl.
 * Uses in-memory H2 database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserProjectDataAccessImplTest {

    @Autowired
    private UserProjectRepository userProjectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private IUserProjectDataAccess userProjectDataAccess;

    @BeforeEach
    void setUp() {
        userProjectDataAccess = new UserProjectDataAccessImpl(userProjectRepository);
        userProjectRepository.deleteAll();
        userRepository.deleteAll();
        projectRepository.deleteAll();
    }

    @Test
    @DisplayName("應該檢查 UserProject 是否存在（依 userId 和 projectId）")
    void testExistsByUserIdAndProjectId() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(project);
        userProjectRepository.save(userProject);

        // Act & Assert
        assertTrue(userProjectDataAccess.existsByUserIdAndProjectId(user.getId(), project.getId()));
        assertFalse(userProjectDataAccess.existsByUserIdAndProjectId(UUID.randomUUID(), project.getId()));
    }

    @Test
    @DisplayName("應該保存 UserProject")
    void testSave() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(project);

        // Act
        UserProject saved = userProjectDataAccess.save(userProject);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals(project.getId(), saved.getProject().getId());
    }

    @Test
    @DisplayName("應該根據 projectId 刪除所有 UserProject")
    void testDeleteByProjectId() {
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

        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        UserProject userProject1 = new UserProject();
        userProject1.setUser(user1);
        userProject1.setProject(project);
        userProjectRepository.save(userProject1);

        UserProject userProject2 = new UserProject();
        userProject2.setUser(user2);
        userProject2.setProject(project);
        userProjectRepository.save(userProject2);

        assertEquals(2, userProjectRepository.count());

        // Act
        userProjectDataAccess.deleteByProjectId(project.getId());

        // Assert
        assertEquals(0, userProjectRepository.count());
    }

    @Test
    @DisplayName("應該根據 userId 查詢所有 UserProject")
    void testFindByUserId() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

        Project project1 = new Project();
        project1.setName("專案1");
        projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("專案2");
        projectRepository.save(project2);

        UserProject userProject1 = new UserProject();
        userProject1.setUser(user);
        userProject1.setProject(project1);
        userProjectRepository.save(userProject1);

        UserProject userProject2 = new UserProject();
        userProject2.setUser(user);
        userProject2.setProject(project2);
        userProjectRepository.save(userProject2);

        // Act
        List<UserProject> result = userProjectDataAccess.findByUserId(user.getId());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(up -> up.getProject().getName().equals("專案1")));
        assertTrue(result.stream().anyMatch(up -> up.getProject().getName().equals("專案2")));
    }

    @Test
    @DisplayName("當 userId 不存在時應該返回空列表")
    void testFindByUserId_NotFound() {
        // Act
        List<UserProject> result = userProjectDataAccess.findByUserId(UUID.randomUUID());

        // Assert
        assertTrue(result.isEmpty());
    }
}
