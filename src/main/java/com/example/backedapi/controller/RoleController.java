package com.example.backedapi.controller;

import com.example.backedapi.Service.IFunctionService;
import com.example.backedapi.Service.IRoleService;
import com.example.backedapi.Service.IUserService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.Dto.Vo.FunctionVo;
import com.example.backedapi.Dto.Vo.PermissionVo;
import com.example.backedapi.Dto.Vo.ResponseType;
import com.example.backedapi.Dto.Vo.RoleOutVo;
import com.example.backedapi.Dto.Vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/role")
@ApiControllerTag(name = "Roles", description = "Backend API endpoints - Role and permission management")
public class RoleController {
    @Autowired
    private IRoleService roleService;
    @Autowired
    private IFunctionService functionService;
    @Autowired
    private IUserService userService;

    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add role", description = "Creates a new role.")
    public ResponseType<RoleOutVo> addRole(@RequestBody RoleOutVo role) {
        return ResponseType.Success(roleService.addRole(role), "Role added successfully");
    }

    @PostMapping("/get")
    @ApiOperationOk(summary = "Get roles", description = "Returns all roles.")
    public ResponseType<List<RoleOutVo>> getRole() {
        return ResponseType.Success(roleService.getRole(), "Role fetched successfully");
    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update role", description = "Updates role details.")
    public ResponseType<RoleOutVo> updateRole(@RequestBody RoleOutVo role) {
        return ResponseType.Success(roleService.updateRole(role), "Role updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete role", description = "Deletes a role.")
    public ResponseType<RoleOutVo> deleteRole(@RequestBody RoleOutVo role) {
        roleService.deleteRole(role);
        return ResponseType.Success(role, "Role deleted successfully");
    }

    @PostMapping("/roleBindFunction")
    @ApiOperationBadRequest(summary = "Bind role to functions", description = "Assigns functions to a role.")
    public ResponseType<RoleOutVo> roleBindFunction(@RequestBody PermissionVo permissionVo) {
        roleService.roleBindFunction(permissionVo.getRole(), permissionVo.getFunctionList());
        return ResponseType.Success(roleService.getRoleById(permissionVo.getRole()), "Role bound to function successfully");
    }

    @PostMapping("/functionBindRole")
    @ApiOperationBadRequest(summary = "Bind function to roles", description = "Assigns roles to a function.")
    public ResponseType<FunctionVo> functionBindRole(@RequestBody PermissionVo permissionVo) {
        roleService.functionBindRole(permissionVo.getFunction(), permissionVo.getRoleList());
        return ResponseType.Success(functionService.getFunctionById(permissionVo.getFunction()), "Function bound to role successfully");
    }

    @PostMapping("/roleBindUser")
    @ApiOperationBadRequest(summary = "Bind role to users", description = "Assigns users to a role.")
    public ResponseType<RoleOutVo> roleBindUser(@RequestBody PermissionVo permissionVo) {
        roleService.roleBindUser(permissionVo.getRole(), permissionVo.getUserList());
        return ResponseType.Success(roleService.getRoleById(permissionVo.getRole()), "Role bound to user successfully");
    }

    @PostMapping("/userBindRole")
    @ApiOperationBadRequest(summary = "Bind user to roles", description = "Assigns roles to a user.")
    public ResponseType<UserVo> userBindRole(@RequestBody PermissionVo permissionVo) {
        roleService.userBindRole(permissionVo.getUser(), permissionVo.getRoleList());
        return ResponseType.Success(userService.getUserById(permissionVo.getUser()), "User bound to role successfully");
    }

    @PostMapping("/roleUnbindUser")
    @ApiOperationBadRequest(summary = "Unbind role from users", description = "Removes users from a role.")
    public ResponseType<RoleOutVo> roleUnbindUser(@RequestBody PermissionVo permissionVo) {
        roleService.roleUnbindUser(permissionVo.getRole(), permissionVo.getUserList());
        return ResponseType.Success(roleService.getRoleById(permissionVo.getRole()), "Role unbound from user successfully");
    }

    @PostMapping("/userUnbindRole")
    @ApiOperationBadRequest(summary = "Unbind user from roles", description = "Removes roles from a user.")
    public ResponseType<UserVo> userUnbindRole(@RequestBody PermissionVo permissionVo) {
        roleService.userUnbindRole(permissionVo.getUser(), permissionVo.getRoleList());
        return ResponseType.Success(userService.getUserById(permissionVo.getUser()), "User unbound from role successfully");
    }

    @PostMapping("/roleUnbindFunction")
    @ApiOperationBadRequest(summary = "Unbind role from functions", description = "Removes functions from a role.")
    public ResponseType<RoleOutVo> roleUnbindFunction(@RequestBody PermissionVo permissionVo) {
        roleService.roleUnbindFunction(permissionVo.getRole(), permissionVo.getFunctionList());
        return ResponseType.Success(roleService.getRoleById(permissionVo.getRole()), "Role unbound from function successfully");
    }

    @PostMapping("/functionUnbindRole")
    @ApiOperationBadRequest(summary = "Unbind function from roles", description = "Removes roles from a function.")
    public ResponseType<FunctionVo> functionUnbindRole(@RequestBody PermissionVo permissionVo) {
        roleService.functionUnbindRole(permissionVo.getFunction(), permissionVo.getRoleList());
        return ResponseType.Success(functionService.getFunctionById(permissionVo.getFunction()), "Function unbound from role successfully");
    }

    @PostMapping("/getFunctionByRole")
    @ApiOperationBadRequest(summary = "Get functions by role", description = "Returns functions assigned to a role.")
    public ResponseType<List<FunctionVo>> getFunctionByRole(@RequestBody RoleOutVo role) {
        return ResponseType.Success(roleService.getFunctionByRole(role.getId().toString()), "Function fetched successfully");
    }

    @PostMapping("/getRoleByFunction")
    @ApiOperationBadRequest(summary = "Get roles by function", description = "Returns roles assigned to a function.")
    public ResponseType<List<RoleOutVo>> getRoleByFunction(@RequestBody FunctionVo function) {
        return ResponseType.Success(roleService.getRoleByFunction(function.getId()), "Role fetched successfully");
    }

    @PostMapping("/getRoleByUser")
    @ApiOperationBadRequest(summary = "Get roles by user", description = "Returns roles assigned to a user.")
    public ResponseType<List<RoleOutVo>> getRoleByUser(@RequestBody UserVo user) {
        return ResponseType.Success(roleService.getRoleByUser(user.getId()), "Role fetched successfully");
    }

    @PostMapping("/getUserByRole")
    @ApiOperationBadRequest(summary = "Get users by role", description = "Returns users assigned to a role.")
    public ResponseType<List<UserVo>> getUserByRole(@RequestBody RoleOutVo role) {
        return ResponseType.Success(roleService.getUserByRole(role.getId().toString()), "User fetched successfully");
    }
}
