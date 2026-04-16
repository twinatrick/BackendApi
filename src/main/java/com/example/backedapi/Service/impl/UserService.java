package com.example.backedapi.Service.impl;

import com.example.backedapi.Service.IRoleService;
import com.example.backedapi.Service.IUserService;
import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.mapper.FunctionMapper;
import com.example.backedapi.mapper.UserMapper;
import com.example.backedapi.Enity.Function;
import com.example.backedapi.Enity.User;
import com.example.backedapi.Dto.Vo.FunctionVo;
import com.example.backedapi.Dto.Vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
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

    private UUID mapUuid(String id) {
        return id == null || id.isBlank() ? null : UUID.fromString(id);
    }
}
