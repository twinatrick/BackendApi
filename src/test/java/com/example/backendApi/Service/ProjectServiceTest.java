package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.ProjectSearchQuery;
import com.example.backendApi.Dto.Vo.ProjectVo;
import com.example.backendApi.Entity.Project;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserProject;
import com.example.backendApi.Service.impl.ProjectService;
import com.example.backendApi.dataaccess.IProjectDataAccess;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.dataaccess.IUserProjectDataAccess;
import com.example.backendApi.exception.AppException;
import com.example.backendApi.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private IProjectDataAccess projectDataAccess;
    @Mock
    private IProjectSkillDataAccess projectSkillDataAccess;
    @Mock
    private IUserProjectDataAccess userProjectDataAccess;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    @Mock
    private User currentUser;

    private Project testProject;
    private ProjectVo testProjectVo;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testProject = new Project();
        testProject.setId(testId);
        testProject.setName("Test Project");
        testProject.setDescription("Test Description");

        testProjectVo = new ProjectVo();
        testProjectVo.setId(testId);
        testProjectVo.setName("Test Project");
        testProjectVo.setDescription("Test Description");
    }

    @Test
    void addProject_shouldThrow_whenNameExists() {
        ProjectVo vo = new ProjectVo();
        vo.setName("Demo");

        Project entity = new Project();
        entity.setName("Demo");

        when(projectMapper.toEntity(vo)).thenReturn(entity);
        when(projectDataAccess.findByName("Demo")).thenReturn(List.of(new Project()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> projectService.addProject(vo));
        assertEquals("Name already exists", exception.getMessage());
    }

    @Test
    void deleteProject_shouldDeleteMappingsAndProject() {
        UUID projectId = UUID.randomUUID();

        ProjectVo vo = new ProjectVo();
        vo.setId(projectId);
        Project mapped = new Project();
        mapped.setId(projectId);
        Project existing = new Project();
        existing.setId(projectId);

        when(projectMapper.toEntity(vo)).thenReturn(mapped);
        when(projectDataAccess.findById(projectId)).thenReturn(Optional.of(existing));

        projectService.deleteProject(vo);

        verify(projectSkillDataAccess).deleteByProjectId(projectId);
        verify(userProjectDataAccess).deleteByProjectId(projectId);
        verify(projectDataAccess).delete(existing);
    }

    @Test
    void updateProject_shouldSave_whenValid() {
        UUID projectId = UUID.randomUUID();

        ProjectVo vo = new ProjectVo();
        vo.setId(projectId);
        vo.setName("Updated");

        Project mapped = new Project();
        mapped.setId(projectId);
        mapped.setName("Updated");

        when(projectMapper.toEntity(vo)).thenReturn(mapped);

        projectService.updateProject(vo);

        verify(projectDataAccess).save(any(Project.class));
    }

    @Test
    void testSearchProjects_Success() {
        // Arrange
        ProjectSearchQuery query = new ProjectSearchQuery();
        query.setPage(0);
        query.setSize(20);
        query.setSortBy("createdTime");
        query.setSortDir("desc");
        query.setName("Test");

        List<Project> projects = List.of(testProject);
        Page<Project> projectPage = new PageImpl<>(projects, PageRequest.of(0, 20), 1);

        when(projectDataAccess.searchProjects(any(ProjectSearchQuery.class))).thenReturn(projectPage);
        when(projectMapper.toVo(testProject)).thenReturn(testProjectVo);

        // Act
        PageResult<ProjectVo> result = projectService.searchProjects(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("Test Project", result.getContent().get(0).getName());
        verify(projectDataAccess).searchProjects(any(ProjectSearchQuery.class));
    }

    @Test
    void testSearchProjects_InvalidSortField() {
        // Arrange
        ProjectSearchQuery query = new ProjectSearchQuery();
        query.setPage(0);
        query.setSize(20);
        query.setSortBy("invalidField");
        query.setSortDir("desc");

        // Act & Assert
        assertThrows(AppException.class, () -> projectService.searchProjects(query));
    }

    @Test
    void testSearchProjects_InvalidSortDirection() {
        // Arrange
        ProjectSearchQuery query = new ProjectSearchQuery();
        query.setPage(0);
        query.setSize(20);
        query.setSortBy("createdTime");
        query.setSortDir("invalid");

        // Act & Assert
        assertThrows(AppException.class, () -> projectService.searchProjects(query));
    }

    @Test
    void testGetCurrentUserProjects_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);

        UserProject userProject = new UserProject();
        userProject.setProject(testProject);

        when(userProjectDataAccess.findByUserId(userId)).thenReturn(List.of(userProject));
        when(projectMapper.toVo(testProject)).thenReturn(testProjectVo);

        // Act
        List<ProjectVo> result = projectService.getCurrentUserProjects();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Project", result.get(0).getName());
        verify(userProjectDataAccess).findByUserId(userId);
    }

    @Test
    void testSearchCurrentUserProjects_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);

        ProjectSearchQuery query = new ProjectSearchQuery();
        query.setPage(0);
        query.setSize(20);
        query.setSortBy("createdTime");
        query.setSortDir("desc");
        query.setName("Test");

        List<Project> projects = List.of(testProject);
        Page<Project> projectPage = new PageImpl<>(projects, PageRequest.of(0, 20), 1);

        when(projectDataAccess.searchCurrentUserProjects(any(String.class), any(ProjectSearchQuery.class)))
                .thenReturn(projectPage);
        when(projectMapper.toVo(testProject)).thenReturn(testProjectVo);

        // Act
        PageResult<ProjectVo> result = projectService.searchCurrentUserProjects(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        assertEquals("Test Project", result.getContent().get(0).getName());
        verify(projectDataAccess).searchCurrentUserProjects(any(String.class), any(ProjectSearchQuery.class));
    }

    @Test
    void testSearchCurrentUserProjects_EmptyResult() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);

        ProjectSearchQuery query = new ProjectSearchQuery();
        query.setPage(0);
        query.setSize(20);
        query.setSortBy("createdTime");
        query.setSortDir("desc");

        Page<Project> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);

        when(projectDataAccess.searchCurrentUserProjects(any(String.class), any(ProjectSearchQuery.class)))
                .thenReturn(emptyPage);

        // Act
        PageResult<ProjectVo> result = projectService.searchCurrentUserProjects(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getContent().size());
        assertEquals(0L, result.getTotalElements());
    }
}
