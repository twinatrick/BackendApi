package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.FunctionVo;
import com.example.backendApi.Dto.Vo.UserVo;
import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.UserSearchQuery;

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

    void bindUserProject(String userId, String projectId);
    
    /**
     * 分頁搜尋使用者
     * 
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<UserVo> searchUsers(UserSearchQuery query);
}

