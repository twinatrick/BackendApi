package com.example.BackendArchitectureLab.Controller;

import com.example.BackendArchitectureLab.Dto.Vo.RoleOutVo;
import com.example.BackendArchitectureLab.Service.IRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role/inner")
@RequiredArgsConstructor
public class PermissionInternalController {
    private final IRoleService roleService;

    @GetMapping("/all")
    public List<RoleOutVo> getAllRoles() {
        return roleService.getRole();
    }

    @GetMapping("/by-name/{name}")
    public RoleOutVo getRoleByName(@PathVariable String name) {
        return roleService.getRoleByName(name);
    }

    @PostMapping("/user-bind-role")
    public void userBindRole(@RequestParam String userId, @RequestParam String roleId) {
        roleService.userBindRole(userId, List.of(roleId));
    }
}
