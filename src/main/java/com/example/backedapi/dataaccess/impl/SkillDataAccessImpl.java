package com.example.backedapi.dataaccess.impl;

import com.example.backedapi.Repository.SkillRepository;
import com.example.backedapi.dataaccess.ISkillDataAccess;
import com.example.backedapi.Enity.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
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
}
