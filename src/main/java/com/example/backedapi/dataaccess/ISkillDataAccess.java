package com.example.backedapi.dataaccess;

import com.example.backedapi.Enity.Skill;
import org.springframework.data.domain.Example;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Data access interface for Skill entity operations.
 * Abstracts SkillRepository operations for service layer.
 */
public interface ISkillDataAccess {

    /**
     * Save a skill entity.
     *
     * @param skill the skill to save
     * @return the saved skill
     */
    Skill save(Skill skill);

    /**
     * Check if a skill exists matching the given example.
     *
     * @param example the skill example to match
     * @return true if a matching skill exists
     */
    boolean exists(Example<Skill> example);

    /**
     * Find all skills.
     *
     * @return list of all skills
     */
    List<Skill> findAll();

    /**
     * Find a skill by its ID.
     *
     * @param key the skill UUID
     * @return optional containing the skill if found
     */
    Optional<Skill> findById(UUID key);

    /**
     * Delete a skill.
     *
     * @param skill the skill to delete
     */
    void delete(Skill skill);
}
