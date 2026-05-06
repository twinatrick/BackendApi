package com.example.backendApi.Service.impl;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.ProjectSearchQuery;
import com.example.backendApi.Dto.Vo.PersonalProjectRequest;
import com.example.backendApi.Entity.ProjectSkill;
import com.example.backendApi.Entity.Skill;
import com.example.backendApi.Entity.SkillLevel;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserProject;
import com.example.backendApi.Service.IProjectService;
import com.example.backendApi.Util.SortFieldValidator;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.dataaccess.IProjectDataAccess;
import com.example.backendApi.dataaccess.ISkillLevelDataAccess;
import com.example.backendApi.dataaccess.IUserDataAccess;
import com.example.backendApi.dataaccess.IUserProjectDataAccess;
import com.example.backendApi.dataaccess.IUserSkillDataAccess;
import com.example.backendApi.mapper.ProjectMapper;
import com.example.backendApi.Dto.Vo.ProjectVo;
import com.example.backendApi.Entity.Project;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * ProjectService - 專案業務邏輯服務
 * 重構後依賴 DataAccess 抽象層,而非直接依賴 Repository
 */
@Service
@RequiredArgsConstructor
public class ProjectService implements IProjectService {
    
    // 依賴注入:通過構造函數注入介面(由 Lombok @RequiredArgsConstructor 自動生成)
    private final IProjectDataAccess projectDataAccess;
    private final IProjectSkillDataAccess projectSkillDataAccess;
    private final IUserProjectDataAccess userProjectDataAccess;
    private final IUserDataAccess userDataAccess;
    private final IUserSkillDataAccess userSkillDataAccess;
    private final ISkillLevelDataAccess skillLevelDataAccess;
    private final ProjectMapper projectMapper;
    private final User currentUser;
    private final EntityManager entityManager;
    /**
     * 新增專案
     * @param project 要新增的專案實體
     * @return 保存後的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗時拋出
     */
    @Transactional
    @Override
    public ProjectVo addProject(ProjectVo projectVo) {
        Project project = projectMapper.toEntity(projectVo);
        if (project.getId() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (project.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        } else if (!projectDataAccess.findByName(project.getName()).isEmpty()) {
            throw new IllegalArgumentException("Name already exists");
        }

        Project savedProject = projectDataAccess.save(project);
        
        // 處理使用者綁定（如果提供了 userIds）
        if (projectVo.getUserIds() != null && !projectVo.getUserIds().isEmpty()) {
            bindUsersToProject(savedProject.getId(), projectVo.getUserIds());
        }
        
        return projectMapper.toVo(savedProject);
    }
    /**
     * 更新專案
     * @param project 要更新的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗時拋出
     */
    @Transactional
    @Override
    public void updateProject(ProjectVo projectVo) {
        Project project = projectMapper.toEntity(projectVo);
        if (project.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (project.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        projectDataAccess.save(project);
        
        // 處理使用者重新綁定（如果提供了 userIds）
        if (projectVo.getUserIds() != null) {
            // 先刪除現有綁定
            userProjectDataAccess.deleteByProjectId(project.getId());
            
            // 重新綁定（如果 userIds 不為空）
            if (!projectVo.getUserIds().isEmpty()) {
                bindUsersToProject(project.getId(), projectVo.getUserIds());
            }
        }
    }
    
    /**
     * 綁定多個使用者到專案
     * 
     * @param projectId 專案 ID
     * @param userIds 使用者 ID 列表
     */
    private void bindUsersToProject(UUID projectId, List<String> userIds) {
        Project project = projectDataAccess.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        // 綁定每個使用者
        for (String userIdStr : userIds) {
            UUID userId = UUID.fromString(userIdStr);
            
            // 驗證使用者是否存在
            if (!userDataAccess.existsById(userId)) {
                throw new IllegalArgumentException("User not found: " + userIdStr);
            }
            
            // 檢查是否已存在綁定
            if (!userProjectDataAccess.existsByUserIdAndProjectId(userId, projectId)) {
                // 使用 EntityManager.getReference 避免 CGLIB 代理問題
                User user = entityManager.getReference(User.class, userId);
                
                UserProject userProject = new UserProject();
                userProject.setUser(user);
                userProject.setProject(project);
                userProjectDataAccess.save(userProject);
            }
        }
    }
    /**
     * 查詢所有專案
     * @return 所有專案列表
     */
    @Override
    public List<ProjectVo> getProject() {
        return projectDataAccess.findAll().stream().map(projectMapper::toVo).toList();
    }
    /**
     * 刪除專案及其關聯的技能映射
     * @param project 要刪除的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗或專案不存在時拋出
     */
    @Transactional
    @Override
    public void deleteProject(ProjectVo projectVo) {
        Project project = projectMapper.toEntity(projectVo);
        if (project.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        
        Project existingProject = projectDataAccess.findById(project.getId())
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        projectSkillDataAccess.deleteByProjectId(existingProject.getId());
        userProjectDataAccess.deleteByProjectId(existingProject.getId());
        projectDataAccess.delete(existingProject);
    }
    
    @Override
    public PageResult<ProjectVo> searchProjects(ProjectSearchQuery query) {
        // 定義允許的排序欄位
        String[] allowedSortFields = {
            "id", "name", "description",
            "createdBy", "updatedBy", "createdTime", "updatedTime"
        };
        
        // 驗證排序欄位
        SortFieldValidator.validateSortField(query.getSortBy(), allowedSortFields);
        
        // 驗證排序方向
        SortFieldValidator.validateSortDirection(query.getSortDir());
        
        // 執行分頁查詢
        Page<Project> projectPage = projectDataAccess.searchProjects(query);
        
        // 轉換為 VO
        List<ProjectVo> projectVos = projectPage.getContent().stream()
                .map(projectMapper::toVo)
                .toList();
        
        // 返回分頁結果
        return PageResult.of(projectPage, projectVos);
    }
    
    @Override
    public List<ProjectVo> getCurrentUserProjects() {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("未找到當前登入的使用者");
        }
        
        // 透過 UserProject 關聯取得當前使用者的專案
        List<UserProject> userProjects = userProjectDataAccess.findByUserId(currentUser.getId());
        return userProjects.stream()
                .map(UserProject::getProject)
                .map(projectMapper::toVo)
                .toList();
    }
    
    @Override
    public PageResult<ProjectVo> searchCurrentUserProjects(ProjectSearchQuery query) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("未找到當前登入的使用者");
        }
        
        // 定義允許的排序欄位
        String[] allowedSortFields = {
            "id", "name", "description",
            "createdBy", "updatedBy", "createdTime", "updatedTime"
        };
        
        // 驗證排序欄位
        SortFieldValidator.validateSortField(query.getSortBy(), allowedSortFields);
        
        // 驗證排序方向
        SortFieldValidator.validateSortDirection(query.getSortDir());
        
        // 執行分頁查詢（只查詢當前使用者的專案）
        Page<Project> projectPage = projectDataAccess.searchCurrentUserProjects(
            currentUser.getId().toString(), 
            query
        );
        
        // 轉換為 VO
        List<ProjectVo> projectVos = projectPage.getContent().stream()
                .map(projectMapper::toVo)
                .toList();
        
        // 返回分頁結果
        return PageResult.of(projectPage, projectVos);
    }
    
    @Transactional
    @Override
    public ProjectVo addPersonalProject(PersonalProjectRequest request) {
        // 驗證輸入
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null");
        }
        
        // 檢查名稱是否已存在
        if (!projectDataAccess.findByName(request.getName()).isEmpty()) {
            throw new IllegalArgumentException("Name already exists");
        }
        
        // 建立專案
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        Project savedProject = projectDataAccess.save(project);
        
        // 自動綁定當前使用者
        UserProject userProject = new UserProject();
        userProject.setUser(currentUser);
        userProject.setProject(savedProject);
        userProjectDataAccess.save(userProject);
        
        return projectMapper.toVo(savedProject);
    }
    
