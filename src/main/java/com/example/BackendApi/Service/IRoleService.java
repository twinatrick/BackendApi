package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.RoleOutVo;
import com.example.BackendApi.Dto.Vo.FunctionVo;
import com.example.BackendApi.Dto.Vo.UserVo;
import com.example.BackendApi.Dto.Vo.Search.RoleSearchQuery;
import com.example.BackendApi.Dto.Vo.common.PageResult;

import java.util.List;

public interface IRoleService {
    RoleOutVo addRole(RoleOutVo role);

    List<RoleOutVo> getRole();

    RoleOutVo getRoleById(String roleId);

    RoleOutVo updateRole(RoleOutVo role);

    void deleteRole(RoleOutVo role);

    void roleBindFunction(String roleId, List<String> functionIds);

    void functionBindRole(String functionId, List<String> roleIds);

    void roleBindUser(String roleId, List<String> userIds);

    void userBindRole(String userId, List<String> roleIds);

    void roleUnbindUser(String roleId, List<String> userIds);

    void userUnbindRole(String userId, List<String> roleIds);

    void userUnbindAllRole(String userId);

    void roleUnbindFunction(String roleId, List<String> functionIds);

    void functionUnbindRole(String functionId, List<String> roleIds);

    List<FunctionVo> getFunctionByRole(String roleId);

    List<RoleOutVo> getRoleByFunction(String functionId);

    List<UserVo> getUserByRole(String roleId);

    List<RoleOutVo> getRoleByUser(String userId);

    RoleOutVo getRoleByName(String name);
    
    /**
     * 分頁搜尋角色
     * 
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<RoleOutVo> searchRoles(RoleSearchQuery query);
}

