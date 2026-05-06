package com.example.backendApi.Repository;

import com.example.backendApi.Entity.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserProjectRepository extends JpaRepository<UserProject, UUID> {
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);
    
    boolean existsByProjectId(UUID projectId);

    void deleteByProjectId(UUID projectId);
    
    void deleteByUserIdAndProjectId(UUID userId, UUID projectId);
    
    List<UserProject> findByUserId(UUID userId);
}
