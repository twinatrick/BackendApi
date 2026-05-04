package com.example.backedapi.dataaccess;

import com.example.backedapi.Enity.UserProject;

import java.util.List;
import java.util.UUID;

public interface IUserProjectDataAccess {
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);

    UserProject save(UserProject userProject);

    void deleteByProjectId(UUID projectId);
    
    List<UserProject> findByUserId(UUID userId);
}
