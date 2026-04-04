package com.example.backedapi.Enity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@Entity
@Table(name = "skill_map_user_and_project")
@NoArgsConstructor
public class    SkillMapUserAndProject extends BaseEntity {
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinColumn(name = "skill_id")
    private Skill skill;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "project_id")
    private Project project;
}
