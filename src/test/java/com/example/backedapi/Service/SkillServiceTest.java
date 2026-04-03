package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IProjectDataAccess;
import com.example.backedapi.dataaccess.ISkillDataAccess;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.db.Skill;
import com.example.backedapi.model.db.SkillMapUserAndProject;
import com.example.backedapi.model.db.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SkillService.
 * Uses Mockito to mock DataAccess dependencies.
 */
@ExtendWith(MockitoExtension.class)
class SkillServiceTest {

    @Mock
    private ISkillDataAccess skillDataAccess;

    @Mock
    private IUserDataAccess userDataAccess;

    @Mock
    private IProjectDataAccess projectDataAccess;

    @Mock
    private ISkillMapUserAndProjectDataAccess skillMapUserAndProjectDataAccess;

    @InjectMocks
    private SkillService skillService;

    private Skill testSkill;
    private User testUser;
    private Project testProject;
    private UUID testSkillId;
    private UUID testUserId;
    private UUID testProjectId;

    @BeforeEach
    void setUp() {
        testSkillId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testProjectId = UUID.randomUUID();

        testSkill = new Skill();
        testSkill.setKey(testSkillId);
        testSkill.setName("Java");

        testUser = new User();
        testUser.setKey(testUserId);
        testUser.setName("Test User");

        testProject = new Project();
        testProject.setKey(testProjectId);
        testProject.setName("Test Project");
    }

    @Test
    void testAddSkill_Success() {
        Skill newSkill = new Skill();
        newSkill.setName("Python");

        when(skillDataAccess.exists(any(Example.class))).thenReturn(false);
        when(skillDataAccess.save(newSkill)).thenReturn(newSkill);

        Skill result = skillService.addSkill(newSkill);

        assertNotNull(result);
        assertEquals("Python", result.getName());
        verify(skillDataAccess).exists(any(Example.class));
        verify(skillDataAccess).save(newSkill);
    }

