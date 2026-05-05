package com.example.backendApi.Service.impl;

import com.example.backendApi.Dto.dto.common.PageResult;
import com.example.backendApi.Dto.dto.search.ProjectSearchQuery;
import com.example.backendApi.Enity.User;
import com.example.backendApi.Enity.UserProject;
import com.example.backendApi.Service.IProjectService;
import com.example.backendApi.Util.SortFieldValidator;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.dataaccess.IProjectDataAccess;
import com.example.backendApi.dataaccess.IUserProjectDataAccess;
import com.example.backendApi.mapper.ProjectMapper;
import com.example.backendApi.Dto.Vo.ProjectVo;
import com.example.backendApi.Enity.Project;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private final ProjectMapper projectMapper;
    private final User currentUser;
    /**
     * 新增專案
     * @param project 要新增的專案實體
     * @return 保存後的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗時拋出
     */
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

        return projectMapper.toVo(projectDataAccess.save(project));
    }
    /**
     * 更新專案
     * @param project 要更新的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗時拋出
     */
    @Override
    public void updateProject(ProjectVo projectVo) {
        Project project = projectMapper.toEntity(projectVo);
        if (project.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (project.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        projectDataAccess.save(project);
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
}
