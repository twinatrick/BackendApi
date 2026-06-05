package com.example.BackendApi.Controller;

import com.example.BackendApi.Service.IRoleService;
import com.example.BackendApi.Service.IUserService;
import com.example.BackendApi.Annotation.OpenApi.ApiControllerTag;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationAuth;
import com.example.BackendApi.Annotation.OpenApi.ApiOperationBadRequest;
import com.example.BackendApi.Annotation.Ingnore;
import com.example.BackendApi.Exception.AppException;
import com.example.BackendApi.Filter.JwtAuthenticationToken;
import com.example.BackendApi.Dto.Vo.LoginRequest;
import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Dto.Vo.RoleOutVo;
import com.example.BackendApi.Dto.Vo.SignupRequest;
import com.example.BackendApi.Dto.Vo.SuperUserRequest;
import com.example.BackendApi.Dto.Vo.UserVo;
import jakarta.servlet.http.HttpServletResponse;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.example.BackendApi.Dto.Response.Token;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;

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

    @Autowired
    private JwtAuthenticationToken jwtUtils;

    @Autowired
    private AuthenticationManager authenticationManager;

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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException e) {
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
