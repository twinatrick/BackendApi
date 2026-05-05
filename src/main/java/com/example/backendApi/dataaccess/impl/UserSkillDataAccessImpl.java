package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Repository.UserSkillRepository;
import com.example.backendApi.dataaccess.IUserSkillDataAccess;
import com.example.backendApi.Enity.UserSkill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserSkillDataAccessImpl implements IUserSkillDataAccess {

    private final UserSkillRepository userSkillRepository;

    @Override
    public boolean existsByUserIdAndSkillId(UUID userId, UUID skillId) {
        return userSkillRepository.existsByUserIdAndSkillId(userId, skillId);
    }

    @Override
    public boolean existsBySkillLevelId(UUID skillLevelId) {
        return userSkillRepository.existsBySkillLevelId(skillLevelId);
    }

    @Override
    public UserSkill save(UserSkill userSkill) {
        return userSkillRepository.save(userSkill);
    }

    @Override
    public void deleteBySkillId(UUID skillId) {
        userSkillRepository.deleteBySkillId(skillId);
    }
    
    @Override
    public List<UserSkill> findByUserId(UUID userId) {
        return userSkillRepository.findByUserId(userId);
    }
}
