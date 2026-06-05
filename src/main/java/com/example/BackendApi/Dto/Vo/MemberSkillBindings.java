package com.example.BackendApi.Dto.Vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class MemberSkillBindings {
    private String userId;
    private List<SkillLevelBindingItem> skills;
}
