package com.example.backedapi.Repository;

import com.example.backedapi.Enity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, UUID> {
    boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);

    void deleteByProjectId(UUID projectId);

    void deleteBySkillId(UUID skillId);
}
