package com.example.backendApi.Aop;

import com.example.backendApi.Dto.Vo.ResponseType;
import com.example.backendApi.Entity.User;
import com.example.backendApi.Entity.UserRole;
import com.example.backendApi.annotation.RequireRole;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class RoleCheck {

    @Around("execution(* com.example.backendApi.controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RequireRole requireRole = resolveRequireRole(joinPoint);
        if (requireRole == null) {
            return joinPoint.proceed();
        }

        ServletRequestAttributes attributes = getServletRequestAttributes();
        HttpServletResponse response = attributes.getResponse();
        User user = (User) attributes.getRequest().getAttribute("user");

        if (user == null) {
            if (response != null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
            }
            return ResponseType.Fail("AUTH_ERROR", "Unauthorized", 401);
        }

        Set<String> actualRoles = user.getRoles() == null
                ? Set.of()
                : user.getRoles().stream()
                .map(UserRole::getRole)
                .filter(Objects::nonNull)
                .map(role -> role.getName() == null ? "" : role.getName().trim().toLowerCase())
                .collect(Collectors.toSet());

        Set<String> requiredRoles = Arrays.stream(requireRole.value())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean matched = requiredRoles.stream().anyMatch(actualRoles::contains);
        if (matched) {
            return joinPoint.proceed();
        }

        if (response != null) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
        return ResponseType.Fail("FORBIDDEN", "Forbidden", 403);
    }

    private RequireRole resolveRequireRole(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole methodAnnotation = method.getAnnotation(RequireRole.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequireRole classAnnotation = AnnotationUtils.findAnnotation(targetClass, RequireRole.class);
        if (classAnnotation != null) {
            return classAnnotation;
        }

        return AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequireRole.class);
    }

    private ServletRequestAttributes getServletRequestAttributes() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new IllegalStateException("Servlet request attributes not found");
        }
        return servletRequestAttributes;
    }
}
