package com.example.backendApi.Aop;

import com.example.backendApi.dataaccess.IUserDataAccess;
import com.example.backendApi.filter.JwtAuthenticationToken;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Dto.Vo.ResponseType;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Objects;
@Aspect
@Order(1)
@Component
public class TokenCheck {

    @Autowired
    private  HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;
    @Autowired
    private JwtAuthenticationToken jwtAuthenticationToken;
    @Autowired
    private IUserDataAccess userDataAccess;
    @Pointcut("execution(* com.example.backendApi.controller.*.*(..))")
    void pointcut(){
    }


    @Pointcut("@annotation(com.example.backendApi.annotation.Ingnore)")
    void ignoreAuthorize(){}
    @Pointcut("execution(@org.springframework.web.bind.annotation.*Mapping * *(..))")
    void requestMappingPointcut(){}
    @Around("pointcut() && !ignoreAuthorize()")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        try {

            String token = resolveToken();
            if (token == null || token.isBlank()) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return ResponseType.Fail("AUTH_ERROR", "Unauthorized", 401);
            }
            token = token.replaceFirst("^Bearer\\s+", "").trim();
            JwtClaims claims = jwtAuthenticationToken.verifyJWT(token);
            String email = (String) claims.getClaimValue("email");
            User user = userDataAccess.findByEmail(email).stream().findFirst().orElse(null);
            if (user == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return ResponseType.Fail("AUTH_ERROR", "Unauthorized", 401);
            }
            request.setAttribute("user", user);

        } catch (InvalidJwtException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return ResponseType.Fail("AUTH_ERROR", "Unauthorized", 401);
        } catch (Throwable e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return ResponseType.Fail("INTERNAL_ERROR", "Internal server error", 500);
        }
        return pjp.proceed();
    }

    private String resolveToken() {
        String header = request.getHeader("Authorization");
        if (header != null && !header.isBlank()) {
            return header;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && Objects.equals(cookie.getName(), "v3-admin-vite-token-key")) {
                return cookie.getValue();
            }
        }
        return null;
    }


}
