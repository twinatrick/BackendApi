package com.example.BackendApi.DataAccess.impl;

import com.example.BackendApi.Dto.Vo.dto.search.SkillLevelSearchQuery;
import com.example.BackendApi.Repository.SkillLevelRepository;
import com.example.BackendApi.DataAccess.ISkillLevelDataAccess;
import com.example.BackendApi.DataAccess.specification.SkillLevelSpecification;
import com.example.BackendApi.Entity.SkillLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SkillLevelDataAccessImpl implements ISkillLevelDataAccess {

    private final SkillLevelRepository skillLevelRepository;

    @Override
    public SkillLevel save(SkillLevel skillLevel) {
        return skillLevelRepository.save(skillLevel);
    }

    @Override
    public Optional<SkillLevel> findById(UUID id) {
        return skillLevelRepository.findById(id);
    }

    @Override
    public List<SkillLevel> findBySkillIdOrderByLevelValueAsc(UUID skillId) {
        return skillLevelRepository.findBySkillIdOrderByLevelValueAsc(skillId);
    }

    @Override
    public boolean existsBySkillIdAndLevelValue(UUID skillId, Integer levelValue) {
        return skillLevelRepository.existsBySkillIdAndLevelValue(skillId, levelValue);
    }

    @Override
    public void delete(SkillLevel skillLevel) {
        skillLevelRepository.delete(skillLevel);
    }

    @Override
    public void deleteBySkillId(UUID skillId) {
        skillLevelRepository.deleteBySkillId(skillId);
    }
    
    @Override
    public Page<SkillLevel> searchSkillLevels(SkillLevelSearchQuery query) {
        Sort sort = Sort.by(
            "asc".equalsIgnoreCase(query.getSortDir()) 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC,
            query.getSortBy()
        );
        
        PageRequest pageRequest = PageRequest.of(query.getPage(), query.getSize(), sort);
        
        return skillLevelRepository.findAll(
            SkillLevelSpecification.buildSpecification(query),
            pageRequest
        );
    }
}
