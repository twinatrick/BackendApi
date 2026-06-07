package com.example.BackendApi.Service.impl;

import com.example.BackendApi.Dto.Vo.Search.UserSearchQuery;
import com.example.BackendApi.Dto.Vo.Common.PageResult;
import com.example.BackendApi.Service.IRoleService;
import com.example.BackendApi.Service.IUserService;
import com.example.BackendApi.Util.SortFieldValidator;
import com.example.BackendApi.DataAccess.IFunctionDataAccess;
import com.example.BackendApi.DataAccess.IProjectDataAccess;
import com.example.BackendApi.DataAccess.IUserDataAccess;
import com.example.BackendApi.DataAccess.IUserProjectDataAccess;
import com.example.BackendApi.Mapper.FunctionMapper;
import com.example.BackendApi.Mapper.UserMapper;
import com.example.BackendApi.Entity.Function;
import com.example.BackendApi.Entity.Project;
import com.example.BackendApi.Entity.User;
import com.example.BackendApi.Entity.UserProject;
import com.example.BackendApi.Dto.Vo.FunctionVo;
import com.example.BackendApi.Dto.Vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final IUserDataAccess userDataAccess;
    private final IRoleService roleService;
    private final IFunctionDataAccess functionDataAccess;
    private final IProjectDataAccess projectDataAccess;
    private final IUserProjectDataAccess userProjectDataAccess;
    private final UserMapper userMapper;
    private final FunctionMapper functionMapper;
    private final PasswordEncoder passwordEncoder;
    private final User currentUser;
    @Caching(put = {
        @CachePut(value = "users", key = "#result.id"),
        @CachePut(value = "users", key = "#result.email")
    })
    @Override
    public UserVo createUser(UserVo userVo) {
        User user = userMapper.toEntity(userVo);
        if (user.getPassword() != null && !user.getPassword().startsWith("{") && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userDataAccess.save(user);
        return userMapper.toVo(user);
    }

    @Override
    public List<UserVo> getUser() {
        return userDataAccess.findAll().stream().map(userMapper::toVo).toList();
    }
    @Override
    public List<UserVo> getUserByEmail(String email) {
        return userDataAccess.findByEmail(email).map(userMapper::toVo).map(List::of).orElseGet(List::of);
    }
    @Cacheable(value = "users", key = "#email", sync = true)
    @Override
    public UserVo getOnlyUserByEmail(String email) {
        User user = userDataAccess.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        return userMapper.toVo(user);
    }
    @Cacheable(value = "users", key = "#id", sync = true)
    @Override
    public UserVo getUserById(String id) {
        UUID userId = mapUuid(id);
        if (userId == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        User user = userDataAccess.findById(userId).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        return userMapper.toVo(user);
    }

    @Caching(put = {
        @CachePut(value = "users", key = "#result.id"),
        @CachePut(value = "users", key = "#result.email")
    })
    @Override
    public UserVo saveUser(UserVo userVo) {
        User user = userMapper.toEntity(userVo);
        if (user.getPassword() != null && !user.getPassword().startsWith("{") && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userDataAccess.save(user);
        return userMapper.toVo(user);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void saveUserWithRole(UserVo userVo) {
        if (userVo.getRoleArr() == null) {
            throw new IllegalArgumentException("Role list is required");
        }
        if(userVo.getId() == null|| userVo.getId().isEmpty()){
            User user = new User();
            user.setEmail(userVo.getEmail());
            user.setPassword(passwordEncoder.encode(userVo.getPassword()));
            user.setDisabled(userVo.isDisabled());
            userDataAccess.save(user);
            roleService.userBindRole(user.getId().toString(), userVo.getRoleArr());
            return;
        }
//        User user = new User();
//        user.setEmail(userVo.getEmail());
//        Example<User> example = Example.of(user);
        User u = userDataAccess.findByEmail(userVo.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        u.setDisabled(userVo.isDisabled());
        if (userVo.getPassword() != null && !userVo.getPassword().startsWith("{") && !userVo.getPassword().startsWith("$2a$") && !userVo.getPassword().startsWith("$2b$") && !userVo.getPassword().startsWith("$2y$")) {
            u.setPassword(passwordEncoder.encode(userVo.getPassword()));
        }
        userDataAccess.save(u);
        roleService.userBindRole(u.getId().toString(), userVo.getRoleArr());

    }

    @Override
    public List<FunctionVo> getAllParent(List<String> child){
        List<UUID> childUUID = child.stream().map(UUID::fromString).toList();
        List<Function> functions = functionDataAccess.findAllById(childUUID);
        List<UUID> parentUUID = functions.stream().map(Function::getParent).filter(parent -> !parent.isEmpty()).map(UUID::fromString).toList();
        List<Function> parentFunctions = functionDataAccess.findAllById(parentUUID);


        List<String> result = new ArrayList<>(parentFunctions.stream().map(Function::getId).map(UUID::toString).toList());
        parentFunctions.stream().map(Function::getParent).forEach(result::add);
        List<Function> parentParentFunctions = functionDataAccess.findAllById(result.stream().filter((x)->!x.isEmpty()).map(UUID::fromString).toList());


        return parentParentFunctions.stream().map(functionMapper::toVo).toList();
    }

    @Override
    public UserVo getCurrentUserInfo() {
        User user = userDataAccess.findByEmail(currentUser.getEmail()).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        UserVo userVo = userMapper.toVo(user);
        List<FunctionVo> parent = getAllParent(userVo.getPermissions().stream().map(FunctionVo::getId).toList());
        userVo.getPermissions().addAll(parent);
        return userVo;
    }

    @Override
    public List<UserVo> getAllUsersVo() {
        return getUser();
    }

    @Override
    @Transactional
    public void bindUserProject(String userId, String projectId) {
        UUID userUuid = mapUuid(userId);
        UUID projectUuid = mapUuid(projectId);
        if (userUuid == null || projectUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        if (userProjectDataAccess.existsByUserIdAndProjectId(userUuid, projectUuid)) {
            throw new IllegalArgumentException("Project already bind to user");
        }

        Set<UUID> targetProjectIds = new HashSet<>();
        List<UserProject> existingUserProjects = userProjectDataAccess.findByUserId(userUuid);
        if (existingUserProjects != null) {
            existingUserProjects.stream()
                    .map(UserProject::getProject)
                    .map(Project::getId)
                    .forEach(targetProjectIds::add);
        }
        targetProjectIds.add(projectUuid);

        rebindUserProjects(userUuid, List.copyOf(targetProjectIds));
    }

    @Override
    @Transactional
    @CacheEvict(value = "userProjects", allEntries = true)
    public void rebindUserProjects(UUID userId, List<UUID> projectIds) {
        if (userId == null) {
            throw new IllegalArgumentException("Key must not be null");
        }

        User user = userDataAccess.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Set<UUID> targetProjectIds = projectIds == null
                ? Set.of()
                : projectIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<UUID, Project> targetProjects = new HashMap<>();
        for (UUID projectId : targetProjectIds) {
            Project project = projectDataAccess.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("Project not found"));
            targetProjects.put(projectId, project);
        }

        List<UserProject> existingBindings = userProjectDataAccess.findByUserId(userId);
        if (existingBindings == null) {
            existingBindings = List.of();
        }
        Set<UUID> existingProjectIds = existingBindings.stream()
                .map(UserProject::getProject)
                .map(Project::getId)
                .collect(Collectors.toSet());

        for (UUID existingProjectId : existingProjectIds) {
            if (!targetProjectIds.contains(existingProjectId)) {
                userProjectDataAccess.deleteByUserIdAndProjectId(userId, existingProjectId);
            }
        }

        for (UUID targetProjectId : targetProjectIds) {
            if (!existingProjectIds.contains(targetProjectId)) {
                Project project = targetProjects.get(targetProjectId);
                UserProject userProject = new UserProject();
                userProject.setUser(user);
                userProject.setProject(project);
                userProjectDataAccess.save(userProject);
            }
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public void rebindUserRoles(UUID userId, List<String> roleIds) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID must not be null");
        }
        if (roleIds == null) {
            throw new IllegalArgumentException("Role list is required");
        }

        // 驗證使用者存在
        userDataAccess.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 直接調用現有的 roleService.userBindRole
        roleService.userBindRole(userId.toString(), roleIds);
    }

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
    
    @Override
    public PageResult<UserVo> searchUsers(UserSearchQuery query) {
        // 定義允許的排序欄位
        String[] allowedSortFields = {
            "id", "name", "email", "phone", "disabled", 
            "createdBy", "updatedBy", "createdTime", "updatedTime"
        };
        
        // 驗證排序欄位
        SortFieldValidator.validateSortField(query.getSortBy(), allowedSortFields);
        
        // 驗證排序方向
        SortFieldValidator.validateSortDirection(query.getSortDir());
        
        // 執行分頁查詢
        Page<User> userPage = userDataAccess.searchUsers(query);
        
        // 轉換為 VO
        List<UserVo> userVos = userPage.getContent().stream()
                .map(userMapper::toVo)
                .toList();
        
        // 返回分頁結果
        return PageResult.of(userPage, userVos);
    }
}
