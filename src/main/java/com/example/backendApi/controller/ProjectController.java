package com.example.backendApi.controller;

import com.example.backendApi.Dto.dto.common.PageResult;
import com.example.backendApi.Dto.dto.search.ProjectSearchQuery;
import com.example.backendApi.Service.IProjectService;
import com.example.backendApi.Service.ISkillService;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import com.example.backendApi.annotation.openapi.ApiOperationOk;
import com.example.backendApi.Dto.Vo.ProjectSkillBindRequest;
import com.example.backendApi.Dto.Vo.ProjectVo;
import com.example.backendApi.Dto.Vo.ResponseType;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/project")
@ApiControllerTag(name = "Projects", description = "Backend API endpoints - Project management")
public class ProjectController {
    private final IProjectService projectService;
    private final ISkillService skillService;

    public ProjectController(IProjectService projectService, ISkillService skillService) {
        this.projectService = projectService;
        this.skillService = skillService;
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

    @PostMapping("/bindSkill")
    @ApiOperationBadRequest(summary = "Bind project skill", description = "Binds a skill level to a project.")
    public ResponseType<String> bindProjectSkill(@RequestBody ProjectSkillBindRequest body) {
        skillService.bindProjectSkill(body.getProjectId(), body.getSkillId(), body.getSkillLevelId());
        return ResponseType.Success("Project skill bound successfully");
    }
    
    @PostMapping("/search")
    @ApiOperationOk(summary = "Search projects with pagination", description = "搜尋專案並回傳分頁結果，支援多種查詢條件與排序")
    public ResponseType<PageResult<ProjectVo>> searchProjects(@Valid @RequestBody ProjectSearchQuery query) {
        PageResult<ProjectVo> result = projectService.searchProjects(query);
        return ResponseType.Success(result, "Projects fetched successfully");
    }
    
    @GetMapping("/current")
    @ApiOperationOk(summary = "Get current user projects", description = "回傳當前使用者所屬的所有專案")
    public ResponseType<List<ProjectVo>> getCurrentUserProjects() {
        List<ProjectVo> projects = projectService.getCurrentUserProjects();
        return ResponseType.Success(projects, "Current user projects fetched successfully");
    }
    
    @PostMapping("/current/search")
    @ApiOperationOk(summary = "Search current user projects with pagination", description = "搜尋當前使用者的專案並回傳分頁結果，支援多種查詢條件與排序")
    public ResponseType<PageResult<ProjectVo>> searchCurrentUserProjects(@Valid @RequestBody ProjectSearchQuery query) {
        PageResult<ProjectVo> result = projectService.searchCurrentUserProjects(query);
        return ResponseType.Success(result, "Current user projects fetched successfully");
    }
}
