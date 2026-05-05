package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.ProjectSearchQuery;
import com.example.backendApi.Dto.Vo.ProjectVo;

import java.util.List;

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
}
