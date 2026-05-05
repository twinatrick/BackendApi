package com.example.backendApi.mapper;

import com.example.backendApi.Dto.Vo.ProjectVo;
import com.example.backendApi.Entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectVo toVo(Project project);

    Project toEntity(ProjectVo projectVo);
}
