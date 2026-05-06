package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.ProjectSearchQuery;
import com.example.backendApi.Dto.Vo.PersonalProjectRequest;
import com.example.backendApi.Dto.Vo.ProjectVo;
import com.example.backendApi.Entity.Project;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserProject;
import com.example.backendApi.Service.impl.ProjectService;
import com.example.backendApi.dataaccess.IProjectDataAccess;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.dataaccess.IUserDataAccess;
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
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private IProjectDataAccess projectDataAccess;
    @Mock
    private IProjectSkillDataAccess projectSkillDataAccess;
    @Mock
    private IUserProjectDataAccess userProjectDataAccess;
    @Mock
    private IUserDataAccess userDataAccess;
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
    
    // Personal Project Tests
    
    @Test
    void addPersonalProject_should_whenValid() {
        // Arrange
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("Personal Project");
        request.setDescription("Personal Description");
        
        when(projectDataAccess.findByName("Personal Project")).thenReturn(Collections.emptyList());
        when(projectDataAccess.save(any(Project.class))).thenReturn(testProject);
        when(projectMapper.toVo(testProject)).thenReturn(testProjectVo);
        
        // Act
        ProjectVo result = projectService.addPersonalProject(request);
        
        // Assert
        assertNotNull(result);
        verify(projectDataAccess).save(any(Project.class));
        verify(userProjectDataAccess).save(any(UserProject.class));
    }
    
    @Test
    void addPersonalProject_shouldThrow_whenNameIsNull() {
        // Arrange
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName(null);
        request.setDescription("Description");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.addPersonalProject(request));
        assertEquals("Name must not be null", exception.getMessage());
    }
    
    @Test
    void addPersonalProject_shouldThrow_whenNameIsEmpty() {
        // Arrange
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("   ");
        request.setDescription("Description");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.addPersonalProject(request));
        assertEquals("Name must not be null", exception.getMessage());
    }
    
    @Test
    void addPersonalProject_shouldThrow_whenNameExists() {
        // Arrange
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("Existing Project");
        request.setDescription("Description");
        
        when(projectDataAccess.findByName("Existing Project")).thenReturn(List.of(new Project()));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.addPersonalProject(request));
        assertEquals("Name already exists", exception.getMessage());
    }
    
    @Test
    void updatePersonalProject_should_whenValid() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);
        
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("Updated Project");
        request.setDescription("Updated Description");
        
        when(projectDataAccess.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);
        
        // Act
        projectService.updatePersonalProject(projectId, request);
        
        // Assert
        verify(projectDataAccess).save(testProject);
        assertEquals("Updated Project", testProject.getName());
        assertEquals("Updated Description", testProject.getDescription());
    }
    
    @Test
    void updatePersonalProject_shouldThrow_whenProjectIdIsNull() {
        // Arrange
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("Updated Project");
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.updatePersonalProject(null, request));
        assertEquals("Project ID must not be null", exception.getMessage());
    }
    
    @Test
    void updatePersonalProject_shouldThrow_whenProjectNotFound() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("Updated Project");
        
        when(projectDataAccess.findById(projectId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.updatePersonalProject(projectId, request));
        assertEquals("Project not found", exception.getMessage());
    }
    
    @Test
    void updatePersonalProject_shouldThrow_whenNotOwner() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);
        
        PersonalProjectRequest request = new PersonalProjectRequest();
        request.setName("Updated Project");
        
        when(projectDataAccess.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId, projectId)).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectService.updatePersonalProject(projectId, request));
        assertEquals("You are not the owner of this project", exception.getMessage());
    }
    
    @Test
    void deletePersonalProject_should_whenValidAndHasOtherBindings() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);
        
        when(projectDataAccess.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);
        when(userProjectDataAccess.existsByProjectId(projectId)).thenReturn(true);
        
        // Act
        projectService.deletePersonalProject(projectId);
        
        // Assert
        verify(userProjectDataAccess).deleteByUserIdAndProjectId(userId, projectId);
        verify(projectDataAccess, never()).delete(any(Project.class));
        verify(projectSkillDataAccess, never()).deleteByProjectId(projectId);
    }
    
    @Test
    void deletePersonalProject_should_whenValidAndNoOtherBindings() {
        // Arrange
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(currentUser.getId()).thenReturn(userId);
        
        when(projectDataAccess.findById(projectId)).thenReturn(Optional.of(testProject));
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId, projectId)).thenReturn(true);
        when(userProjectDataAccess.existsByProjectId(projectId)).thenReturn(false);
        
        // Act
        projectService.deletePersonalProject(projectId);
        
        // Assert
        verify(userProjectDataAccess).deleteByUserIdAndProjectId(userId, projectId);
        verify(projectSkillDataAccess).deleteByProjectId(projectId);
        verify(projectDataAccess).delete(testProject);
    }
    
    // ========== 管理者介面測試 ==========
    
    @Test
    void addProject_shouldBindUsers_whenUserIdsProvided() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user1 = new User();
        user1.setId(userId1);
        User user2 = new User();
        user2.setId(userId2);
        
        Project newProject = new Project();
        newProject.setName("Java Project");
        newProject.setDescription("Java Project Description");
        
        ProjectVo projectVo = new ProjectVo();
        projectVo.setName("Java Project");
        projectVo.setDescription("Java Project Description");
        projectVo.setUserIds(List.of(userId1.toString(), userId2.toString()));
        
        when(projectMapper.toEntity(projectVo)).thenReturn(newProject);
        when(projectDataAccess.findByName("Java Project")).thenReturn(Collections.emptyList());
        when(projectDataAccess.save(newProject)).thenReturn(testProject);
        when(projectDataAccess.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(userDataAccess.findById(userId1)).thenReturn(Optional.of(user1));
        when(userDataAccess.findById(userId2)).thenReturn(Optional.of(user2));
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId1, testProject.getId())).thenReturn(false);
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId2, testProject.getId())).thenReturn(false);
        when(projectMapper.toVo(testProject)).thenReturn(projectVo);
        
        // Act
        ProjectVo result = projectService.addProject(projectVo);
        
        // Assert
        assertNotNull(result);
        verify(userProjectDataAccess, times(2)).save(any(UserProject.class));
    }
    
    @Test
    void addProject_shouldThrow_whenInvalidUserId() {
        // Arrange
        UUID invalidUserId = UUID.randomUUID();
        
        Project newProject = new Project();
        newProject.setName("Java Project");
        newProject.setDescription("Java Project Description");
        
        ProjectVo projectVo = new ProjectVo();
        projectVo.setName("Java Project");
        projectVo.setDescription("Java Project Description");
        projectVo.setUserIds(List.of(invalidUserId.toString()));
        
        when(projectMapper.toEntity(projectVo)).thenReturn(newProject);
        when(projectDataAccess.findByName("Java Project")).thenReturn(Collections.emptyList());
        when(projectDataAccess.save(newProject)).thenReturn(testProject);
        when(projectDataAccess.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(userDataAccess.findById(invalidUserId)).thenReturn(Optional.empty());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                projectService.addProject(projectVo)
        );
        assertTrue(exception.getMessage().contains("User not found"));
    }
    
    @Test
    void addProject_shouldNotBindUsers_whenUserIdsNull() {
        // Arrange
        Project newProject = new Project();
        newProject.setName("Java Project");
        newProject.setDescription("Java Project Description");
        
        ProjectVo projectVo = new ProjectVo();
        projectVo.setName("Java Project");
        projectVo.setDescription("Java Project Description");
        projectVo.setUserIds(null);
        
        when(projectMapper.toEntity(projectVo)).thenReturn(newProject);
        when(projectDataAccess.findByName("Java Project")).thenReturn(Collections.emptyList());
        when(projectDataAccess.save(newProject)).thenReturn(testProject);
        when(projectMapper.toVo(testProject)).thenReturn(projectVo);
        
        // Act
        ProjectVo result = projectService.addProject(projectVo);
        
        // Assert
        assertNotNull(result);
        verify(userProjectDataAccess, never()).save(any(UserProject.class));
    }
    
    @Test
    void addProject_shouldNotBindUsers_whenUserIdsEmpty() {
        // Arrange
        Project newProject = new Project();
        newProject.setName("Java Project");
        newProject.setDescription("Java Project Description");
        
        ProjectVo projectVo = new ProjectVo();
        projectVo.setName("Java Project");
        projectVo.setDescription("Java Project Description");
        projectVo.setUserIds(List.of());
        
        when(projectMapper.toEntity(projectVo)).thenReturn(newProject);
        when(projectDataAccess.findByName("Java Project")).thenReturn(Collections.emptyList());
        when(projectDataAccess.save(newProject)).thenReturn(testProject);
        when(projectMapper.toVo(testProject)).thenReturn(projectVo);
        
        // Act
        ProjectVo result = projectService.addProject(projectVo);
        
        // Assert
        assertNotNull(result);
        verify(userProjectDataAccess, never()).save(any(UserProject.class));
    }
    
    @Test
    void updateProject_shouldRebindUsers_whenUserIdsProvided() {
        // Arrange
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        User user1 = new User();
        user1.setId(userId1);
        User user2 = new User();
        user2.setId(userId2);
        
        ProjectVo projectVo = new ProjectVo();
        projectVo.setId(testId);
        projectVo.setName("Updated Project");
        projectVo.setDescription("Updated Description");
        projectVo.setUserIds(List.of(userId2.toString()));
        
        when(projectMapper.toEntity(projectVo)).thenReturn(testProject);
        when(projectDataAccess.findById(testId)).thenReturn(Optional.of(testProject));
        when(userDataAccess.findById(userId2)).thenReturn(Optional.of(user2));
        when(userProjectDataAccess.existsByUserIdAndProjectId(userId2, testId)).thenReturn(false);
        
        // Act
        projectService.updateProject(projectVo);
        
        // Assert
        verify(userProjectDataAccess).deleteByProjectId(testId);
        verify(userProjectDataAccess).save(any(UserProject.class));
    }
    
    @Test
    void updateProject_shouldRemoveAllBindings_whenEmptyUserIds() {
        // Arrange
        ProjectVo projectVo = new ProjectVo();
        projectVo.setId(testId);
        projectVo.setName("Updated Project");
        projectVo.setDescription("Updated Description");
        projectVo.setUserIds(List.of());
        
        when(projectMapper.toEntity(projectVo)).thenReturn(testProject);
        
        // Act
        projectService.updateProject(projectVo);
        
        // Assert
        verify(userProjectDataAccess).deleteByProjectId(testId);
        verify(userProjectDataAccess, never()).save(any(UserProject.class));
    }
    
    @Test
    void updateProject_shouldNotRebind_whenUserIdsNull() {
        // Arrange
        ProjectVo projectVo = new ProjectVo();
        projectVo.setId(testId);
        projectVo.setName("Updated Project");
        projectVo.setDescription("Updated Description");
        projectVo.setUserIds(null);
        
        when(projectMapper.toEntity(projectVo)).thenReturn(testProject);
        
        // Act
        projectService.updateProject(projectVo);
        
        // Assert
        verify(userProjectDataAccess, never()).deleteByProjectId(any());
        verify(userProjectDataAccess, never()).save(any(UserProject.class));
    }
}
