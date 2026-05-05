package com.example.backendApi.mapper;

import com.example.backendApi.Dto.Vo.SkillVo;
import com.example.backendApi.Enity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillVo toVo(Skill skill);

    Skill toEntity(SkillVo skillVo);
}
