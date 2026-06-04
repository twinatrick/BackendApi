package com.example.BackendApi.Controller;

import com.example.BackendApi.Dto.Vo.Search.UserSearchQuery;
import com.example.BackendApi.Dto.Vo.Common.PageResult;
import com.example.BackendApi.Service.ISkillService;
import com.example.BackendApi.Service.IUserService;
import com.example.BackendApi.Annotation.RequirePermission;
import com.example.BackendApi.Annotation.OpenApi.ApiControllerTag;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationAuth;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationBadRequest;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationOk;
import com.example.BackendApi.Dto.Vo.UserProjectBindRequest;
import com.example.BackendApi.Dto.Vo.UserSkillBindRequest;
import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Dto.Vo.UserVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
@ApiControllerTag(name = "Users", description = "Backend API endpoints - User management")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ISkillService skillService;


    @PostMapping(value = "/create")
    @ApiOperationBadRequest(summary = "Create user", description = "Creates a new user account.")
    public boolean createUser(@RequestBody UserVo user) {
        userService.createUser(user);
        return true;
    }

    @GetMapping("/infoVo")
    @ApiOperationAuth(summary = "Get current user info", description = "Returns current user profile and permissions.")
    public ResponseType<UserVo> getUserInfo() {
        return new ResponseType<>(userService.getCurrentUserInfo());
    }

    @PostMapping("/bindSkill")
    @Deprecated
    @RequirePermission({"System", "ProjectManagement", "Edit"})
    @ApiOperationBadRequest(summary = "Bind user skill", description = "Binds a skill level to a user. This operation manages binding relation only and does not modify skill content. Admin-assigned skills can still be bound by authorized users.")
    public ResponseType<String> bindUserSkill(@RequestBody UserSkillBindRequest body) {
        skillService.bindUserSkill(body.getUserId(), body.getSkillId(), body.getSkillLevelId());
        return ResponseType.Success("User skill bound successfully");
    }

    @PostMapping("/bindProject")
    @Deprecated
    @RequirePermission({"System", "ProjectManagement", "Edit"})
    @ApiOperationBadRequest(summary = "Bind user project", description = "Binds a user to a project.")
    public ResponseType<String> bindUserProject(@RequestBody UserProjectBindRequest body) {
        userService.bindUserProject(body.getUserId(), body.getProjectId());
        return ResponseType.Success("User project bound successfully");
    }

    @GetMapping("/getAllUser")
    @ApiOperationOk(summary = "Get all users", description = "Returns all users with their roles and permissions.")
    public ResponseType<List<UserVo>> getAllUser() {
        return new ResponseType<>(0, userService.getAllUsersVo());
    }

    @PostMapping("/saveUser")
    @ApiOperationBadRequest(summary = "Save user with roles", description = "Updates a user and their role assignments.")
    public ResponseType<String> saveUser(@RequestBody UserVo user) {
        userService.saveUserWithRole(user);
        return new ResponseType<>(0, "User updated successfully");
    }
    
    @PostMapping("/search")
    @ApiOperationOk(summary = "Search users with pagination", description = "搜尋使用者並回傳分頁結果，支援多種查詢條件與排序")
    public ResponseType<PageResult<UserVo>> searchUsers(@Valid @RequestBody UserSearchQuery query) {
        PageResult<UserVo> result = userService.searchUsers(query);
        return new ResponseType<>(result);
    }
}

