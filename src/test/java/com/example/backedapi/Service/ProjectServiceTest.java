package com.example.backedapi.Service;

import com.example.backedapi.Service.impl.ProjectService;
import com.example.backedapi.dataaccess.IProjectDataAccess;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.db.SkillMapUserAndProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProjectService 單元測試
 * 使用 Mockito 模擬 DataAccess 層,不依賴數據庫
 * 
 * 測試策略:
 * 1. 測試正常業務流程
 * 2. 測試異常情況處理
 * 3. 驗證與 DataAccess 層的交互
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectService 單元測試")
class ProjectServiceTest {
    
    @Mock
    private IProjectDataAccess projectDataAccess;
    
    @Mock
    private ISkillMapUserAndProjectDataAccess skillMapDataAccess;
    
    @InjectMocks
    private ProjectService projectService;
    
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setName("Test Project");
    }
    
    // ==================== addProject 測試 ====================
    
    @Test
    @DisplayName("addProject - 成功新增專案")
    void addProject_shouldSaveProject_whenValid() {
        // Given
        when(projectDataAccess.findByName("Test Project"))
            .thenReturn(Collections.emptyList());
        when(projectDataAccess.save(testProject))
            .thenReturn(testProject);
        
        // When
        Project result = projectService.addProject(testProject);
        
        // Then
        assertNotNull(result);
        verify(projectDataAccess, times(1)).findByName("Test Project");
        verify(projectDataAccess, times(1)).save(testProject);
    }
    
    @Test
    @DisplayName("addProject - Key 不為 null 時拋出例外")
    void addProject_shouldThrowException_whenKeyNotNull() {
        // Given
        testProject.setId(UUID.randomUUID());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.addProject(testProject)
        );
        
        assertEquals("Key must be null", exception.getMessage());
        verify(projectDataAccess, never()).save(any());
        verify(projectDataAccess, never()).findByName(any());
    }
    
    @Test
    @DisplayName("addProject - Name 為 null 時拋出例外")
    void addProject_shouldThrowException_whenNameIsNull() {
        // Given
        testProject.setName(null);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.addProject(testProject)
        );
        
        assertEquals("Name must not be null", exception.getMessage());
        verify(projectDataAccess, never()).save(any());
        verify(projectDataAccess, never()).findByName(any());
    }
    
    @Test
    @DisplayName("addProject - Name 已存在時拋出例外")
    void addProject_shouldThrowException_whenNameExists() {
        // Given
        Project existingProject = new Project();
        existingProject.setName("Test Project");
        when(projectDataAccess.findByName("Test Project"))
            .thenReturn(List.of(existingProject));
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.addProject(testProject)
        );
        
        assertEquals("Name already exists", exception.getMessage());
        verify(projectDataAccess, times(1)).findByName("Test Project");
        verify(projectDataAccess, never()).save(any());
    }
    
    // ==================== updateProject 測試 ====================
    
    @Test
    @DisplayName("updateProject - 成功更新專案")
    void updateProject_shouldUpdateProject_whenValid() {
        // Given
        UUID projectId = UUID.randomUUID();
        testProject.setId(projectId);
        
        // When
        projectService.updateProject(testProject);
        
        // Then
        verify(projectDataAccess, times(1)).save(testProject);
    }
    
    @Test
    @DisplayName("updateProject - Key 為 null 時拋出例外")
    void updateProject_shouldThrowException_whenKeyIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.updateProject(testProject)
        );
        
        assertEquals("Key must not be null", exception.getMessage());
        verify(projectDataAccess, never()).save(any());
    }
    
    @Test
    @DisplayName("updateProject - Name 為 null 時拋出例外")
    void updateProject_shouldThrowException_whenNameIsNull() {
        // Given
        testProject.setId(UUID.randomUUID());
        testProject.setName(null);
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.updateProject(testProject)
        );
        
        assertEquals("Name must not be null", exception.getMessage());
        verify(projectDataAccess, never()).save(any());
    }
    
    // ==================== getProject 測試 ====================
    
    @Test
    @DisplayName("getProject - 返回所有專案")
    void getProject_shouldReturnAllProjects() {
        // Given
        List<Project> projects = List.of(testProject);
        when(projectDataAccess.findAll()).thenReturn(projects);
        
        // When
        List<Project> result = projectService.getProject();
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProject, result.get(0));
        verify(projectDataAccess, times(1)).findAll();
    }
    
    @Test
    @DisplayName("getProject - 返回空列表當無專案時")
    void getProject_shouldReturnEmptyList_whenNoProjects() {
        // Given
        when(projectDataAccess.findAll()).thenReturn(Collections.emptyList());
        
        // When
        List<Project> result = projectService.getProject();
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectDataAccess, times(1)).findAll();
    }
    
    // ==================== deleteProject 測試 ====================
    
    @Test
    @DisplayName("deleteProject - 成功刪除專案及關聯")
    void deleteProject_shouldDeleteProjectAndMappings() {
        // Given
        UUID projectId = UUID.randomUUID();
        testProject.setId(projectId);
        
        when(projectDataAccess.findById(projectId))
            .thenReturn(Optional.of(testProject));
        
        SkillMapUserAndProject mapping = new SkillMapUserAndProject();
        when(skillMapDataAccess.findByProject(testProject))
            .thenReturn(List.of(mapping));
        
        // When
        projectService.deleteProject(testProject);
        
        // Then
        verify(projectDataAccess, times(1)).findById(projectId);
        verify(skillMapDataAccess, times(1)).findByProject(testProject);
        verify(skillMapDataAccess, times(1)).deleteAll(List.of(mapping));
        verify(projectDataAccess, times(1)).delete(testProject);
    }
    
    @Test
    @DisplayName("deleteProject - 無關聯時僅刪除專案")
    void deleteProject_shouldDeleteProject_whenNoMappings() {
        // Given
        UUID projectId = UUID.randomUUID();
        testProject.setId(projectId);
        
        when(projectDataAccess.findById(projectId))
            .thenReturn(Optional.of(testProject));
        when(skillMapDataAccess.findByProject(testProject))
            .thenReturn(Collections.emptyList());
        
        // When
        projectService.deleteProject(testProject);
        
        // Then
        verify(skillMapDataAccess, times(1)).deleteAll(Collections.emptyList());
        verify(projectDataAccess, times(1)).delete(testProject);
    }
    
    @Test
    @DisplayName("deleteProject - 專案不存在時拋出例外")
    void deleteProject_shouldThrowException_whenProjectNotFound() {
        // Given
        UUID projectId = UUID.randomUUID();
        testProject.setId(projectId);
        
        when(projectDataAccess.findById(projectId))
            .thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.deleteProject(testProject)
        );
        
        assertEquals("Project not found", exception.getMessage());
        verify(projectDataAccess, times(1)).findById(projectId);
        verify(skillMapDataAccess, never()).findByProject(any());
        verify(projectDataAccess, never()).delete(any());
    }
    
    @Test
    @DisplayName("deleteProject - Key 為 null 時拋出例外")
    void deleteProject_shouldThrowException_whenKeyIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> projectService.deleteProject(testProject)
        );
        
        assertEquals("Key must not be null", exception.getMessage());
        verify(projectDataAccess, never()).findById(any());
        verify(projectDataAccess, never()).delete(any());
    }
}
