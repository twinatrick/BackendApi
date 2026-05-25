package com.example.backendApi.controller;

import com.example.backendApi.Dto.Vo.ProjectSkillRebindRequest;
import com.example.backendApi.Dto.Vo.ResponseType;
import com.example.backendApi.Dto.Vo.SkillLevelBindingItem;
import com.example.backendApi.Dto.Vo.UserProjectRebindRequest;
import com.example.backendApi.Dto.Vo.UserSkillRebindRequest;
import com.example.backendApi.Service.IProjectService;
import com.example.backendApi.Service.ISkillService;
import com.example.backendApi.Service.IUserService;
import com.example.backendApi.annotation.RequireRole;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/bindings")
@RequireRole({"admin"})
@ApiControllerTag(name = "Admin Bindings", description = "Backend API endpoints - Admin binding management")
public class AdminBindingController {

    private final IUserService userService;
    private final ISkillService skillService;
    private final IProjectService projectService;

    public AdminBindingController(IUserService userService, ISkillService skillService, IProjectService projectService) {
        this.userService = userService;
        this.skillService = skillService;
        this.projectService = projectService;
    }

    @PostMapping("/user-project/rebind")
    @ApiOperationBadRequest(summary = "Rebind user projects", description = "Rebind all user-project relations with diff strategy")
    public ResponseType<String> rebindUserProjects(@RequestBody UserProjectRebindRequest request) {
        UUID userId = UUID.fromString(request.getUserId());
        List<UUID> projectIds = request.getProjectIds() == null
                ? List.of()
                : request.getProjectIds().stream().map(UUID::fromString).toList();
        userService.rebindUserProjects(userId, projectIds);
        return ResponseType.Success("User projects rebound successfully");
    }

    @PostMapping("/user-skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind user skills", description = "Rebind all user-skill relations with level diff strategy")
    public ResponseType<String> rebindUserSkills(@RequestBody UserSkillRebindRequest request) {
        UUID userId = UUID.fromString(request.getUserId());
        skillService.rebindUserSkills(userId, toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("User skills rebound successfully");
    }

    @PostMapping("/project-skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind project skills", description = "Rebind all project-skill relations with level diff strategy")
    public ResponseType<String> rebindProjectSkills(@RequestBody ProjectSkillRebindRequest request) {
        UUID projectId = UUID.fromString(request.getProjectId());
        projectService.rebindProjectSkills(projectId, toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("Project skills rebound successfully");
    }

    private Map<UUID, UUID> toSkillLevelMap(List<SkillLevelBindingItem> bindings) {
        if (bindings == null || bindings.isEmpty()) {
            return Map.of();
        }

        Map<UUID, UUID> map = new LinkedHashMap<>();
        for (SkillLevelBindingItem item : bindings) {
            if (item == null || item.getSkillId() == null || item.getSkillLevelId() == null) {
                throw new IllegalArgumentException("Key must not be null");
            }
            map.put(UUID.fromString(item.getSkillId()), UUID.fromString(item.getSkillLevelId()));
        }
        return map;
    }
}
