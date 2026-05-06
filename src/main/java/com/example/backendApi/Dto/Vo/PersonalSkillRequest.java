package com.example.backendApi.Dto.Vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 個人技能請求 DTO
 * 用於一般使用者新增或修改個人技能
 */
@Getter
@Setter
@NoArgsConstructor
public class PersonalSkillRequest {
    /**
     * 技能名稱
     */
    private String name;
    
    /**
     * 技能描述
     */
    private String description;
    
    /**
     * 技能等級 ID
     */
    private String skillLevelId;
}
