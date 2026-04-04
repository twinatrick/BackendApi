package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.ProjectRepository;
import com.example.backedapi.dataaccess.IProjectDataAccess;
import com.example.backedapi.Enity.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ProjectDataAccess 實現類
 * 封裝 Project 相關的數據庫操作,內部使用 JPA Repository
 */
@Component
public class ProjectDataAccessImpl implements IProjectDataAccess {
    
    private final ProjectRepository projectRepository;
    
    @Autowired
    public ProjectDataAccessImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    
    @Override
    public Project save(Project project) {
        return projectRepository.save(project);
    }
    
    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }
    
    @Override
    public Optional<Project> findById(UUID id) {
        return projectRepository.findById(id);
    }
    
    @Override
    public List<Project> findByName(String name) {
        return projectRepository.findByName(name);
    }
    
    @Override
    public void delete(Project project) {
        projectRepository.delete(project);
    }
    
    @Override
    public boolean existsById(UUID id) {
        return projectRepository.existsById(id);
    }
}
