package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.SkillMapUserAndProjectRepository;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.db.Skill;
import com.example.backedapi.model.db.SkillMapUserAndProject;
import com.example.backedapi.model.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SkillMapUserAndProjectDataAccessImpl.
 * Uses in-memory H2 database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class SkillMapUserAndProjectDataAccessImplTest {

    @Autowired
    private SkillMapUserAndProjectRepository skillMapRepository;

    @Autowired
    private TestEntityManager entityManager;

    private ISkillMapUserAndProjectDataAccess skillMapDataAccess;

    @BeforeEach
    void setUp() {
        skillMapDataAccess = new SkillMapUserAndProjectDataAccessImpl(skillMapRepository);
        skillMapRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    @DisplayName("Should find all mappings by project")
    void testFindByProject() {
        // Arrange
        Project project1 = createAndPersistProject("Project A", "Description A");
        Project project2 = createAndPersistProject("Project B", "Description B");
        User user = createAndPersistUser("user@test.com", "Test User");
        Skill skill = createAndPersistSkill("Java", "Java skills");

        SkillMapUserAndProject mapping1 = new SkillMapUserAndProject();
        mapping1.setProject(project1);
        mapping1.setUser(user);
        mapping1.setSkill(skill);
        skillMapRepository.save(mapping1);

        SkillMapUserAndProject mapping2 = new SkillMapUserAndProject();
        mapping2.setProject(project1);
        mapping2.setUser(user);
        mapping2.setSkill(skill);
        skillMapRepository.save(mapping2);

        SkillMapUserAndProject mapping3 = new SkillMapUserAndProject();
        mapping3.setProject(project2);
        mapping3.setUser(user);
        mapping3.setSkill(skill);
        skillMapRepository.save(mapping3);

        entityManager.flush();
        entityManager.clear();

        // Act
        List<SkillMapUserAndProject> results = skillMapDataAccess.findByProject(project1);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(m -> m.getProject().getKey().equals(project1.getKey())));
    }

    @Test
    @DisplayName("Should return empty list when no mappings exist for project")
    void testFindByProject_Empty() {
        // Arrange
        Project project = createAndPersistProject("Empty Project", "No mappings");

        // Act
        List<SkillMapUserAndProject> results = skillMapDataAccess.findByProject(project);

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Should delete all mappings in batch")
    void testDeleteAll() {
        // Arrange
        Project project = createAndPersistProject("Project X", "Description X");
        User user = createAndPersistUser("batch@test.com", "Batch User");
        Skill skill = createAndPersistSkill("Python", "Python skills");

        SkillMapUserAndProject mapping1 = new SkillMapUserAndProject();
        mapping1.setProject(project);
        mapping1.setUser(user);
        mapping1.setSkill(skill);
        skillMapRepository.save(mapping1);

        SkillMapUserAndProject mapping2 = new SkillMapUserAndProject();
        mapping2.setProject(project);
        mapping2.setUser(user);
        mapping2.setSkill(skill);
        skillMapRepository.save(mapping2);

        entityManager.flush();
        List<SkillMapUserAndProject> mappingsToDelete = List.of(mapping1, mapping2);

        // Act
        skillMapDataAccess.deleteAll(mappingsToDelete);
        entityManager.flush();

        // Assert
        List<SkillMapUserAndProject> remaining = skillMapRepository.findAll();
        assertTrue(remaining.isEmpty());
    }

    @Test
    @DisplayName("Should handle empty list in deleteAll")
    void testDeleteAll_EmptyList() {
        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> skillMapDataAccess.deleteAll(List.of()));
    }

    @Test
    @DisplayName("Should find one mapping by example")
    void testFindOne_Found() {
        // Arrange
        Project project = createAndPersistProject("Project Y", "Description Y");
        User user = createAndPersistUser("findone@test.com", "FindOne User");
        Skill skill = createAndPersistSkill("JavaScript", "JS skills");

        SkillMapUserAndProject mapping = new SkillMapUserAndProject();
        mapping.setProject(project);
        mapping.setUser(user);
        mapping.setSkill(skill);
        skillMapRepository.save(mapping);
        entityManager.flush();

        // Create probe for Example
        SkillMapUserAndProject probe = new SkillMapUserAndProject();
        probe.setUser(user);
        probe.setSkill(skill);
        Example<SkillMapUserAndProject> example = Example.of(probe);

        // Act
        Optional<SkillMapUserAndProject> result = skillMapDataAccess.findOne(example);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(user.getKey(), result.get().getUser().getKey());
        assertEquals(skill.getKey(), result.get().getSkill().getKey());
    }

    @Test
    @DisplayName("Should return empty optional when no mapping matches example")
    void testFindOne_NotFound() {
        // Arrange
        User nonExistentUser = new User();
        nonExistentUser.setEmail("nonexistent@test.com");

        SkillMapUserAndProject probe = new SkillMapUserAndProject();
        probe.setUser(nonExistentUser);
        Example<SkillMapUserAndProject> example = Example.of(probe);

        // Act
        Optional<SkillMapUserAndProject> result = skillMapDataAccess.findOne(example);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should save a new mapping")
    void testSave_NewMapping() {
        // Arrange
        Project project = createAndPersistProject("New Project", "New Description");
        User user = createAndPersistUser("save@test.com", "Save User");
        Skill skill = createAndPersistSkill("Docker", "Container skills");

        SkillMapUserAndProject mapping = new SkillMapUserAndProject();
        mapping.setProject(project);
        mapping.setUser(user);
        mapping.setSkill(skill);

        // Act
        SkillMapUserAndProject savedMapping = skillMapDataAccess.save(mapping);
        entityManager.flush();

        // Assert
        assertNotNull(savedMapping);
        assertNotNull(savedMapping.getKey());
        assertEquals(project.getKey(), savedMapping.getProject().getKey());
        assertEquals(user.getKey(), savedMapping.getUser().getKey());
        assertEquals(skill.getKey(), savedMapping.getSkill().getKey());
    }

    @Test
    @DisplayName("Should update an existing mapping")
    void testSave_UpdateMapping() {
        // Arrange
        Project project1 = createAndPersistProject("Project 1", "Desc 1");
        Project project2 = createAndPersistProject("Project 2", "Desc 2");
        User user = createAndPersistUser("update@test.com", "Update User");
        Skill skill = createAndPersistSkill("React", "React framework");

        SkillMapUserAndProject mapping = new SkillMapUserAndProject();
        mapping.setProject(project1);
        mapping.setUser(user);
        mapping.setSkill(skill);
        SkillMapUserAndProject savedMapping = skillMapRepository.save(mapping);
        entityManager.flush();

        // Act - update the mapping with different project
        savedMapping.setProject(project2);
        SkillMapUserAndProject updatedMapping = skillMapDataAccess.save(savedMapping);
        entityManager.flush();

        // Assert
        assertNotNull(updatedMapping);
        assertEquals(savedMapping.getKey(), updatedMapping.getKey());
        assertEquals(project2.getKey(), updatedMapping.getProject().getKey());
    }

    @Test
    @DisplayName("Should find all mappings by example")
    void testFindAll_ByExample() {
        // Arrange
        Project project = createAndPersistProject("Project Z", "Description Z");
        User user1 = createAndPersistUser("user1@test.com", "User 1");
        User user2 = createAndPersistUser("user2@test.com", "User 2");
        Skill skill = createAndPersistSkill("Angular", "Angular framework");

        SkillMapUserAndProject mapping1 = new SkillMapUserAndProject();
        mapping1.setProject(project);
        mapping1.setUser(user1);
        mapping1.setSkill(skill);
        skillMapRepository.save(mapping1);

        SkillMapUserAndProject mapping2 = new SkillMapUserAndProject();
        mapping2.setProject(project);
        mapping2.setUser(user2);
        mapping2.setSkill(skill);
        skillMapRepository.save(mapping2);

        entityManager.flush();

        // Create probe for Example
        SkillMapUserAndProject probe = new SkillMapUserAndProject();
        probe.setSkill(skill);
        Example<SkillMapUserAndProject> example = Example.of(probe);

        // Act
        List<SkillMapUserAndProject> results = skillMapDataAccess.findAll(example);

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(m -> m.getSkill().getKey().equals(skill.getKey())));
    }

    @Test
    @DisplayName("Should return empty list when no mappings match example")
    void testFindAll_ByExample_Empty() {
        // Arrange
        Skill nonExistentSkill = new Skill();
        nonExistentSkill.setName("NonExistent");

        SkillMapUserAndProject probe = new SkillMapUserAndProject();
        probe.setSkill(nonExistentSkill);
        Example<SkillMapUserAndProject> example = Example.of(probe);

        // Act
        List<SkillMapUserAndProject> results = skillMapDataAccess.findAll(example);

        // Assert
        assertTrue(results.isEmpty());
    }

    // Helper methods to create and persist test data
    private Project createAndPersistProject(String name, String description) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setCreatedBy("test");
        project.setCreatedTime(new Date());
        return entityManager.persistAndFlush(project);
    }

    private User createAndPersistUser(String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword("password");
        user.setCreatedBy("test");
        user.setCreatedTime(new Date());
        user.setDisabled(false);
        return entityManager.persistAndFlush(user);
    }

    private Skill createAndPersistSkill(String name, String description) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setDescription(description);
        skill.setCreatedBy("test");
        skill.setCreatedTime(new Date());
        return entityManager.persistAndFlush(skill);
    }
}
