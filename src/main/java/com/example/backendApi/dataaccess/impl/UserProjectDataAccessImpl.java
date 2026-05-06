package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Repository.UserProjectRepository;
import com.example.backendApi.dataaccess.IUserProjectDataAccess;
import com.example.backendApi.Entity.UserProject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserProjectDataAccessImpl implements IUserProjectDataAccess {

    private final UserProjectRepository userProjectRepository;

    @Override
    public boolean existsByUserIdAndProjectId(UUID userId, UUID projectId) {
        return userProjectRepository.existsByUserIdAndProjectId(userId, projectId);
    }
    
    @Override
    public boolean existsByProjectId(UUID projectId) {
        return userProjectRepository.existsByProjectId(projectId);
    }

    @Override
    public UserProject save(UserProject userProject) {
        return userProjectRepository.save(userProject);
    }

    @Override
    public void deleteByProjectId(UUID projectId) {
        userProjectRepository.deleteByProjectId(projectId);
    }
    
    @Override
    public void deleteByUserIdAndProjectId(UUID userId, UUID projectId) {
        userProjectRepository.deleteByUserIdAndProjectId(userId, projectId);
    }
    
    @Override
    public List<UserProject> findByUserId(UUID userId) {
        return userProjectRepository.findByUserId(userId);
    }
}
