package com.example.BackendApi.Controller;

import com.example.BackendApi.Dto.Vo.MemberSkillBindings;
import com.example.BackendApi.Dto.Vo.ProjectMemberSkillsRebindRequest;
import com.example.BackendApi.Dto.Vo.ProjectSkillRebindRequest;
import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Dto.Vo.SkillLevelBindingItem;
import com.example.BackendApi.Dto.Vo.UserProjectRebindRequest;
import com.example.BackendApi.Dto.Vo.UserSkillRebindRequest;
import com.example.BackendApi.Service.IProjectService;
import com.example.BackendApi.Service.ISkillService;
import com.example.BackendApi.Service.IUserService;
import com.example.BackendApi.Util.SkillLevelBindingMapper;
import com.example.BackendApi.Annotation.RequirePermission;
import com.example.BackendApi.Annotation.OpenApi.ApiControllerTag;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationBadRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin/bindings")
@RequirePermission({"System", "ProjectManagement", "EditAll"})
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
        skillService.rebindUserSkills(userId, SkillLevelBindingMapper.toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("User skills rebound successfully");
    }

    @PostMapping("/project-skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind project skills", description = "Rebind all project-skill relations with level diff strategy")
    public ResponseType<String> rebindProjectSkills(@RequestBody ProjectSkillRebindRequest request) {
        UUID projectId = UUID.fromString(request.getProjectId());
        projectService.rebindProjectSkills(projectId, SkillLevelBindingMapper.toSkillLevelMap(request.getBindings()));
        return ResponseType.Success("Project skills rebound successfully");
    }

    @PostMapping("/project-members-skills/rebind")
    @ApiOperationBadRequest(
            summary = "Rebind project member skills",
            description = "完整覆蓋式綁定專案成員技能。使用者必須已是專案成員（user_project 存在），否則拋出異常。"
    )
    public ResponseType<String> rebindProjectMemberSkills(@RequestBody ProjectMemberSkillsRebindRequest request) {
        UUID projectId = UUID.fromString(request.getProjectId());

        // 轉換 DTO 為 Service 層格式
        Map<UUID, Map<UUID, UUID>> memberSkillsMap = new HashMap<>();
        if (request.getMembers() != null) {
            for (MemberSkillBindings member : request.getMembers()) {
                UUID userId = UUID.fromString(member.getUserId());
                Map<UUID, UUID> skillLevelMap = SkillLevelBindingMapper.toSkillLevelMap(
                        member.getSkills() == null ? List.of() : member.getSkills()
                );
                memberSkillsMap.put(userId, skillLevelMap);
            }
        }

        projectService.rebindProjectMemberSkills(projectId, memberSkillsMap);
        return ResponseType.Success("Project member skills rebound successfully");
    }
}
