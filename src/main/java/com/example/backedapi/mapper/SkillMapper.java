package com.example.backedapi.mapper;

import com.example.backedapi.Dto.Vo.SkillVo;
import com.example.backedapi.Enity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillVo toVo(Skill skill);

    Skill toEntity(SkillVo skillVo);
}
