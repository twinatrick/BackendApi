package com.example.BackendApi.Dto.Vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ProjectMemberSkillsRebindRequest {
    private String projectId;
    private List<MemberSkillBindings> members;
}
