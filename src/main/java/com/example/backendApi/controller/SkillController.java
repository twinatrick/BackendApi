package com.example.backendApi.controller;

import com.example.backendApi.Dto.dto.common.PageResult;
import com.example.backendApi.Dto.dto.search.SkillLevelSearchQuery;
import com.example.backendApi.Dto.dto.search.SkillSearchQuery;
import com.example.backendApi.Service.ISkillService;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import com.example.backendApi.annotation.openapi.ApiOperationOk;
import com.example.backendApi.Dto.Vo.CurrentUserSkillVo;
import com.example.backendApi.Dto.Vo.ResponseType;
import com.example.backendApi.Dto.Vo.SkillLevelVo;
import com.example.backendApi.Dto.Vo.SkillVo;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/backend/skill")
@ApiControllerTag(name = "Skills", description = "Backend API endpoints - Skill management")
public class SkillController {
    private final ISkillService skillService;

    public SkillController(ISkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add skill", description = "Creates a new skill.")
    public ResponseType<SkillVo> addSkill(@RequestBody SkillVo skill) {
        return ResponseType.Success(skillService.addSkill(skill), "Skill added successfully");
    }

    @GetMapping("/get")
    @ApiOperationOk(summary = "Get skills", description = "Returns all skills.")
    public ResponseType<List<SkillVo>> getSkill() {
        return ResponseType.Success(skillService.getSkill(), "Skills fetched successfully");
    }

    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update skill", description = "Updates an existing skill.")
    public ResponseType<String> updateSkill(@RequestBody SkillVo skill) {
        skillService.updateSkill(skill);
        return ResponseType.Success("Skill updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete skill", description = "Deletes a skill.")
    public ResponseType<String> deleteSkill(@RequestBody SkillVo skill) {
        skillService.deleteSkill(skill);
        return ResponseType.Success("Skill deleted successfully");
    }

    @PostMapping("/level/add")
    @ApiOperationBadRequest(summary = "Add skill level", description = "Creates a level under a specific skill.")
    public ResponseType<SkillLevelVo> addSkillLevel(@RequestBody SkillLevelVo skillLevelVo) {
        return ResponseType.Success(skillService.addSkillLevel(skillLevelVo), "Skill level added successfully");
    }

    @GetMapping("/level/get/{skillId}")
    @ApiOperationOk(summary = "Get skill levels", description = "Returns all levels for a skill.")
    public ResponseType<List<SkillLevelVo>> getSkillLevels(@PathVariable String skillId) {
        return ResponseType.Success(skillService.getSkillLevels(skillId), "Skill levels fetched successfully");
    }

    @PostMapping("/level/update")
    @ApiOperationBadRequest(summary = "Update skill level", description = "Updates a skill level.")
    public ResponseType<String> updateSkillLevel(@RequestBody SkillLevelVo skillLevelVo) {
        skillService.updateSkillLevel(skillLevelVo);
        return ResponseType.Success("Skill level updated successfully");
    }

    @PostMapping("/level/delete")
    @ApiOperationBadRequest(summary = "Delete skill level", description = "Deletes a skill level.")
    public ResponseType<String> deleteSkillLevel(@RequestBody SkillLevelVo skillLevelVo) {
        skillService.deleteSkillLevel(skillLevelVo.getId());
        return ResponseType.Success("Skill level deleted successfully");
    }
    
    @PostMapping("/search")
    @ApiOperationBadRequest(summary = "搜尋技能", description = "支援分頁與條件查詢的技能搜尋")
    @Operation(summary = "搜尋技能（分頁）", description = "支援 name、description、createdBy 查詢條件，預設按 createdTime 降序排序")
    public ResponseType<PageResult<SkillVo>> searchSkills(@RequestBody SkillSearchQuery query) {
        return ResponseType.Success(skillService.searchSkills(query), "技能查詢成功");
    }
    
    @GetMapping("/current")
    @ApiOperationOk(summary = "取得當前使用者技能", description = "返回當前使用者所有技能（合併 USER 直接綁定與 PROJECT 專案技能）")
    @Operation(summary = "取得當前使用者技能", description = "合併 USER（直接綁定）和 PROJECT（專案技能）兩個來源，每筆標記 sourceType")
    public ResponseType<List<CurrentUserSkillVo>> getCurrentUserSkills() {
        return ResponseType.Success(skillService.getCurrentUserSkills(), "當前使用者技能查詢成功");
    }
    
    @PostMapping("/current/search")
    @ApiOperationBadRequest(summary = "搜尋當前使用者技能", description = "支援分頁與條件查詢的當前使用者技能搜尋")
    @Operation(summary = "搜尋當前使用者技能（分頁）", description = "在合併後的技能列表中搜尋，支援 name、description、createdBy 查詢條件")
    public ResponseType<PageResult<CurrentUserSkillVo>> searchCurrentUserSkills(@RequestBody SkillSearchQuery query) {
        return ResponseType.Success(skillService.searchCurrentUserSkills(query), "當前使用者技能查詢成功");
    }
    
    @PostMapping("/level/search")
    @ApiOperationBadRequest(summary = "搜尋技能等級", description = "支援分頁與條件查詢的技能等級搜尋")
    @Operation(summary = "搜尋技能等級（分頁）", description = "支援 skillId、levelValue、title、description、createdBy 查詢條件，預設按 createdTime 降序排序")
    public ResponseType<PageResult<SkillLevelVo>> searchSkillLevels(@RequestBody SkillLevelSearchQuery query) {
        return ResponseType.Success(skillService.searchSkillLevels(query), "技能等級查詢成功");
    }
}
