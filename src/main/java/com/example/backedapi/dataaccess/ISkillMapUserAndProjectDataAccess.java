package com.example.backedapi.dataaccess;

import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.db.SkillMapUserAndProject;
import org.springframework.data.domain.Example;

import java.util.List;
import java.util.Optional;

/**
 * SkillMapUserAndProject 數據訪問介面
 * 定義技能與用戶、專案映射關係的數據操作方法
 */
public interface ISkillMapUserAndProjectDataAccess {
    
    /**
     * 根據 Project 查詢所有映射關係
     * @param project 專案實體
     * @return 映射關係列表
     */
    List<SkillMapUserAndProject> findByProject(Project project);
    
    /**
     * 批量刪除映射關係
     * @param mappings 要刪除的映射關係列表
     */
    void deleteAll(List<SkillMapUserAndProject> mappings);

    /**
     * 根據 Example 查詢單個映射關係
     * @param example 查詢範例
     * @return Optional 包裝的映射關係
     */
    Optional<SkillMapUserAndProject> findOne(Example<SkillMapUserAndProject> example);

    /**
     * 保存映射關係
     * @param mapping 要保存的映射關係
     * @return 保存後的映射關係
     */
    SkillMapUserAndProject save(SkillMapUserAndProject mapping);

    /**
     * 根據 Example 查詢所有映射關係
     * @param example 查詢範例
     * @return 映射關係列表
     */
    List<SkillMapUserAndProject> findAll(Example<SkillMapUserAndProject> example);
}

