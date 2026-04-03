package com.example.backedapi.model.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
public class SkillMapUserAndProject  implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`key`")
    private UUID key ;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    private User user;
    @ManyToOne(cascade = CascadeType.ALL)
    @JsonIgnore
    private Skill skill;
    @JsonIgnore
    @ManyToOne(cascade = CascadeType.ALL)
    private Project project;
}
