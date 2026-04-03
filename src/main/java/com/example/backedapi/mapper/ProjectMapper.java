package com.example.backedapi.mapper;

import com.example.backedapi.model.Vo.ProjectVo;
import com.example.backedapi.model.db.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProjectMapper {
    ProjectVo toVo(Project project);

    Project toEntity(ProjectVo projectVo);
}
