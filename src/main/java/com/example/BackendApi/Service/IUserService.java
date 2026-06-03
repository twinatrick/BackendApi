package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.FunctionVo;
import com.example.BackendApi.Dto.Vo.UserVo;
import com.example.BackendApi.Dto.Vo.dto.common.PageResult;
import com.example.BackendApi.Dto.Vo.dto.search.UserSearchQuery;

import java.util.List;
import java.util.UUID;

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

    void rebindUserProjects(UUID userId, List<UUID> projectIds);
    
    /**
     * 分頁搜尋使用者
     * 
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<UserVo> searchUsers(UserSearchQuery query);
}

