package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IProjectDataAccess;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.db.SkillMapUserAndProject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * ProjectService - 專案業務邏輯服務
 * 重構後依賴 DataAccess 抽象層,而非直接依賴 Repository
 */
@Service
@RequiredArgsConstructor
public class ProjectService {
    
    // 依賴注入:通過構造函數注入介面(由 Lombok @RequiredArgsConstructor 自動生成)
    private final IProjectDataAccess projectDataAccess;
    private final ISkillMapUserAndProjectDataAccess skillMapDataAccess;
    /**
     * 新增專案
     * @param project 要新增的專案實體
     * @return 保存後的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗時拋出
     */
    public Project addProject(Project project) {
        if (project.getKey() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (project.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        } else if (!projectDataAccess.findByName(project.getName()).isEmpty()) {
            throw new IllegalArgumentException("Name already exists");
        }

        return projectDataAccess.save(project);
    }
    /**
     * 更新專案
     * @param project 要更新的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗時拋出
     */
    public void updateProject(Project project) {
        if (project.getKey() == null) {
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
    public List<Project> getProject() {
        return projectDataAccess.findAll();
    }
    /**
     * 刪除專案及其關聯的技能映射
     * @param project 要刪除的專案實體
     * @throws IllegalArgumentException 當參數驗證失敗或專案不存在時拋出
     */
    @Transactional
    public void deleteProject(Project project) {
        if (project.getKey() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        
        Project existingProject = projectDataAccess.findById(project.getKey())
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        
        List<SkillMapUserAndProject> skillMapUserAndProjectList = 
            skillMapDataAccess.findByProject(existingProject);
        
        skillMapDataAccess.deleteAll(skillMapUserAndProjectList);
        projectDataAccess.delete(existingProject);
    }
}
