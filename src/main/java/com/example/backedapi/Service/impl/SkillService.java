package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.ISkillService;
import com.example.backedapi.dataaccess.IProjectDataAccess;
import com.example.backedapi.dataaccess.ISkillDataAccess;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.mapper.SkillMapper;
import com.example.backedapi.model.Vo.SkillVo;
import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.db.Skill;
import com.example.backedapi.model.db.SkillMapUserAndProject;
import com.example.backedapi.model.db.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SkillService implements ISkillService {
    private final ISkillDataAccess skillDataAccess;
    private final IUserDataAccess userDataAccess;
    private final IProjectDataAccess projectDataAccess;
    private final ISkillMapUserAndProjectDataAccess skillMapUserAndProjectDataAccess;
    private final SkillMapper skillMapper;

    @Override
    public SkillVo addSkill(SkillVo skillVo) {
        Skill skill = skillMapper.toEntity(skillVo);
        if (skill.getId() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (skill.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        Example<Skill> example = Example.of(skill);
        if (skillDataAccess.exists(example)) {
            throw new IllegalArgumentException("Name already exists");
        }

        return skillMapper.toVo(skillDataAccess.save(skill));

    }

    @Override
    public void updateSkill(SkillVo skillVo) {
        Skill skill = skillMapper.toEntity(skillVo);
        if (skill.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (skill.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        skillDataAccess.save(skill);

    }

    @Override
    public List<SkillVo> getSkill() {
        return skillDataAccess.findAll().stream().map(skillMapper::toVo).toList();
    }

    private void bindSkillToUser(String skillKey, String userKey) {
        Skill skill = skillDataAccess.findById(UUID.fromString(skillKey))
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        User user = userDataAccess.findById(UUID.fromString(userKey))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SkillMapUserAndProject skillMapUserAndProject = new SkillMapUserAndProject();
        skillMapUserAndProject.setSkill(skill);
        skillMapUserAndProject.setUser(user);
        Example<SkillMapUserAndProject> example = Example.of(skillMapUserAndProject);
        skillMapUserAndProjectDataAccess.findOne(
                example
        ).ifPresent(skillMapUserAndProject1 -> {
            throw new IllegalArgumentException("Skill already bind to user");
        });
        skillMapUserAndProjectDataAccess.save(skillMapUserAndProject);
    }

    private void bindSkillToProjectAndUser(String skillKey, String projectKey, String userKey) {
        Skill skill = skillDataAccess.findById(UUID.fromString(skillKey))
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        Project project = projectDataAccess.findById(UUID.fromString(projectKey))
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        User user = userDataAccess.findById(UUID.fromString(userKey))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        SkillMapUserAndProject skillMapUserAndProject = new SkillMapUserAndProject();
        skillMapUserAndProject.setSkill(skill);
        skillMapUserAndProject.setProject(project);
        skillMapUserAndProject.setUser(user);
        Example<SkillMapUserAndProject> example = Example.of(skillMapUserAndProject);
        skillMapUserAndProjectDataAccess.findOne(
                example
        ).ifPresent(skillMapUserAndProject1 -> {
            throw new IllegalArgumentException("Skill already bind to project");
        });
        skillMapUserAndProjectDataAccess.save(skillMapUserAndProject);
    }

    @Transactional
    @Override
    public void deleteSkill(SkillVo skillVo) {
        Skill skill = skillMapper.toEntity(skillVo);
        if (skill.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        skill=skillDataAccess.findById(skill.getId()).orElseThrow(()->new IllegalArgumentException("Skill not found"));
        SkillMapUserAndProject skillMapUserAndProject = new SkillMapUserAndProject();
        skillMapUserAndProject.setSkill(skill);
        Example<SkillMapUserAndProject> example = Example.of(skillMapUserAndProject);
        List<SkillMapUserAndProject> skillMapUserAndProjects = skillMapUserAndProjectDataAccess.findAll(example);
        skillMapUserAndProjectDataAccess.deleteAll(skillMapUserAndProjects);
        skillDataAccess.delete(skill);

    }

    @Override
    public void bindSkillByType(String type, String skillId, String projectId, String userId) {
        if ("skill".equals(type)) {
            bindSkillToUser(skillId, userId);
        } else if ("project".equals(type)) {
            bindSkillToProjectAndUser(skillId, projectId, userId);
        }
    }
}
