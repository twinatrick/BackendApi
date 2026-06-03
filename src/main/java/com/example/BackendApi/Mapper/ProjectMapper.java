package com.example.BackendApi.Mapper;

import com.example.BackendApi.Dto.Vo.ProjectVo;
import com.example.BackendApi.Entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectVo toVo(Project project);

    Project toEntity(ProjectVo projectVo);
}
