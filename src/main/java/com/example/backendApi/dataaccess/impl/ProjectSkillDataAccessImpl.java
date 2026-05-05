package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Repository.ProjectSkillRepository;
import com.example.backendApi.dataaccess.IProjectSkillDataAccess;
import com.example.backendApi.Entity.ProjectSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProjectSkillDataAccessImpl implements IProjectSkillDataAccess {

    private final ProjectSkillRepository projectSkillRepository;

    @Override
    public boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId) {
        return projectSkillRepository.existsByProjectIdAndSkillId(projectId, skillId);
    }

    @Override
    public boolean existsBySkillLevelId(UUID skillLevelId) {
        return projectSkillRepository.existsBySkillLevelId(skillLevelId);
    }

    @Override
    public ProjectSkill save(ProjectSkill projectSkill) {
        return projectSkillRepository.save(projectSkill);
    }

    @Override
    public void deleteByProjectId(UUID projectId) {
        projectSkillRepository.deleteByProjectId(projectId);
    }

    @Override
    public void deleteBySkillId(UUID skillId) {
        projectSkillRepository.deleteBySkillId(skillId);
    }
}
