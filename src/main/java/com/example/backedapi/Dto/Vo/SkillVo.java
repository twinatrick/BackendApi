package com.example.backedapi.Dto.Vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;
@Getter
@Setter
@NoArgsConstructor
public class SkillVo {
    private UUID id;
    //skill Name
    private String name;

    //技能描述
    private String description;
    private String createdBy;
    private String updatedBy;
    private Date createdTime;
    private Date updatedTime;
}
