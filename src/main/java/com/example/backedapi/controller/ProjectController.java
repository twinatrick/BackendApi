package com.example.backedapi.controller;

import com.example.backedapi.Service.IProjectService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.Vo.ProjectVo;
import com.example.backedapi.model.Vo.ResponseType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/backend/project")
@ApiControllerTag(name = "Projects", description = "Backend API endpoints - Project management")
public class ProjectController {
    private final IProjectService projectService;

    public ProjectController(IProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add project", description = "Creates a new project.")
    public ResponseType<ProjectVo> addProject(@RequestBody ProjectVo project) {
        return ResponseType.Success(projectService.addProject(project), "Project added successfully");
    }

    @GetMapping("/get")
    @ApiOperationOk(summary = "Get projects", description = "Returns all projects.")
    public ResponseType<List<ProjectVo>> getProject() {
        return ResponseType.Success(projectService.getProject(), "Projects fetched successfully");
    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update project", description = "Updates an existing project.")
    public ResponseType<String> updateProject(@RequestBody ProjectVo project) {
        projectService.updateProject(project);
        return ResponseType.Success("Project updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete project", description = "Deletes a project.")
    public ResponseType<String> deleteProject(@RequestBody ProjectVo project) {
        projectService.deleteProject(project);
        return ResponseType.Success("Project deleted successfully");
    }
}
