package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Repository.ProjectSkillRepository;
import com.example.backendApi.Repository.ProjectRepository;
import com.example.backendApi.Repository.SkillRepository;
import com.example.backendApi.Repository.SkillLevelRepository;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.Enity.ProjectSkill;
import com.example.backendApi.Enity.Project;
import com.example.backendApi.Enity.Skill;
import com.example.backendApi.Enity.SkillLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for ProjectSkillDataAccessImpl.
 * Uses in-memory H2 database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class ProjectSkillDataAccessImplTest {

    @Autowired
    private ProjectSkillRepository projectSkillRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillLevelRepository skillLevelRepository;

    private IProjectSkillDataAccess projectSkillDataAccess;

    @BeforeEach
    void setUp() {
        projectSkillDataAccess = new ProjectSkillDataAccessImpl(projectSkillRepository);
        projectSkillRepository.deleteAll();
        projectRepository.deleteAll();
        skillRepository.deleteAll();
        skillLevelRepository.deleteAll();
    }

    @Test
    @DisplayName("應該檢查 ProjectSkill 是否存在（依 projectId 和 skillId）")
    void testExistsByProjectIdAndSkillId() {
        // Arrange
        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Default");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        ProjectSkill projectSkill = new ProjectSkill();
        projectSkill.setProject(project);
        projectSkill.setSkill(skill);
        projectSkill.setSkillLevel(skillLevel);
        projectSkillRepository.save(projectSkill);

        // Act & Assert
        assertTrue(projectSkillDataAccess.existsByProjectIdAndSkillId(project.getId(), skill.getId()));
        assertFalse(projectSkillDataAccess.existsByProjectIdAndSkillId(UUID.randomUUID(), skill.getId()));
    }

    @Test
    @DisplayName("應該檢查是否存在使用指定 SkillLevel 的 ProjectSkill")
    void testExistsBySkillLevelId() {
        // Arrange
        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Junior");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        ProjectSkill projectSkill = new ProjectSkill();
        projectSkill.setProject(project);
        projectSkill.setSkill(skill);
        projectSkill.setSkillLevel(skillLevel);
        projectSkillRepository.save(projectSkill);

        // Act & Assert
        assertTrue(projectSkillDataAccess.existsBySkillLevelId(skillLevel.getId()));
        assertFalse(projectSkillDataAccess.existsBySkillLevelId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("應該保存 ProjectSkill")
    void testSave() {
        // Arrange
        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Default");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        ProjectSkill projectSkill = new ProjectSkill();
        projectSkill.setProject(project);
        projectSkill.setSkill(skill);
        projectSkill.setSkillLevel(skillLevel);

        // Act
        ProjectSkill saved = projectSkillDataAccess.save(projectSkill);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(project.getId(), saved.getProject().getId());
        assertEquals(skill.getId(), saved.getSkill().getId());
    }

    @Test
    @DisplayName("應該根據 projectId 刪除所有 ProjectSkill")
    void testDeleteByProjectId() {
        // Arrange
        Project project = new Project();
        project.setName("測試專案");
        projectRepository.save(project);

        Skill skill1 = new Skill();
        skill1.setName("Java");
        skillRepository.save(skill1);

        Skill skill2 = new Skill();
        skill2.setName("Python");
        skillRepository.save(skill2);

        SkillLevel skillLevel1 = new SkillLevel();
        skillLevel1.setSkill(skill1);
        skillLevel1.setTitle("Default");
        skillLevel1.setLevelValue(1);
        skillLevelRepository.save(skillLevel1);

        SkillLevel skillLevel2 = new SkillLevel();
        skillLevel2.setSkill(skill2);
        skillLevel2.setTitle("Default");
        skillLevel2.setLevelValue(1);
        skillLevelRepository.save(skillLevel2);

        ProjectSkill projectSkill1 = new ProjectSkill();
        projectSkill1.setProject(project);
        projectSkill1.setSkill(skill1);
        projectSkill1.setSkillLevel(skillLevel1);
        projectSkillRepository.save(projectSkill1);

        ProjectSkill projectSkill2 = new ProjectSkill();
        projectSkill2.setProject(project);
        projectSkill2.setSkill(skill2);
        projectSkill2.setSkillLevel(skillLevel2);
        projectSkillRepository.save(projectSkill2);

        assertEquals(2, projectSkillRepository.count());

        // Act
        projectSkillDataAccess.deleteByProjectId(project.getId());

        // Assert
        assertEquals(0, projectSkillRepository.count());
    }

    @Test
    @DisplayName("應該根據 skillId 刪除所有 ProjectSkill")
    void testDeleteBySkillId() {
        // Arrange
        Project project1 = new Project();
        project1.setName("專案1");
        projectRepository.save(project1);

        Project project2 = new Project();
        project2.setName("專案2");
        projectRepository.save(project2);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Default");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        ProjectSkill projectSkill1 = new ProjectSkill();
        projectSkill1.setProject(project1);
        projectSkill1.setSkill(skill);
        projectSkill1.setSkillLevel(skillLevel);
        projectSkillRepository.save(projectSkill1);

        ProjectSkill projectSkill2 = new ProjectSkill();
        projectSkill2.setProject(project2);
        projectSkill2.setSkill(skill);
        projectSkill2.setSkillLevel(skillLevel);
        projectSkillRepository.save(projectSkill2);

        assertEquals(2, projectSkillRepository.count());

        // Act
        projectSkillDataAccess.deleteBySkillId(skill.getId());

        // Assert
        assertEquals(0, projectSkillRepository.count());
    }
}
