package com.example.backedapi.Service;

import com.example.backedapi.Dto.Vo.FunctionVo;
import com.example.backedapi.Dto.Vo.UserVo;

import java.util.List;

public interface IUserService {
    UserVo createUser(UserVo user);

    List<UserVo> getUser();

    List<UserVo> getUserByEmail(String email);

    UserVo getOnlyUserByEmail(String email);

    UserVo getUserById(String id);

    UserVo saveUser(UserVo user);

    void saveUserWithRole(UserVo userVo);

    List<FunctionVo> getAllParent(List<String> child);

    UserVo getCurrentUserInfo();

    List<UserVo> getAllUsersVo();
}
