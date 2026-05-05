package com.example.backendApi.dataaccess.impl;

import com.example.backendApi.Dto.Vo.dto.search.SkillSearchQuery;
import com.example.backendApi.Repository.SkillRepository;
import com.example.backendApi.dataaccess.ISkillDataAccess;
import com.example.backendApi.dataaccess.specification.SkillSpecification;
import com.example.backendApi.Entity.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of ISkillDataAccess.
 * Delegates to Spring Data JPA SkillRepository.
 */
@Component
@RequiredArgsConstructor
public class SkillDataAccessImpl implements ISkillDataAccess {

    private final SkillRepository skillRepository;

    @Override
    public Skill save(Skill skill) {
        return skillRepository.save(skill);
    }

    @Override
    public boolean exists(Example<Skill> example) {
        return skillRepository.exists(example);
    }

    @Override
    public List<Skill> findAll() {
        return skillRepository.findAll();
    }

    @Override
    public Optional<Skill> findById(UUID key) {
        return skillRepository.findById(key);
    }

    @Override
    public void delete(Skill skill) {
        skillRepository.delete(skill);
    }
    
    @Override
    public Page<Skill> searchSkills(SkillSearchQuery query) {
        Sort sort = Sort.by(
            "asc".equalsIgnoreCase(query.getSortDir()) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC,
            query.getSortBy()
        );
        
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), sort);
        
        return skillRepository.findAll(
            SkillSpecification.buildSpecification(query),
            pageRequest
        );
    }
}
