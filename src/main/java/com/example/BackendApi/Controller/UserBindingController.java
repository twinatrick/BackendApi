package com.example.BackendApi.Controller;

import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Dto.Vo.SkillBindingsRebindRequest;
import com.example.BackendApi.Entity.User;
import com.example.BackendApi.Service.IProjectService;
import com.example.BackendApi.Service.ISkillService;
import com.example.BackendApi.Util.SkillLevelBindingMapper;
import com.example.BackendApi.Annotation.RequirePermission;
import com.example.BackendApi.Annotation.OpenApi.ApiControllerTag;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationBadRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/user/bindings")
@RequirePermission({"System", "ProjectManagement", "Edit"})
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
        skillService.rebindUserSkills(currentUserId, SkillLevelBindingMapper.toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("Current user skills rebound successfully");
    }

    @PostMapping("/project/{projectId}/skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind current user project skills", description = "Rebind project skills for a manageable project with diff strategy")
    public ResponseType<String> rebindCurrentUserProjectSkills(
            @PathVariable UUID projectId,
            @RequestBody SkillBindingsRebindRequest request) {
        projectService.rebindPersonalProjectSkills(projectId, SkillLevelBindingMapper.toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("Current user project skills rebound successfully");
    }

    private UUID requireCurrentUserId() {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalStateException("Current user not found");
        }
        return currentUser.getId();
    }

}
