package com.example.backedapi.controller;

import com.example.backedapi.Service.SkillService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.openapi.ApiOperationOk;
import com.example.backedapi.model.db.Skill;
import com.example.backedapi.model.Vo.ResponseType;
import com.example.backedapi.model.Vo.SkillVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/backend/skill")
@ApiControllerTag(name = "Skills", description = "Backend API endpoints - Skill management")
public class SkillController {
    private SkillService skillService;
    @PostMapping("/add")
    @ApiOperationBadRequest(summary = "Add skill", description = "Creates a new skill.")
    public ResponseType<SkillVo> addSkill(@RequestBody Skill skill) {
        try {
            SkillVo  data=   skillService.addSkill(skill).toVo();
            return new ResponseType<>( 0,data);
        }catch (Exception e){
            return new ResponseType<>( -1,null,"Error adding skill");
        }


    }
    @GetMapping("/get")
    @ApiOperationOk(summary = "Get skills", description = "Returns all skills.")
    public ResponseType<List<SkillVo>> getSkill() {
        return new ResponseType<>( 0,skillService.getSkill().stream().map(Skill::toVo).toList());
    }
    @PostMapping("/update")
    @ApiOperationBadRequest(summary = "Update skill", description = "Updates an existing skill.")
    public ResponseType<String> updateSkill(@RequestBody Skill skill) {
        try {
            skillService.updateSkill(skill);
        }catch (Exception e){
            return new ResponseType<>( -1,"Error updating skill");
        }

        return new ResponseType<>( 0,"Skill updated successfully");
    }

    @PostMapping("/delete")
    @ApiOperationBadRequest(summary = "Delete skill", description = "Deletes a skill.")
    public ResponseType<String> deleteSkill(@RequestBody Skill skill) {
        try {
            skillService.deleteSkill(skill);
        }catch (Exception e){
            return new ResponseType<>( -1,"Error deleting skill");
        }

        return new ResponseType<>( 0,"Skill deleted successfully");
    }
}
