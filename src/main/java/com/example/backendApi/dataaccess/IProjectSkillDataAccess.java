package com.example.backendApi.dataaccess;

import com.example.backendApi.Entity.ProjectSkill;

import java.util.UUID;

public interface IProjectSkillDataAccess {
    boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);

    ProjectSkill save(ProjectSkill projectSkill);

    void deleteByProjectId(UUID projectId);

    void deleteBySkillId(UUID skillId);
}
