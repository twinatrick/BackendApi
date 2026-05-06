package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.ProjectSearchQuery;
import com.example.backendApi.Dto.Vo.PersonalProjectRequest;
import com.example.backendApi.Dto.Vo.ProjectVo;

import java.util.List;
import java.util.UUID;

public interface IProjectService {
    ProjectVo addProject(ProjectVo projectVo);

    void updateProject(ProjectVo projectVo);

    List<ProjectVo> getProject();

    void deleteProject(ProjectVo projectVo);
    
    /**
     * 分頁搜尋專案
     * 
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<ProjectVo> searchProjects(ProjectSearchQuery query);
    
    /**
     * 取得當前使用者的專案列表
     * 
     * @return 當前使用者的專案列表
     */
    List<ProjectVo> getCurrentUserProjects();
    
    /**
     * 分頁搜尋當前使用者的專案
     * 
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<ProjectVo> searchCurrentUserProjects(ProjectSearchQuery query);
    
    /**
     * 新增個人專案（自動綁定當前使用者）
     *
     * @param request 個人專案請求
     * @return 新增的專案 VO
     */
    ProjectVo addPersonalProject(PersonalProjectRequest request);
    
    /**
     * 修改個人專案（僅限擁有者）
     *
     * @param projectId 專案 ID
     * @param request 個人專案請求
     */
    void updatePersonalProject(UUID projectId, PersonalProjectRequest request);
    
    /**
     * 刪除個人專案（僅限擁有者）
     *
     * @param projectId 專案 ID
     */
    void deletePersonalProject(UUID projectId);
}
