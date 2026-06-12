package com.example.BackendArchitectureLab.Controller;

import com.example.BackendArchitectureLab.Dto.Vo.SkillLevelVo;
import com.example.BackendArchitectureLab.Dto.Vo.SkillVo;
import com.example.BackendArchitectureLab.Service.ISkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/skill/inner")
@RequiredArgsConstructor
public class SkillInternalController {
    private final ISkillService skillService;

    @GetMapping("/{id}")
    public SkillVo getSkillById(@PathVariable Long id) {
        return skillService.getSkillById(id);
    }

    @GetMapping("/level/{id}")
    public SkillLevelVo getSkillLevelById(@PathVariable Long id) {
        return skillService.getSkillLevelById(id);
    }

    @PostMapping("/bind-project")
    public void bindProjectSkill(@RequestParam("projectId") String projectId,
                                 @RequestParam("skillId") String skillId,
                                 @RequestParam("skillLevelId") String skillLevelId) {
        skillService.bindProjectSkill(projectId, skillId, skillLevelId);
    }

    @PostMapping("/rebind-user-skills")
    public void rebindUserSkills(@RequestParam("userId") UUID userId,
                                 @RequestBody Map<UUID, UUID> skillLevelMap) {
        skillService.rebindUserSkills(userId, skillLevelMap);
    }
}
