package com.example.backendApi.controller;

import com.example.backendApi.Dto.Vo.ResponseType;
import com.example.backendApi.Dto.Vo.SkillBindingsRebindRequest;
import com.example.backendApi.Dto.Vo.SkillLevelBindingItem;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Service.IProjectService;
import com.example.backendApi.Service.ISkillService;
import com.example.backendApi.annotation.RequireRole;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/user/bindings")
@RequireRole({"user", "admin"})
@ApiControllerTag(name = "User Bindings", description = "Backend API endpoints - User binding self-service")
public class UserBindingController {

    private final User currentUser;
    private final ISkillService skillService;
    private final IProjectService projectService;

    public UserBindingController(User currentUser, ISkillService skillService, IProjectService projectService) {
        this.currentUser = currentUser;
        this.skillService = skillService;
        this.projectService = projectService;
    }

    @PostMapping("/skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind current user skills", description = "Rebind all current-user skill-level bindings with diff strategy")
    public ResponseType<String> rebindCurrentUserSkills(@RequestBody SkillBindingsRebindRequest request) {
        UUID currentUserId = requireCurrentUserId();
        skillService.rebindUserSkills(currentUserId, toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("Current user skills rebound successfully");
    }

    @PostMapping("/project/{projectId}/skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind current user project skills", description = "Rebind project skills for a manageable project with diff strategy")
    public ResponseType<String> rebindCurrentUserProjectSkills(
            @PathVariable UUID projectId,
            @RequestBody SkillBindingsRebindRequest request) {
        projectService.rebindPersonalProjectSkills(projectId, toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("Current user project skills rebound successfully");
    }

    private UUID requireCurrentUserId() {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Current user not found");
        }
        return currentUser.getId();
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