    @Test
    void testAddSkill_KeyNotNull_ThrowsException() {
        Skill skillWithKey = new Skill();
        skillWithKey.setKey(testSkillId);
        skillWithKey.setName("Java");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.addSkill(skillWithKey);
        });

        assertEquals("Key must be null", exception.getMessage());
        verify(skillDataAccess, never()).save(any());
    }

    @Test
    void testAddSkill_NameNull_ThrowsException() {
        Skill skillWithoutName = new Skill();
        skillWithoutName.setName(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.addSkill(skillWithoutName);
        });

        assertEquals("Name must not be null", exception.getMessage());
        verify(skillDataAccess, never()).save(any());
    }

    @Test
    void testAddSkill_NameAlreadyExists_ThrowsException() {
        Skill newSkill = new Skill();
        newSkill.setName("Existing Skill");

        when(skillDataAccess.exists(any(Example.class))).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.addSkill(newSkill);
        });

        assertEquals("Name already exists", exception.getMessage());
        verify(skillDataAccess, never()).save(any());
    }

    @Test
    void testUpdateSkill_Success() {
        when(skillDataAccess.save(testSkill)).thenReturn(testSkill);

        skillService.updateSkill(testSkill);

        verify(skillDataAccess).save(testSkill);
    }

    @Test
    void testUpdateSkill_KeyNull_ThrowsException() {
        Skill skillWithoutKey = new Skill();
        skillWithoutKey.setName("Java");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.updateSkill(skillWithoutKey);
        });

        assertEquals("Key must not be null", exception.getMessage());
        verify(skillDataAccess, never()).save(any());
    }

    @Test
    void testUpdateSkill_NameNull_ThrowsException() {
        Skill skillWithoutName = new Skill();
        skillWithoutName.setKey(testSkillId);
        skillWithoutName.setName(null);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.updateSkill(skillWithoutName);
        });

        assertEquals("Name must not be null", exception.getMessage());
        verify(skillDataAccess, never()).save(any());
    }

    @Test
    void testGetSkill() {
        List<Skill> skills = List.of(testSkill, new Skill());
        when(skillDataAccess.findAll()).thenReturn(skills);

        List<Skill> result = skillService.getSkill();

        assertEquals(2, result.size());
        verify(skillDataAccess).findAll();
    }

    @Test
    void testBindSkillToUser_Success() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(userDataAccess.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(skillMapUserAndProjectDataAccess.findOne(any(Example.class))).thenReturn(Optional.empty());
        when(skillMapUserAndProjectDataAccess.save(any(SkillMapUserAndProject.class)))
                .thenReturn(new SkillMapUserAndProject());

        skillService.BindSkillToUser(testSkillId.toString(), testUserId.toString());

        verify(skillDataAccess).findById(testSkillId);
        verify(userDataAccess).findById(testUserId);
        verify(skillMapUserAndProjectDataAccess).findOne(any(Example.class));
        verify(skillMapUserAndProjectDataAccess).save(any(SkillMapUserAndProject.class));
    }

    @Test
    void testBindSkillToUser_SkillNotFound_ThrowsException() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToUser(testSkillId.toString(), testUserId.toString());
        });

        assertEquals("Skill not found", exception.getMessage());
        verify(userDataAccess, never()).findById(any());
    }

    @Test
    void testBindSkillToUser_UserNotFound_ThrowsException() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(userDataAccess.findById(testUserId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToUser(testSkillId.toString(), testUserId.toString());
        });

        assertEquals("User not found", exception.getMessage());
        verify(skillMapUserAndProjectDataAccess, never()).save(any());
    }

    @Test
    void testBindSkillToUser_AlreadyBound_ThrowsException() {
        SkillMapUserAndProject existing = new SkillMapUserAndProject();
        existing.setSkill(testSkill);
        existing.setUser(testUser);

        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(userDataAccess.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(skillMapUserAndProjectDataAccess.findOne(any(Example.class))).thenReturn(Optional.of(existing));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToUser(testSkillId.toString(), testUserId.toString());
        });

        assertEquals("Skill already bind to user", exception.getMessage());
        verify(skillMapUserAndProjectDataAccess, never()).save(any());
    }

    @Test
    void testBindSkillToProjectAndUser_Success() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(projectDataAccess.findById(testProjectId)).thenReturn(Optional.of(testProject));
        when(userDataAccess.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(skillMapUserAndProjectDataAccess.findOne(any(Example.class))).thenReturn(Optional.empty());
        when(skillMapUserAndProjectDataAccess.save(any(SkillMapUserAndProject.class)))
                .thenReturn(new SkillMapUserAndProject());

        skillService.BindSkillToProjectAndUser(
                testSkillId.toString(),
                testProjectId.toString(),
                testUserId.toString()
        );

        verify(skillDataAccess).findById(testSkillId);
        verify(projectDataAccess).findById(testProjectId);
        verify(userDataAccess).findById(testUserId);
        verify(skillMapUserAndProjectDataAccess).save(any(SkillMapUserAndProject.class));
    }

    @Test
    void testBindSkillToProjectAndUser_SkillNotFound_ThrowsException() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToProjectAndUser(
                    testSkillId.toString(),
                    testProjectId.toString(),
                    testUserId.toString()
            );
        });

        assertEquals("Skill not found", exception.getMessage());
    }

    @Test
    void testBindSkillToProjectAndUser_ProjectNotFound_ThrowsException() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(projectDataAccess.findById(testProjectId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToProjectAndUser(
                    testSkillId.toString(),
                    testProjectId.toString(),
                    testUserId.toString()
            );
        });

        assertEquals("Project not found", exception.getMessage());
    }

    @Test
    void testBindSkillToProjectAndUser_UserNotFound_ThrowsException() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(projectDataAccess.findById(testProjectId)).thenReturn(Optional.of(testProject));
        when(userDataAccess.findById(testUserId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToProjectAndUser(
                    testSkillId.toString(),
                    testProjectId.toString(),
                    testUserId.toString()
            );
        });

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testBindSkillToProjectAndUser_AlreadyBound_ThrowsException() {
        SkillMapUserAndProject existing = new SkillMapUserAndProject();

        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(projectDataAccess.findById(testProjectId)).thenReturn(Optional.of(testProject));
        when(userDataAccess.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(skillMapUserAndProjectDataAccess.findOne(any(Example.class))).thenReturn(Optional.of(existing));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.BindSkillToProjectAndUser(
                    testSkillId.toString(),
                    testProjectId.toString(),
                    testUserId.toString()
            );
        });

        assertEquals("Skill already bind to project", exception.getMessage());
        verify(skillMapUserAndProjectDataAccess, never()).save(any());
    }

    @Test
    void testDeleteSkill_Success() {
        List<SkillMapUserAndProject> mappings = List.of(new SkillMapUserAndProject());

        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(skillMapUserAndProjectDataAccess.findAll(any(Example.class))).thenReturn(mappings);
        doNothing().when(skillMapUserAndProjectDataAccess).deleteAll(mappings);
        doNothing().when(skillDataAccess).delete(testSkill);

        skillService.deleteSkill(testSkill);

        verify(skillDataAccess).findById(testSkillId);
        verify(skillMapUserAndProjectDataAccess).findAll(any(Example.class));
        verify(skillMapUserAndProjectDataAccess).deleteAll(mappings);
        verify(skillDataAccess).delete(testSkill);
    }

    @Test
    void testDeleteSkill_KeyNull_ThrowsException() {
        Skill skillWithoutKey = new Skill();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.deleteSkill(skillWithoutKey);
        });

        assertEquals("Key must not be null", exception.getMessage());
        verify(skillDataAccess, never()).delete(any());
    }

    @Test
    void testDeleteSkill_SkillNotFound_ThrowsException() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            skillService.deleteSkill(testSkill);
        });

        assertEquals("Skill not found", exception.getMessage());
        verify(skillDataAccess, never()).delete(any());
    }

    @Test
    void testDeleteSkill_WithNoMappings_Success() {
        when(skillDataAccess.findById(testSkillId)).thenReturn(Optional.of(testSkill));
        when(skillMapUserAndProjectDataAccess.findAll(any(Example.class))).thenReturn(Collections.emptyList());
        doNothing().when(skillMapUserAndProjectDataAccess).deleteAll(Collections.emptyList());
        doNothing().when(skillDataAccess).delete(testSkill);

        skillService.deleteSkill(testSkill);

        verify(skillMapUserAndProjectDataAccess).deleteAll(Collections.emptyList());
        verify(skillDataAccess).delete(testSkill);
    }
}
