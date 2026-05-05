package com.example.backendApi.dataaccess;

import com.example.backendApi.Entity.UserSkill;

import java.util.List;
import java.util.UUID;

public interface IUserSkillDataAccess {
    boolean existsByUserIdAndSkillId(UUID userId, UUID skillId);

    boolean existsBySkillLevelId(UUID skillLevelId);

    UserSkill save(UserSkill userSkill);

    void deleteBySkillId(UUID skillId);
    
    List<UserSkill> findByUserId(UUID userId);
}
