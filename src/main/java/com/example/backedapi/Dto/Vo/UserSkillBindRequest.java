package com.example.backedapi.Dto.Vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserSkillBindRequest {
    private String userId;
    private String skillId;
    private String skillLevelId;
}
