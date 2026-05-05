package com.example.backendApi.Service.impl;

import com.example.backendApi.Dto.dto.common.PageResult;
import com.example.backendApi.Dto.dto.search.UserSearchQuery;
import com.example.backendApi.Service.IRoleService;
import com.example.backendApi.Service.IUserService;
import com.example.backendApi.Util.SortFieldValidator;
import com.example.backendApi.dataaccess.IFunctionDataAccess;
import com.example.backendApi.dataaccess.IProjectDataAccess;
import com.example.backendApi.dataaccess.IUserDataAccess;
import com.example.backendApi.dataaccess.IUserProjectDataAccess;
import com.example.backendApi.mapper.FunctionMapper;
import com.example.backendApi.mapper.UserMapper;
import com.example.backendApi.Entity.Function;
import com.example.backendApi.Entity.Project;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserProject;
import com.example.backendApi.Dto.Vo.FunctionVo;
import com.example.backendApi.Dto.Vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

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
    private final User currentUser;
    @CachePut(value = "users", key = "#user.email")
    @Override
    public UserVo createUser(UserVo userVo) {
        User user = userMapper.toEntity(userVo);
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
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
        return userDataAccess.findByEmail(email).stream().map(userMapper::toVo).toList();
    }
//    @Cacheable(value = "users", key = "#email")
    @Override
    public UserVo getOnlyUserByEmail(String email) {
        List<User> users = userDataAccess.findByEmail(email);
        return userMapper.toVo(users.getFirst());
    }
//    @CachePut(value = "users", key = "#user.email")
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

    //    @CachePut(value = "users", key = "#user.email")
    @Override
    public UserVo saveUser(UserVo userVo) {
        User user = userMapper.toEntity(userVo);
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$") && !user.getPassword().startsWith("$2b$") && !user.getPassword().startsWith("$2y$")) {
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        }
        userDataAccess.save(user);
        return userMapper.toVo(user);
    }

    @Override
    public void saveUserWithRole(UserVo userVo) {
        if(userVo.getId() == null|| userVo.getId().isEmpty()){
            User user = new User();
            user.setEmail(userVo.getEmail());
            user.setPassword(BCrypt.hashpw(userVo.getPassword(), BCrypt.gensalt()));
            user.setDisabled(userVo.isDisabled());
            userDataAccess.save(user);
            roleService.userBindRole(user.getId().toString(), userVo.getRoleArr());
            return;
        }
//        User user = new User();
//        user.setEmail(userVo.getEmail());
//        Example<User> example = Example.of(user);
        User u = userDataAccess.findByEmail(userVo.getEmail()).stream().findFirst().orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );

        u.setPassword(BCrypt.hashpw(userVo.getPassword(), BCrypt.gensalt()));
        userDataAccess.save(u);
        roleService.userUnbindAllRole(u.getId().toString());
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
        User user = userDataAccess.findByEmail(currentUser.getEmail()).stream().findFirst().orElseThrow(
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
    public void bindUserProject(String userId, String projectId) {
        UUID userUuid = mapUuid(userId);
        UUID projectUuid = mapUuid(projectId);
        if (userUuid == null || projectUuid == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        User user = userDataAccess.findById(userUuid).orElseThrow(
                () -> new IllegalArgumentException("User not found")
        );
        Project project = projectDataAccess.findById(projectUuid).orElseThrow(
                () -> new IllegalArgumentException("Project not found")
        );
        if (userProjectDataAccess.existsByUserIdAndProjectId(userUuid, projectUuid)) {
            throw new IllegalArgumentException("Project already bind to user");
        }

        UserProject userProject = new UserProject();
        userProject.setUser(user);
        userProject.setProject(project);
        userProjectDataAccess.save(userProject);
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
