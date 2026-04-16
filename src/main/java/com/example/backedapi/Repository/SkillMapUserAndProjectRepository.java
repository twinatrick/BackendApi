package com.example.backedapi.Repository;

import com.example.backedapi.model.db.SkillMapUserAndProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface SkillMapUserAndProjectRepository extends JpaRepository<SkillMapUserAndProject, UUID> {
    @Modifying
    @Query("delete from SkillMapUserAndProject s where s.project.id = ?1")
    void deleteByProjectId(UUID id);
    @Modifying
    @Query("delete from SkillMapUserAndProject s where s.user.id = ?1")
    void deleteByUserId(UUID id);
    @Modifying
    @Query("delete from SkillMapUserAndProject s where s.skill.id = ?1")
    void deleteBySkillId(UUID id);
}
