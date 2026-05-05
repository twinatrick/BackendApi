package com.example.backendApi.Repository;

import com.example.backendApi.Entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface SkillRepository extends JpaRepository<Skill, UUID>, JpaSpecificationExecutor<Skill> {
    List<Skill> findByName(String name);
}
