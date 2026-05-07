package com.example.backendApi.controller;

import com.example.backendApi.Dto.Response.Token;
import com.example.backendApi.Service.IRoleService;
import com.example.backendApi.Service.IUserService;
import com.example.backendApi.annotation.openapi.ApiControllerTag;
import com.example.backendApi.annotation.openapi.ApiOperationAuth;
import com.example.backendApi.annotation.openapi.ApiOperationBadRequest;
import com.example.backendApi.annotation.Ingnore;
import com.example.backendApi.exception.AppException;
import com.example.backendApi.filter.JwtAuthenticationToken;
import com.example.backendApi.Dto.Vo.LoginRequest;
import com.example.backendApi.Dto.Vo.ResponseType;
import com.example.backendApi.Dto.Vo.RoleOutVo;
import com.example.backendApi.Dto.Vo.SignupRequest;
import com.example.backendApi.Dto.Vo.SuperUserRequest;
import com.example.backendApi.Dto.Vo.UserVo;
import jakarta.servlet.http.HttpServletResponse;
import org.jose4j.lang.JoseException;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
@ApiControllerTag(name = "Auth", description = "Backend API endpoints - Authentication and registration")
public class AuthController {
//    @Autowired
//    private   UserRepository userRepository;
    @Autowired
    private IUserService userService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private  HttpServletResponse httpResponse;

//    @Autowired
//
//    private  BCryptPasswordEncoder passwordEncoder;


    @Autowired
    private JwtAuthenticationToken jwtUtils;

    @Value("${superuser.key}")
    private String superUserKey;
//    @Autowired
//    private  HttpServletRequest request;

    // 註冊
    @Ingnore
    @PostMapping("/signup")
    @ApiOperationBadRequest(summary = "Register a new user", description = "Creates a user account and returns a JWT access token.")
    public ResponseType<Token> signup(@RequestBody SignupRequest request) throws JoseException {
        if (!userService.getUserByEmail(request.getEmail()).isEmpty()) {
            throw new AppException("VALIDATION_ERROR", "User already exists", 400);
        }

        UserVo user = new UserVo();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        String  token = jwtUtils.generateJWT(request.getEmail() );
        UserVo savedUser = userService.createUser(user);
        List<RoleOutVo> roles = roleService.getRole();
        List<RoleOutVo> defaultRoles = roles.stream()
                .filter(role -> "user".equalsIgnoreCase(role.getName()))
                .toList();
        if (!defaultRoles.isEmpty()) {
            roleService.userBindRole(savedUser.getId(), List.of(defaultRoles.getFirst().getId().toString()));
        }
        httpResponse.addHeader("Authorization", "Bearer " + token);
        Token res=new Token();
        res.setAccessToken(token);
        return new ResponseType<>(0, res,"User registered successfully");
//        return ResponseEntity.ok("User registered successfully");
    }

    // 登入

    @Ingnore
    @PostMapping("/login")
    @ApiOperationAuth(summary = "User login", description = "Authenticates user credentials and returns a JWT access token.")
    public ResponseType<Token> login(@RequestBody LoginRequest request) throws JoseException {
        List<UserVo> user = userService.getUserByEmail(request.getEmail());
        if (user.isEmpty() || !BCrypt.checkpw(request.getPassword(), user.getFirst().getPassword())) {
            throw new AppException("AUTH_ERROR", "Invalid username or password", 401);
        }

        String token = jwtUtils.generateJWT(request.getEmail());

        httpResponse.addHeader("Authorization", "Bearer " + token);
        Token res=new Token();
        res.setAccessToken(token);
        return new ResponseType<>(0, res,"Login successful");

    }

    @Ingnore
    @PostMapping("/superuser")
    @ApiOperationBadRequest(summary = "Create super user", description = "Creates an admin user when the provided key matches configuration.")
    public ResponseType<?> createSuperUser(@RequestBody SuperUserRequest request) {
        if (request.getKey() == null || !request.getKey().equals(superUserKey)) {
            throw new AppException("VALIDATION_ERROR", "Invalid key", 400);
        }
        String email = (request.getEmail() == null || request.getEmail().isBlank()) ? "admin" : request.getEmail();
        if (!userService.getUserByEmail(email).isEmpty()) {
            throw new AppException("VALIDATION_ERROR", "User already exists", 400);
        }
        UserVo user = new UserVo();
        user.setEmail(email);
        user.setPassword("admin");
        UserVo savedUser = userService.createUser(user);

        RoleOutVo adminRole = roleService.getRoleByName("admin");
        if (adminRole == null) {
            RoleOutVo role = new RoleOutVo();
            role.setName("admin");
            adminRole = roleService.addRole(role);
        }
        roleService.userBindRole(savedUser.getId(), List.of(adminRole.getId().toString()));

        HashMap<String, String> res = new HashMap<>();
        res.put("email", email);
        res.put("password", "admin");
        return new ResponseType<>(0, res, "Super user created");
    }
}
