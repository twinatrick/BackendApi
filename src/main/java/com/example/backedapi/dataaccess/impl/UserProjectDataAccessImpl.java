package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.UserProjectRepository;
import com.example.backedapi.dataaccess.IUserProjectDataAccess;
import com.example.backedapi.Enity.UserProject;
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
    public UserProject save(UserProject userProject) {
        return userProjectRepository.save(userProject);
    }

    @Override
    public void deleteByProjectId(UUID projectId) {
        userProjectRepository.deleteByProjectId(projectId);
    }
    
    @Override
    public List<UserProject> findByUserId(UUID userId) {
        return userProjectRepository.findByUserId(userId);
    }
}
