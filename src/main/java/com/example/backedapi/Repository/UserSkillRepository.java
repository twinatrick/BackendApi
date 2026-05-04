package com.example.backedapi.Repository;

import com.example.backedapi.Enity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);

    void deleteBySkillId(UUID skillId);
    
    List<UserSkill> findByUserId(UUID userId);
}
