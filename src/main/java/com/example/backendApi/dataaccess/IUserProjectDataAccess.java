package com.example.backendApi.dataaccess;

import com.example.backendApi.Entity.UserProject;

import java.util.List;
import java.util.UUID;

public interface IUserProjectDataAccess {
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);

    UserProject save(UserProject userProject);

    void deleteByProjectId(UUID projectId);
    
    List<UserProject> findByUserId(UUID userId);
}
