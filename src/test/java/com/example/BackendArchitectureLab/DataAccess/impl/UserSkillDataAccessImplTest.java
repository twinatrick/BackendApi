package com.example.BackendArchitectureLab.DataAccess.impl;

import com.example.BackendArchitectureLab.Repository.UserSkillRepository;
import com.example.BackendArchitectureLab.Repository.UserRepository;
import com.example.BackendArchitectureLab.Repository.SkillRepository;
import com.example.BackendArchitectureLab.Repository.SkillLevelRepository;
import com.example.BackendArchitectureLab.DataAccess.IUserSkillDataAccess;
import com.example.BackendArchitectureLab.Entity.UserSkill;
import com.example.BackendArchitectureLab.Entity.User;
import com.example.BackendArchitectureLab.Entity.Skill;
import com.example.BackendArchitectureLab.Entity.SkillLevel;
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
 * Integration tests for UserSkillDataAccessImpl.
 * Uses in-memory H2 database for testing.
 */
@DataJpaTest
@ActiveProfiles("test")
class UserSkillDataAccessImplTest {

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private SkillLevelRepository skillLevelRepository;

    private IUserSkillDataAccess userSkillDataAccess;

    @BeforeEach
    void setUp() {
        userSkillDataAccess = new UserSkillDataAccessImpl(userSkillRepository);
        userSkillRepository.deleteAll();
        userRepository.deleteAll();
        skillRepository.deleteAll();
        skillLevelRepository.deleteAll();
    }

    @Test
    @DisplayName("應該檢查 UserSkill 是否存在（依 userId 和 skillId）")
    void testExistsByUserIdAndSkillId() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Default");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setSkillLevel(skillLevel);
        userSkillRepository.save(userSkill);

        // Act & Assert
        assertTrue(userSkillDataAccess.existsByUserIdAndSkillId(user.getId(), skill.getId()));
        assertFalse(userSkillDataAccess.existsByUserIdAndSkillId(UUID.randomUUID(), skill.getId()));
    }

    @Test
    @DisplayName("應該檢查是否存在使用指定 SkillLevel 的 UserSkill")
    void testExistsBySkillLevelId() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Junior");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setSkillLevel(skillLevel);
        userSkillRepository.save(userSkill);

        // Act & Assert
        assertTrue(userSkillDataAccess.existsBySkillLevelId(skillLevel.getId()));
        assertFalse(userSkillDataAccess.existsBySkillLevelId(UUID.randomUUID()));
    }

    @Test
    @DisplayName("應該保存 UserSkill")
    void testSave() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Default");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        UserSkill userSkill = new UserSkill();
        userSkill.setUser(user);
        userSkill.setSkill(skill);
        userSkill.setSkillLevel(skillLevel);

        // Act
        UserSkill saved = userSkillDataAccess.save(userSkill);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(user.getId(), saved.getUser().getId());
        assertEquals(skill.getId(), saved.getSkill().getId());
    }

    @Test
    @DisplayName("應該根據 skillId 刪除所有 UserSkill")
    void testDeleteBySkillId() {
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

        Skill skill = new Skill();
        skill.setName("Java");
        skillRepository.save(skill);

        SkillLevel skillLevel = new SkillLevel();
        skillLevel.setSkill(skill);
        skillLevel.setTitle("Default");
        skillLevel.setLevelValue(1);
        skillLevelRepository.save(skillLevel);

        UserSkill userSkill1 = new UserSkill();
        userSkill1.setUser(user1);
        userSkill1.setSkill(skill);
        userSkill1.setSkillLevel(skillLevel);
        userSkillRepository.save(userSkill1);

        UserSkill userSkill2 = new UserSkill();
        userSkill2.setUser(user2);
        userSkill2.setSkill(skill);
        userSkill2.setSkillLevel(skillLevel);
        userSkillRepository.save(userSkill2);

        assertEquals(2, userSkillRepository.count());

        // Act
        userSkillDataAccess.deleteBySkillId(skill.getId());

        // Assert
        assertEquals(0, userSkillRepository.count());
    }

    @Test
    @DisplayName("應該根據 userId 查詢所有 UserSkill")
    void testFindByUserId() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("password");
        user.setDisabled(false);
        userRepository.save(user);

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

        UserSkill userSkill1 = new UserSkill();
        userSkill1.setUser(user);
        userSkill1.setSkill(skill1);
        userSkill1.setSkillLevel(skillLevel1);
        userSkillRepository.save(userSkill1);

        UserSkill userSkill2 = new UserSkill();
        userSkill2.setUser(user);
        userSkill2.setSkill(skill2);
        userSkill2.setSkillLevel(skillLevel2);
        userSkillRepository.save(userSkill2);

        // Act
        List<UserSkill> result = userSkillDataAccess.findByUserId(user.getId());

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(us -> us.getSkill().getName().equals("Java")));
        assertTrue(result.stream().anyMatch(us -> us.getSkill().getName().equals("Python")));
    }

    @Test
    @DisplayName("當 userId 不存在時應該返回空列表")
    void testFindByUserId_NotFound() {
        // Act
        List<UserSkill> result = userSkillDataAccess.findByUserId(UUID.randomUUID());

        // Assert
        assertTrue(result.isEmpty());
    }
}
