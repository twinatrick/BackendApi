package com.example.backedapi.dataaccess;

import com.example.backedapi.Enity.ProjectSkill;

import java.util.UUID;

public interface IProjectSkillDataAccess {
    boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);

    ProjectSkill save(ProjectSkill projectSkill);

    void deleteByProjectId(UUID projectId);

    void deleteBySkillId(UUID skillId);
}
