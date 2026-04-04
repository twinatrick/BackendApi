package com.example.backedapi.controller;

import com.example.backedapi.Service.ISkillService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.Dto.Vo.ResponseType;
import com.example.backedapi.Dto.Vo.SkillVo;
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
}