    @Transactional
    @Override
    public void updatePersonalProject(UUID projectId, PersonalProjectRequest request) {
        // 驗證輸入
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID must not be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name must not be null");
        }
        
        // 查找專案
        Project project = projectDataAccess.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        // 驗證是否為擁有者
        if (!userProjectDataAccess.existsByUserIdAndProjectId(currentUser.getId(), projectId)) {
            throw new IllegalArgumentException("You are not the owner of this project");
        }

        if (!canEditContent(project.getCreatedBy(), currentUser.getId())) {
            throw new IllegalArgumentException("Project assigned by admin is read-only");
        }
        
        // 更新專案資訊
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        projectDataAccess.save(project);
    }
    
    @Transactional
    @Override
    public void deletePersonalProject(UUID projectId) {
        // 驗證輸入
        if (projectId == null) {
            throw new IllegalArgumentException("Project ID must not be null");
        }
        
        // 查找專案
        Project project = projectDataAccess.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        // 驗證是否為擁有者
        if (!userProjectDataAccess.existsByUserIdAndProjectId(currentUser.getId(), projectId)) {
            throw new IllegalArgumentException("You are not the owner of this project");
        }

        if (!canEditContent(project.getCreatedBy(), currentUser.getId())) {
            throw new IllegalArgumentException("Project assigned by admin is read-only");
        }
        
        // 刪除當前使用者與專案的綁定
        userProjectDataAccess.deleteByUserIdAndProjectId(currentUser.getId(), projectId);
        
        // 檢查是否還有其他使用者綁定此專案
        boolean hasOtherBindings = userProjectDataAccess.existsByProjectId(projectId);
        
        // 如果沒有其他綁定，刪除專案本身及其技能綁定
        if (!hasOtherBindings) {
            projectSkillDataAccess.deleteByProjectId(projectId);
            projectDataAccess.delete(project);
        }
    }

    @Transactional
    @Override
    public void bindPersonalProjectSkill(UUID projectId, UUID skillId, UUID skillLevelId) {
        UUID currentUserId = requireCurrentUserId();
        validateBindingInput(projectId, skillId, skillLevelId);
        ensureCanManageProjectBinding(projectId, currentUserId);
        ensureSkillVisibleToCurrentUser(skillId, currentUserId);

        if (projectSkillDataAccess.existsByProjectIdAndSkillId(projectId, skillId)) {
            throw new IllegalArgumentException("Skill already bind to project");
        }

        Project project = projectDataAccess.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        SkillLevel skillLevel = resolveAndValidateSkillLevel(skillId, skillLevelId);

        ProjectSkill projectSkill = new ProjectSkill();
        projectSkill.setProject(project);
        projectSkill.setSkill(entityManager.getReference(Skill.class, skillId));
        projectSkill.setSkillLevel(skillLevel);
        projectSkillDataAccess.save(projectSkill);
    }

    @Transactional
    @Override
    public void updatePersonalProjectSkillLevel(UUID projectId, UUID skillId, UUID skillLevelId) {
        UUID currentUserId = requireCurrentUserId();
        validateBindingInput(projectId, skillId, skillLevelId);
        ensureCanManageProjectBinding(projectId, currentUserId);
        ensureSkillVisibleToCurrentUser(skillId, currentUserId);

        ProjectSkill projectSkill = projectSkillDataAccess.findByProjectIdAndSkillId(projectId, skillId)
                .orElseThrow(() -> new IllegalArgumentException("Skill binding not found for project"));
        SkillLevel skillLevel = resolveAndValidateSkillLevel(skillId, skillLevelId);

        projectSkill.setSkillLevel(skillLevel);
        projectSkillDataAccess.save(projectSkill);
    }

    @Transactional
    @Override
    public void unbindPersonalProjectSkill(UUID projectId, UUID skillId) {
        UUID currentUserId = requireCurrentUserId();
        if (projectId == null || skillId == null) {
            throw new IllegalArgumentException("Key must not be null");
        }

        ensureCanManageProjectBinding(projectId, currentUserId);

        if (!projectSkillDataAccess.existsByProjectIdAndSkillId(projectId, skillId)) {
            throw new IllegalArgumentException("Skill binding not found for project");
        }

        projectSkillDataAccess.deleteByProjectIdAndSkillId(projectId, skillId);
    }

    private UUID requireCurrentUserId() {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Current user not found");
        }
        return currentUser.getId();
    }

    private void validateBindingInput(UUID projectId, UUID skillId, UUID skillLevelId) {
        if (projectId == null || skillId == null || skillLevelId == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
    }

    private void ensureCanManageProjectBinding(UUID projectId, UUID currentUserId) {
        if (!userProjectDataAccess.existsByUserIdAndProjectId(currentUserId, projectId)) {
            throw new IllegalArgumentException("You are not allowed to manage bindings for this project");
        }
    }

    private void ensureSkillVisibleToCurrentUser(UUID skillId, UUID currentUserId) {
        Set<UUID> visibleSkillIds = userSkillDataAccess.findByUserId(currentUserId).stream()
                .map(userSkill -> userSkill.getSkill().getId())
                .collect(Collectors.toSet());

        List<UserProject> userProjects = userProjectDataAccess.findByUserId(currentUserId);
        for (UserProject userProject : userProjects) {
            for (ProjectSkill projectSkill : userProject.getProject().getProjectSkills()) {
                visibleSkillIds.add(projectSkill.getSkill().getId());
            }
        }

        if (!visibleSkillIds.contains(skillId)) {
            throw new IllegalArgumentException("Skill is not visible to current user");
        }
    }

    private SkillLevel resolveAndValidateSkillLevel(UUID skillId, UUID skillLevelId) {
        SkillLevel skillLevel = skillLevelDataAccess.findById(skillLevelId)
                .orElseThrow(() -> new IllegalArgumentException("Skill level not found"));
        if (!skillLevel.getSkill().getId().equals(skillId)) {
            throw new IllegalArgumentException("Skill level does not belong to skill");
        }
        return skillLevel;
    }

    private boolean canEditContent(String createdBy, UUID currentUserId) {
        if (createdBy == null || createdBy.isBlank()) {
            return true;
        }
        return createdBy.equals(currentUserId.toString());
    }
}
