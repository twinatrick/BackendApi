package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IProjectDataAccess;
import com.example.backedapi.dataaccess.ISkillDataAccess;
import com.example.backedapi.dataaccess.ISkillMapUserAndProjectDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
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
public class SkillService {
    private final ISkillDataAccess skillDataAccess;
    private final IUserDataAccess userDataAccess;
    private final IProjectDataAccess projectDataAccess;
    private final ISkillMapUserAndProjectDataAccess skillMapUserAndProjectDataAccess;

    public Skill addSkill(Skill skill) {
        if (skill.getKey() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (skill.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        Skill s=new Skill();
        s.setName(skill.getName());
        Example<Skill> example = Example.of(skill);
            if (skillDataAccess.exists(example)) {
            throw new IllegalArgumentException("Name already exists");
        }

        return skillDataAccess.save(skill);

    }

    public void updateSkill(Skill skill) {
        if (skill.getKey() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (skill.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        skillDataAccess.save(skill);

    }

    public List<Skill> getSkill() {
        return skillDataAccess.findAll();
    }

    public void BindSkillToUser(String skillKey, String UserKey) {
        Skill skill = skillDataAccess.findById(UUID.fromString(skillKey))
                .orElseThrow(() -> new IllegalArgumentException("Skill not found"));
        User user = userDataAccess.findById(UUID.fromString(UserKey))
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

    public void BindSkillToProjectAndUser(String skillKey, String projectKey, String userKey) {
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
    public void deleteSkill(Skill skill) {
        if (skill.getKey() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        skill=skillDataAccess.findById(skill.getKey()).orElseThrow(()->new IllegalArgumentException("Skill not found"));
        SkillMapUserAndProject skillMapUserAndProject = new SkillMapUserAndProject();
        skillMapUserAndProject.setSkill(skill);
        Example<SkillMapUserAndProject> example = Example.of(skillMapUserAndProject);
        List<SkillMapUserAndProject> skillMapUserAndProjects = skillMapUserAndProjectDataAccess.findAll(example);
        skillMapUserAndProjectDataAccess.deleteAll(skillMapUserAndProjects);
        skillDataAccess.delete(skill);

    }
}
