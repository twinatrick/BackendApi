package com.example.backedapi.Service;

import com.example.backedapi.model.Vo.ProjectVo;

import java.util.List;

public interface IProjectService {
    ProjectVo addProject(ProjectVo projectVo);

    void updateProject(ProjectVo projectVo);

    List<ProjectVo> getProject();

    void deleteProject(ProjectVo projectVo);
}
