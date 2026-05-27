package com.example.BackendApi.Mapper;

import com.example.BackendApi.Dto.Vo.SkillVo;
import com.example.BackendApi.Entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillVo toVo(Skill skill);

    Skill toEntity(SkillVo skillVo);
}
