package com.example.backedapi.Service;

import com.example.backedapi.model.Vo.SkillVo;

import java.util.List;

public interface ISkillService {
    SkillVo addSkill(SkillVo skillVo);

    void updateSkill(SkillVo skillVo);

    List<SkillVo> getSkill();

    void bindSkillByType(String type, String skillId, String projectId, String userId);

    void deleteSkill(SkillVo skillVo);
}
