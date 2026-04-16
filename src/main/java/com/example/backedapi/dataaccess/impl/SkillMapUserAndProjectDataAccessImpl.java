package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.SkillMapUserAndProjectRepository;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.Enity.Project;
import com.example.backedapi.Enity.SkillMapUserAndProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * SkillMapUserAndProjectDataAccess 實現類
 * 封裝技能映射關係的數據庫操作
 */
@Component
public class SkillMapUserAndProjectDataAccessImpl implements ISkillMapUserAndProjectDataAccess {
    
    private final SkillMapUserAndProjectRepository repository;
    
    @Autowired
    public SkillMapUserAndProjectDataAccessImpl(SkillMapUserAndProjectRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public List<SkillMapUserAndProject> findByProject(Project project) {
        SkillMapUserAndProject example = new SkillMapUserAndProject();
        example.setProject(project);
        return repository.findAll(Example.of(example));
    }
    
    @Override
    public void deleteAll(List<SkillMapUserAndProject> mappings) {
        repository.deleteAll(mappings);
    }

    @Override
    public Optional<SkillMapUserAndProject> findOne(Example<SkillMapUserAndProject> example) {
        return repository.findOne(example);
    }

    @Override
    public SkillMapUserAndProject save(SkillMapUserAndProject mapping) {
        return repository.save(mapping);
    }

    @Override
    public List<SkillMapUserAndProject> findAll(Example<SkillMapUserAndProject> example) {
        return repository.findAll(example);
    }
}

