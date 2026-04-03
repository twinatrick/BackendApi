package com.example.backedapi.model.db;

import com.example.backedapi.model.Vo.SkillVo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table
@NoArgsConstructor
public class Skill implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "`key`")
    private UUID key ;
    //skill Name
    private String name;

    //技能描述
    private String description;
    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;

    @JsonIgnore
    @OneToMany(mappedBy = "skill",fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    private List<SkillMapUserAndProject> skillMapUserAndProjects =new ArrayList<>();

    public SkillVo toVo(){
        SkillVo vo = new SkillVo();
        vo.setKey(this.key);
        vo.setName(this.name);
        vo.setDescription(this.description);
        vo.setCreatedBy(this.createdBy);
        vo.setUpdatedBy(this.updatedBy);
        vo.setCreatedTime(this.createdTime);
        vo.setUpdatedTime(this.updatedTime);
        return vo;
    }
}
