package com.example.backendApi.Repository;

import com.example.backendApi.Entity.ProjectSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectSkillRepository extends JpaRepository<ProjectSkill, UUID> {
    boolean existsByProjectIdAndSkillId(UUID projectId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);
    
    boolean existsBySkillId(UUID skillId);

    Optional<ProjectSkill> findByProjectIdAndSkillId(UUID projectId, UUID skillId);

    void deleteByProjectId(UUID projectId);

    void deleteByProjectIdAndSkillId(UUID projectId, UUID skillId);

    void deleteBySkillId(UUID skillId);
}
