
package com.example.backedapi.controller;

import com.example.backedapi.Service.ISkillService;
import com.example.backedapi.Service.IUserService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationAuth;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.Vo.BindUserSkillOrProject;
import com.example.backedapi.model.Vo.ResponseType;
import com.example.backedapi.model.Vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/backend/users")
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

    @PostMapping("/BindUserSkillOrProject")
    @ApiOperationBadRequest(summary = "Bind user skill or project", description = "Binds a skill to a user or to a project and user.")
    public ResponseType<String> BindUserSkillOrProject(@RequestBody BindUserSkillOrProject body) {
        skillService.bindSkillByType(body.getType(), body.getSkill(), body.getProjectId(), body.getUserId());
        return new ResponseType<>(0, "Bind updated successfully");
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
}
