package com.example.backendApi.Service;

import com.example.backendApi.Dto.Vo.dto.common.PageResult;
import com.example.backendApi.Dto.Vo.dto.search.SkillLevelSearchQuery;
import com.example.backendApi.Dto.Vo.dto.search.SkillSearchQuery;
import com.example.backendApi.Dto.Vo.CurrentUserSkillVo;
import com.example.backendApi.Dto.Vo.SkillVo;
import com.example.backendApi.Dto.Vo.SkillLevelVo;

import java.util.List;

public interface ISkillService {
    SkillVo addSkill(SkillVo skillVo);

    void updateSkill(SkillVo skillVo);

    List<SkillVo> getSkill();

    SkillLevelVo addSkillLevel(SkillLevelVo skillLevelVo);

    void updateSkillLevel(SkillLevelVo skillLevelVo);

    List<SkillLevelVo> getSkillLevels(String skillId);

    void deleteSkillLevel(String skillLevelId);

    void bindUserSkill(String userId, String skillId, String skillLevelId);

    void bindProjectSkill(String projectId, String skillId, String skillLevelId);

    void deleteSkill(SkillVo skillVo);
    
    /**
     * 搜尋所有技能（支援分頁與條件查詢）
     *
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<SkillVo> searchSkills(SkillSearchQuery query);
    
    /**
     * 取得當前使用者技能（合併 USER 直接綁定 + PROJECT 專案技能）
     *
     * @return 技能列表（含來源標記）
     */
    List<CurrentUserSkillVo> getCurrentUserSkills();
    
    /**
     * 搜尋當前使用者技能（支援分頁與條件查詢）
     *
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<CurrentUserSkillVo> searchCurrentUserSkills(SkillSearchQuery query);
    
    /**
     * 搜尋技能等級（支援分頁與條件查詢）
     *
     * @param query 搜尋查詢參數
     * @return 分頁結果
     */
    PageResult<SkillLevelVo> searchSkillLevels(SkillLevelSearchQuery query);
}
