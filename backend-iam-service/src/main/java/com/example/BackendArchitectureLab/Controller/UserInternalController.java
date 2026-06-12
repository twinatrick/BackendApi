package com.example.BackendArchitectureLab.Controller;

import com.example.BackendArchitectureLab.Dto.Vo.UserVo;
import com.example.BackendArchitectureLab.Service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/inner")
@RequiredArgsConstructor
public class UserInternalController {
    private final IUserService userService;

    @GetMapping("/{id}")
    public UserVo getUserById(@PathVariable Long id) {
        return userService.getUserById(String.valueOf(id));
    }

    @PostMapping("/by-email")
    public UserVo getUserByEmail(@RequestBody String email) {
        return userService.getOnlyUserByEmail(email);
    }

    @PostMapping("/rebind-projects")
    public void rebindUserProjects(@RequestParam("userId") UUID userId,
                                   @RequestBody List<UUID> projectIds) {
        userService.rebindUserProjects(userId, projectIds);
    }

    @GetMapping("/exists/{id}")
    public boolean existsUserById(@PathVariable Long id) {
        return userService.getUserById(String.valueOf(id)) != null;
    }

    @GetMapping("/by-email-exists")
    public boolean existsUserByEmail(@RequestParam String email) {
        try {
            userService.getOnlyUserByEmail(email);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
