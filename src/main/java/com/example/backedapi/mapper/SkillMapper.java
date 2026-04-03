package com.example.backedapi.mapper;

import com.example.backedapi.model.Vo.SkillVo;
import com.example.backedapi.model.db.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillVo toVo(Skill skill);

    Skill toEntity(SkillVo skillVo);
}
