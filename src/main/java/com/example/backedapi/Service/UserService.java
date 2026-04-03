package com.example.backedapi.Service;

import com.example.backedapi.dataaccess.IFunctionDataAccess;
import com.example.backedapi.dataaccess.IRoleDataAccess;
import com.example.backedapi.dataaccess.IUserDataAccess;
import com.example.backedapi.model.db.Function;
import com.example.backedapi.model.db.Role;
import com.example.backedapi.model.db.User;
import com.example.backedapi.model.Vo.FunctionVo;
import com.example.backedapi.model.Vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.mindrot.jbcrypt.BCrypt;

@Service
@RequiredArgsConstructor
public class UserService {

    private final IUserDataAccess userDataAccess;
    private final IRoleDataAccess roleDataAccess;
    private final RoleService roleService;
    private final IFunctionDataAccess functionDataAccess;
    @CachePut(value = "users", key = "#user.email")
    public void createUser(User user) {
        userDataAccess.save(user);
    }

    public List<User> getUser() {
        return userDataAccess.findAll();
    }
    public List<User> getUserByEmail(String email) {
        return userDataAccess.findByEmail(email);
    }
//    @Cacheable(value = "users", key = "#email")
    public User getOnlyUserByEmail(String email) {
        List<User> users = userDataAccess.findByEmail(email);
        return users.getFirst();
    }
//    @CachePut(value = "users", key = "#user.email")
    public void saveUser(User user) {
        userDataAccess.save(user);
    }

    public void saveUserWithRole(UserVo userVo) {
        if(userVo.getKey() == null|| userVo.getKey().isEmpty()){
            User user = new User();
            user.setEmail(userVo.getEmail());
            user.setPassword(BCrypt.hashpw(userVo.getPassword(), BCrypt.gensalt()));
            user.setDisabled(userVo.isDisabled());
            userDataAccess.save(user);
            List<Role> roles = roleDataAccess.findRoleByKeyIn(userVo.getRoleArr().stream().map(UUID::fromString).toList());
            roleService.userBindRole(user, roles);
            return;
        }
//        User user = new User();
//        user.setEmail(userVo.getEmail());
        List<Role> roles = roleDataAccess.findRoleByKeyIn(userVo.getRoleArr().stream().map(UUID::fromString).toList());
//        Example<User> example = Example.of(user);
        User u = getOnlyUserByEmail(userVo.getEmail());

        u.setPassword(BCrypt.hashpw(userVo.getPassword(), BCrypt.gensalt()));
        userDataAccess.save(u);
        roleService.userUnbindAllRole(u);
        roleService.userBindRole(u, roles);

    }

    public List<FunctionVo> getAllParent(List<String> child){
        List<UUID> childUUID = child.stream().map(UUID::fromString).toList();
        List<Function> functions = functionDataAccess.findAllById(childUUID);
        List<UUID> parentUUID = functions.stream().map(Function::getParent).filter(parent -> !parent.isEmpty()).map(UUID::fromString).toList();
        List<Function> parentFunctions = functionDataAccess.findAllById(parentUUID);


        List<String> result = new ArrayList<>(parentFunctions.stream().map(Function::getId).map(UUID::toString).toList());
        parentFunctions.stream().map(Function::getParent).forEach(result::add);
        List<Function> parentParentFunctions = functionDataAccess.findAllById(result.stream().filter((x)->!x.isEmpty()).map(UUID::fromString).toList());


        return parentParentFunctions.stream().map(Function::toVo).toList();
    }
}
