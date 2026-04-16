package com.example.backedapi.Dto.Vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BindUserSkillOrProject{
    private String userId;
    private String skill;
    private String projectId;
    private String type;
}