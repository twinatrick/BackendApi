package com.example.BackendArchitectureLab.Feign;

import com.example.BackendArchitectureLab.Dto.Vo.RoleOutVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "iam-service")
public interface PermissionServiceFeignClient {

    @GetMapping("/role/inner/all")
    List<RoleOutVo> getAllRoles();

    @GetMapping("/role/inner/by-name/{name}")
    RoleOutVo getRoleByName(@PathVariable("name") String name);

    @PostMapping("/role/inner/user-bind-role")
    void userBindRole(@RequestParam("userId") String userId, @RequestParam("roleId") String roleId);

    @PostMapping("/role/add")
    RoleOutVo addRole(@RequestBody RoleOutVo role);
}
