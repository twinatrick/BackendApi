package com.example.backedapi.mapper;

import com.example.backedapi.Dto.Vo.ProjectVo;
import com.example.backedapi.Enity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectVo toVo(Project project);

    Project toEntity(ProjectVo projectVo);
}
