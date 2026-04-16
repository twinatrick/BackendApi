package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.IRoleService;
import com.example.backedapi.dataaccess.*;
import com.example.backedapi.mapper.FunctionMapper;
import com.example.backedapi.mapper.RoleMapper;
import com.example.backedapi.mapper.UserMapper;
import com.example.backedapi.Dto.Vo.FunctionVo;
import com.example.backedapi.Dto.Vo.RoleOutVo;
import com.example.backedapi.Dto.Vo.UserVo;
import com.example.backedapi.Enity.Function;
import com.example.backedapi.Enity.Role;
import com.example.backedapi.Enity.RoleFunction;
import com.example.backedapi.Enity.User;
import com.example.backedapi.Enity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {
    private final IRoleDataAccess roleDataAccess;
    private final IRoleFunctionDataAccess roleFunctionDataAccess;
    private final IFunctionDataAccess functionDataAccess;
    private final IUserDataAccess userDataAccess;
    private final IUserRoleDataAccess userRoleDataAccess;
    private final RoleMapper roleMapper;
    private final FunctionMapper functionMapper;
    private final UserMapper userMapper;

    @Override
    public RoleOutVo addRole(RoleOutVo roleOutVo) {
        Role role = roleMapper.toEntity(roleOutVo);
        Role exampleRole = new Role();
        exampleRole.setName(role.getName());
        Example<Role> example = Example.of(exampleRole);
        if (role.getId() != null) {
            throw new IllegalArgumentException("Key must be null");
        } else if (role.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        } else if (roleDataAccess.exists(example)) {
            throw new IllegalArgumentException("Name already exists");
        }

        return roleMapper.toVo(roleDataAccess.save(role));

    }

    @Override
    public List<RoleOutVo> getRole() {
        return roleDataAccess.findAll().stream().map(roleMapper::toVo).toList();
    }

    @Override
    public RoleOutVo getRoleById(String roleId) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        return roleMapper.toVo(role);
    }

    @Override
    public RoleOutVo updateRole(RoleOutVo roleOutVo) {
        Role role = roleMapper.toEntity(roleOutVo);
        if (role.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (role.getName() == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        Role existing = roleDataAccess.findById(role.getId()).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        existing.setName(role.getName());
        existing.setDescription(role.getDescription());
        existing.setPermissions(role.getPermissions());
        return roleMapper.toVo(roleDataAccess.save(existing));
    }

    @Override
    public void deleteRole(RoleOutVo roleOutVo) {
        Role role = roleMapper.toEntity(roleOutVo);
        if (role.getId() == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        role = roleDataAccess.findById(role.getId()).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        RoleFunction roleFunction = new RoleFunction();
        roleFunction.setRole(role);
        Example<RoleFunction> example = Example.of(roleFunction);
        List<RoleFunction> roleFunctions = roleFunctionDataAccess.findAll(example);
        roleFunctionDataAccess.deleteAll(roleFunctions);
        roleDataAccess.delete(role);

    }
    @Transactional
    @Override
    public void roleBindFunction(String roleId, List<String> functionIds) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (functionIds == null || functionIds.isEmpty()) {
            throw new IllegalArgumentException("Function list is empty");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        List<UUID> functionUuids = functionIds.stream().map(UUID::fromString).toList();
        List<Function> functions = functionDataAccess.findAllById(functionUuids);
        roleFunctionDataAccess.deleteByFunctionAndRole(functions, List.of(role));
        List<RoleFunction> roleFunctions = functions.stream().map(function -> {
            RoleFunction roleFunction = new RoleFunction();
            roleFunction.setRole(role);
            roleFunction.setFunction(function);
            return roleFunction;
        }).toList();
        roleFunctionDataAccess.saveAll(roleFunctions);
    }

    @Transactional
    @Override
    public void functionBindRole(String functionId, List<String> roleIds) {
        UUID functionUuid = mapUuid(functionId);
        if (functionUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Role list is empty");
        }
        Function function = functionDataAccess.findById(functionUuid).orElseThrow(
                () -> new IllegalArgumentException("Function not found")
        );
        List<UUID> roleUuids = roleIds.stream().map(UUID::fromString).toList();
        List<Role> roles = roleDataAccess.findAllById(roleUuids);
        roleFunctionDataAccess.deleteByFunctionAndRole(List.of(function), roles);

        List<RoleFunction> roleFunctions = roles.stream().map(role -> {
            RoleFunction roleFunction = new RoleFunction();
            roleFunction.setRole(role);
            roleFunction.setFunction(function);
            return roleFunction;
        }).toList();
        roleFunctionDataAccess.saveAll(roleFunctions);
    }

    @Transactional
    @Override
    public void roleBindUser(String roleId, List<String> userIds) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("User list is empty");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        List<UUID> userUuids = userIds.stream().map(UUID::fromString).toList();
        List<User> users = userDataAccess.findAllById(userUuids);
        userRoleDataAccess.deleteAllByUserInAndRoleIn(users, List.of(role));
        List<UserRole> userRoles = users.stream().map(user -> {
            UserRole userRole = new UserRole();
            userRole.setRole(role);
            userRole.setUser(user);
            return userRole;
        }).toList();
        userRoleDataAccess.saveAll(userRoles);
    }

    @Override
    public void userBindRole(String userId, List<String> roleIds) {
        UUID userUuid = mapUuid(userId);
        if (userUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Role list is empty");
        }
        User user = userDataAccess.findById(userUuid).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        List<UUID> roleUuids = roleIds.stream().map(UUID::fromString).toList();
        List<Role> roles = roleDataAccess.findAllById(roleUuids);
        userRoleDataAccess.deleteAllByUserInAndRoleIn(List.of(user), roles);
        List<UserRole> userRoles = roles.stream().map(role -> {
            UserRole userRole = new UserRole();
            userRole.setRole(role);
            userRole.setUser(user);
            return userRole;
        }).toList();
        userRoleDataAccess.saveAll(userRoles);
    }
    @Transactional
    @Override
    public void roleUnbindUser(String roleId, List<String> userIds) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (userIds == null || userIds.isEmpty()) {
            throw new IllegalArgumentException("User list is empty");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        List<UUID> userUuids = userIds.stream().map(UUID::fromString).toList();
        List<User> users = userDataAccess.findAllById(userUuids);
        userRoleDataAccess.deleteAllByUserInAndRoleIn(users, List.of(role));

    }
    @Transactional
    @Override
    public void userUnbindRole(String userId, List<String> roleIds) {
        UUID userUuid = mapUuid(userId);
        if (userUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Role list is empty");
        }
        User user = userDataAccess.findById(userUuid).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        List<UUID> roleUuids = roleIds.stream().map(UUID::fromString).toList();
        List<Role> roles = roleDataAccess.findAllById(roleUuids);
        userRoleDataAccess.deleteAllByUserInAndRoleIn(List.of(user), roles);

    }
    @Transactional
    @Override
    public void userUnbindAllRole(String userId) {
        UUID userUuid = mapUuid(userId);
        if (userUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        User user = userDataAccess.findById(userUuid).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        List<Role> roles = roleDataAccess.findAll();
        userRoleDataAccess.deleteAllByUserInAndRoleIn(List.of(user), roles);

    }
    @Transactional
    @Override
    public void roleUnbindFunction(String roleId, List<String> functionIds) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (functionIds == null || functionIds.isEmpty()) {
            throw new IllegalArgumentException("Function list is empty");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        List<UUID> functionUuids = functionIds.stream().map(UUID::fromString).toList();
        List<Function> functions = functionDataAccess.findAllById(functionUuids);
        roleFunctionDataAccess.deleteByFunctionAndRole(functions, List.of(role));

    }

    @Transactional
    @Override
    public void functionUnbindRole(String functionId, List<String> roleIds) {
        UUID functionUuid = mapUuid(functionId);
        if (functionUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        } else if (roleIds == null || roleIds.isEmpty()) {
            throw new IllegalArgumentException("Role list is empty");
        }
        Function function = functionDataAccess.findById(functionUuid).orElseThrow(
                () -> new IllegalArgumentException("Function not found")
        );
        List<UUID> roleUuids = roleIds.stream().map(UUID::fromString).toList();
        List<Role> roles = roleDataAccess.findAllById(roleUuids);
        roleFunctionDataAccess.deleteByFunctionAndRole(List.of(function), roles);

    }


    @Override
    public List<FunctionVo> getFunctionByRole(String roleId) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        return role.getRoleFunctions().stream()
                .map(RoleFunction::getFunction)
                .map(functionMapper::toVo)
                .toList();
    }

    @Override
    public List<RoleOutVo> getRoleByFunction(String functionId) {
        UUID functionUuid = mapUuid(functionId);
        if (functionUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Function function = functionDataAccess.findById(functionUuid).orElseThrow(
                () -> new IllegalArgumentException("Function not found")
        );
        return function.getRoleFunctions().stream()
                .map(RoleFunction::getRole)
                .map(roleMapper::toVo)
                .toList();
    }

    @Override
    public List<UserVo> getUserByRole(String roleId) {
        UUID roleUuid = mapUuid(roleId);
        if (roleUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        Role role = roleDataAccess.findById(roleUuid).orElseThrow(
                () -> new IllegalArgumentException("Role not found")
        );
        return role.getUserRoles().stream()
                .map(UserRole::getUser)
                .map(userMapper::toVo)
                .toList();
    }

    @Override
    public List<RoleOutVo> getRoleByUser(String userId) {
        UUID userUuid = mapUuid(userId);
        if (userUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        User user = userDataAccess.findById(userUuid).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        return user.getRoles().stream()
                .map(UserRole::getRole)
                .map(roleMapper::toVo)
                .toList();
    }
    @Override
    public RoleOutVo getRoleByName(String name){
        Role role = roleDataAccess.findRoleByName(name);
        return role == null ? null : roleMapper.toVo(role);
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}
