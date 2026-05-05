package com.example.backendApi.Service.impl;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.RoleSearchQuery;
import com.example.backendApi.Service.IRoleService;
import com.example.backendApi.Util.SortFieldValidator;
import com.example.backendApi.dataaccess.*;
import com.example.backendApi.mapper.FunctionMapper;
import com.example.backendApi.mapper.RoleMapper;
import com.example.backendApi.mapper.UserMapper;
import com.example.backendApi.Dto.Vo.FunctionVo;
import com.example.backendApi.Dto.Vo.RoleOutVo;
import com.example.backendApi.Dto.Vo.UserVo;
import com.example.backendApi.Entity.Function;
import com.example.backendApi.Entity.Role;
import com.example.backendApi.Entity.RoleFunction;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
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
    
    @Override
    public PageResult<RoleOutVo> searchRoles(RoleSearchQuery query) {
        // 定義允許的排序欄位
        String[] allowedSortFields = {
            "id", "name", "description", "permissions",
            "createdBy", "updatedBy", "createdTime", "updatedTime"
        };
        
        // 驗證排序欄位
        SortFieldValidator.validateSortField(query.getSortBy(), allowedSortFields);
        
        // 驗證排序方向
        SortFieldValidator.validateSortDirection(query.getSortDir());
        
        // 執行分頁查詢
        Page<Role> rolePage = roleDataAccess.searchRoles(query);
        
        // 轉換為 VO
        List<RoleOutVo> roleVos = rolePage.getContent().stream()
                .map(roleMapper::toVo)
                .toList();
        
        // 返回分頁結果
        return PageResult.of(rolePage, roleVos);
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}
