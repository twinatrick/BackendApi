package com.example.BackendApi.Service;

import com.example.BackendApi.Dto.Vo.PersonalProjectRequest;
import com.example.BackendApi.Dto.Vo.ProjectSkillVo;
import com.example.BackendApi.Dto.Vo.ProjectVo;
import com.example.BackendApi.Dto.Vo.Search.ProjectSearchQuery;
import com.example.BackendApi.Dto.Vo.Common.PageResult;

import java.util.List;
import java.util.Map;
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
     * 獲取專案綁定的所有技能資訊
     * 
     * @param projectId 專案 ID
     * @return 技能資訊列表
     */
    List<ProjectSkillVo> getProjectSkills(UUID projectId);

    /**
     * 獲取個人專案綁定的所有技能資訊
     *
     * @param projectId 專案 ID
     * @return 技能資訊列表
     */
    List<ProjectSkillVo> getPersonalProjectSkills(UUID projectId);
    
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

    /**
     * 綁定技能到個人可操作的專案（每個技能僅能綁定一個等級）
     *
     * @param projectId 專案 ID
     * @param skillId 技能 ID
     * @param skillLevelId 技能等級 ID
     */
    void bindPersonalProjectSkill(UUID projectId, UUID skillId, UUID skillLevelId);

    /**
     * 更新個人可操作專案中的技能等級綁定
     *
     * @param projectId 專案 ID
     * @param skillId 技能 ID
     * @param skillLevelId 技能等級 ID
     */
    void updatePersonalProjectSkillLevel(UUID projectId, UUID skillId, UUID skillLevelId);

    /**
     * 解除個人可操作專案中的技能綁定
     *
     * @param projectId 專案 ID
     * @param skillId 技能 ID
     */
    void unbindPersonalProjectSkill(UUID projectId, UUID skillId);

    void rebindProjectSkills(UUID projectId, Map<UUID, UUID> skillLevelMapping);

    void rebindPersonalProjectSkills(UUID projectId, Map<UUID, UUID> skillLevelMapping);
}
