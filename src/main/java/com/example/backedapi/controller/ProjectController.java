package com.example.backedapi.controller;

import com.example.backedapi.Service.ProjectService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.db.Project;
import com.example.backedapi.model.Vo.ResponseType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/backend/project")
@ApiControllerTag(name = "Projects", description = "Backend API endpoints - Project management")
public class ProjectController {
    private ProjectService projectService;
    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add project", description = "Creates a new project.")
    public ResponseType<Project> addProject(@RequestBody Project project) {
        try {
            project=  projectService.addProject(project);
        }catch (Exception e){
            return new ResponseType<>( -1,null,"Error adding project");
        }

        return new ResponseType<>( 0,project);
    }
    @GetMapping("/get")
    @ApiOperationOk(summary = "Get projects", description = "Returns all projects.")
    public ResponseType<List<Project>> getProject() {
        return new ResponseType<>( 0,projectService.getProject());
    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update project", description = "Updates an existing project.")
    public ResponseType<String> updateProject(@RequestBody Project project) {
        try {
            projectService.updateProject(project);
        }catch (Exception e){
            return new ResponseType<>( -1,"Error updating project");
        }

        return new ResponseType<>( 0,"Project updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete project", description = "Deletes a project.")
    public ResponseType<String> deleteProject(@RequestBody Project project) {
        try {
            projectService.deleteProject(project);
        }catch (Exception e){
            return new ResponseType<>( -1,"Error deleting project");
        }

        return new ResponseType<>( 0,"Project deleted successfully");
    }
}
