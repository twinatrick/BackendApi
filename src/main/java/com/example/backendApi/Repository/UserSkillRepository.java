package com.example.backendApi.Repository;

import com.example.backendApi.Entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserSkillRepository extends JpaRepository<UserSkill, UUID> {
    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);
    
    boolean existsBySkillId(UUID skillId);

    void deleteBySkillId(UUID skillId);
    
    void deleteByUserIdAndSkillId(UUID userId, UUID skillId);
    
    List<UserSkill> findByUserId(UUID userId);
    
    List<UserSkill> findBySkillId(UUID skillId);
    
    List<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);
}
