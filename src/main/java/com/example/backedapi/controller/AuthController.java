package com.example.backedapi.controller;

import com.example.backedapi.Service.RoleService;
import com.example.backedapi.Service.UserService;
import com.example.backedapi.annotation.openapi.ApiControllerTag;
import com.example.backedapi.annotation.openapi.ApiOperationAuth;
import com.example.backedapi.annotation.openapi.ApiOperationBadRequest;
import com.example.backedapi.annotation.Ingnore;
import com.example.backedapi.fillter.JwtAuthenticationToken;
import com.example.backedapi.model.db.Role;
import com.example.backedapi.model.db.User;
import com.example.backedapi.model.Vo.LoginRequest;
import com.example.backedapi.model.Vo.ResponseType;
import com.example.backedapi.model.Vo.SignupRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jose4j.lang.JoseException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/backend/auth")
@ApiControllerTag(name = "Auth", description = "Backend API endpoints - Authentication and registration")
public class AuthController {
//    @Autowired
//    private   UserRepository userRepository;
    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private  HttpServletResponse httpResponse;

//    @Autowired
//
//    private  BCryptPasswordEncoder passwordEncoder;


    @Autowired
    private JwtAuthenticationToken jwtUtils;
//    @Autowired
//    private  HttpServletRequest request;

    // 註冊
    @Ingnore
    @PostMapping("/signup")
    @ApiOperationBadRequest(summary = "Register a new user", description = "Creates a user account and returns a JWT access token.")
    public ResponseType<?> signup(@RequestBody SignupRequest request) throws JoseException {
        if (!userService.getUserByEmail(request.getEmail()).isEmpty()) {
            return new ResponseType<>(-1, "User already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setCreatedTime(new Date());
        user.setCreatedBy("system");
        String  token = jwtUtils.generateJWT(request.getEmail() );
        userService.saveUser(user);
        List<Role> roles = roleService.getRoleRestIn();
        roles=roles.stream().filter(role ->
                role.getName().equals("admin")).toList();
        roleService.userBindRole(user,roles);
        httpResponse.addHeader("Authorization", "Bearer " + token);
        HashMap<String,String> res=new HashMap<>();
        res.put("accessToken",token);
        return new ResponseType<>(0, res,"User registered successfully");
//        return ResponseEntity.ok("User registered successfully");
    }

    // 登入

    @Ingnore
    @PostMapping("/login")
    @ApiOperationAuth(summary = "User login", description = "Authenticates user credentials and returns a JWT access token.")
    public ResponseType<?> login(@RequestBody LoginRequest request) throws JoseException {
        List<User> user = userService.getUserByEmail(request.getEmail());
        if (user.isEmpty() || !BCrypt.checkpw(request.getPassword(), user.getFirst().getPassword())) {
            return new  ResponseType<>(-1, "Invalid username or password");
        }

        String token = jwtUtils.generateJWT(request.getEmail());

        httpResponse.addHeader("Authorization", "Bearer " + token);
        HashMap<String,String> res=new HashMap<>();
        res.put("accessToken",token);
        return new ResponseType<>(0, res,"User registered successfully");

    }
}
