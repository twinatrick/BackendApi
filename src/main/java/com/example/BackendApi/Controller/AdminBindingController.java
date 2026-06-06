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
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(AdminBindingController.class);

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
    public ResponseType<String> rebindUserProjects(@Valid @RequestBody UserProjectRebindRequest request) {
        UUID userId = parseUuid(request.getUserId(), "userId");
        List<UUID> projectIds = request.getProjectIds() == null
                ? List.of()
                : request.getProjectIds().stream().map(id -> parseUuid(id, "projectId")).toList();
        log.info("Admin rebinding user {} to {} projects", userId, projectIds.size());
        userService.rebindUserProjects(userId, projectIds);
        log.info("Admin rebound user {} projects successfully", userId);
        return ResponseType.Success("User projects rebound successfully");
    }

    @PostMapping("/user-skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind user skills", description = "Rebind all user-skill relations with level diff strategy")
    public ResponseType<String> rebindUserSkills(@Valid @RequestBody UserSkillRebindRequest request) {
        UUID userId = parseUuid(request.getUserId(), "userId");
        int bindingCount = request.getBindings() == null ? 0 : request.getBindings().size();
        log.info("Admin rebinding user {} with {} skill bindings", userId, bindingCount);
        skillService.rebindUserSkills(userId, SkillLevelBindingMapper.toSkillLevelMap(request.getBindings()));
        log.info("Admin rebound user {} skills successfully", userId);
        return ResponseType.Success("User skills rebound successfully");
    }

    @PostMapping("/project-skill/rebind")
    @ApiOperationBadRequest(summary = "Rebind project skills", description = "Rebind all project-skill relations with level diff strategy")
    public ResponseType<String> rebindProjectSkills(@Valid @RequestBody ProjectSkillRebindRequest request) {
        UUID projectId = parseUuid(request.getProjectId(), "projectId");
        int bindingCount = request.getBindings() == null ? 0 : request.getBindings().size();
        log.info("Admin rebinding project {} with {} skill bindings", projectId, bindingCount);
        projectService.rebindProjectSkills(projectId, SkillLevelBindingMapper.toSkillLevelMap(request.getBindings()));
        log.info("Admin rebound project {} skills successfully", projectId);
        return ResponseType.Success("Project skills rebound successfully");
    }

    @PostMapping("/project-members-skills/rebind")
    @ApiOperationBadRequest(
            summary = "Rebind project member skills",
            description = "完整覆蓋式綁定專案成員技能。使用者必須已是專案成員（user_project 存在），否則拋出異常。"
    )
    public ResponseType<String> rebindProjectMemberSkills(@Valid @RequestBody ProjectMemberSkillsRebindRequest request) {
        UUID projectId = parseUuid(request.getProjectId(), "projectId");
        int memberCount = request.getMembers() == null ? 0 : request.getMembers().size();
        log.info("Admin rebinding project {} member skills for {} members", projectId, memberCount);

        // 轉換 DTO 為 Service 層格式
        Map<UUID, Map<UUID, UUID>> memberSkillsMap = new HashMap<>();
        if (request.getMembers() != null) {
            for (MemberSkillBindings member : request.getMembers()) {
                if (member == null) continue;
                UUID userId = parseUuid(member.getUserId(), "member.userId");
                Map<UUID, UUID> skillLevelMap = SkillLevelBindingMapper.toSkillLevelMap(
                        member.getSkills() == null ? List.of() : member.getSkills()
                );
                memberSkillsMap.put(userId, skillLevelMap);
            }
        }

        projectService.rebindProjectMemberSkills(projectId, memberSkillsMap);
        log.info("Admin rebound project {} member skills successfully", projectId);
        return ResponseType.Success("Project member skills rebound successfully");
    }

    private static UUID parseUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + fieldName + " format: " + value);
        }
    }
}
