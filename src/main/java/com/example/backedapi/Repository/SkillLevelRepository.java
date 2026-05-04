package com.example.backedapi.Repository;

import com.example.backedapi.Enity.SkillLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface SkillLevelRepository extends JpaRepository<SkillLevel, UUID>, JpaSpecificationExecutor<SkillLevel> {
    List<SkillLevel> findBySkillIdOrderByLevelValueAsc(UUID skillId);

    boolean existsBySkillIdAndLevelValue(UUID skillId, Integer levelValue);

    void deleteBySkillId(UUID skillId);
}
