package com.example.backendApi.Repository;

import com.example.backendApi.Entity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, UUID> {
    boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);

    void deleteByProjectId(UUID projectId);

    void deleteBySkillId(UUID skillId);
}
