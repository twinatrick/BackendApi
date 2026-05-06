package com.example.backendApi.dataaccess;

import com.example.backendApi.Entity.ProjectSkill;

import java.util.Optional;
import java.util.UUID;

public interface IProjectSkillDataAccess {
    boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);
    
    boolean existsBySkillId(UUID skillId);

    ProjectSkill save(ProjectSkill projectSkill);

    Optional<ProjectSkill> findByProjectIdAndSkillId(UUID projectId, UUID skillId);

    void deleteByProjectId(UUID projectId);

    void deleteByProjectIdAndSkillId(UUID projectId, UUID skillId);

    void deleteBySkillId(UUID skillId);
}
