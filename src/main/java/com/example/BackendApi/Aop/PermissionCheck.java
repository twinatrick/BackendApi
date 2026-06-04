package com.example.BackendApi.Aop;

import com.example.BackendApi.Annotation.RequirePermission;
import com.example.BackendApi.Dto.Vo.FunctionVo;
import com.example.BackendApi.Dto.Vo.ResponseType;
import com.example.BackendApi.Entity.RoleFunction;
import com.example.BackendApi.Entity.User;
import com.example.BackendApi.Service.IFunctionService;
import com.example.BackendApi.Service.IInitAndCheckService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class PermissionCheck {

    private final IInitAndCheckService initAndCheckService;
    private final IFunctionService functionService;

    @Around("execution(* com.example.BackendApi.Controller..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RequirePermission requirePermission = resolveRequirePermission(joinPoint);
        if (requirePermission == null) {
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

        List<String> permissionPath = Arrays.stream(requirePermission.value())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();

        boolean matched = permissionPath.size() == 3 && hasPermission(user, permissionPath);
        if (matched) {
            return joinPoint.proceed();
        }

        if (response != null) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
        }
        return ResponseType.Fail("FORBIDDEN", "Forbidden", 403);
    }

    private boolean hasPermission(User user, List<String> permissionPath) {
        String oneLayer = permissionPath.get(0);
        String twoLayer = permissionPath.get(1);
        String threeLayer = permissionPath.get(2);
        if (!initAndCheckService.checkIsExist(oneLayer, twoLayer, threeLayer)) {
            return false;
        }

        FunctionVo one = functionService.getFunctionByName(oneLayer);
        FunctionVo two = functionService.getFunctionByNameAndParent(twoLayer, one.getId());
        FunctionVo three = functionService.getFunctionByNameAndParent(threeLayer, two.getId());
        String requiredFunctionId = three.getId();

        return user.getRoles() != null && user.getRoles().stream()
                .filter(Objects::nonNull)
                .map(userRole -> userRole.getRole())
                .filter(Objects::nonNull)
                .filter(role -> role.getRoleFunctions() != null)
                .flatMap(role -> role.getRoleFunctions().stream())
                .filter(Objects::nonNull)
                .map(RoleFunction::getFunction)
                .filter(Objects::nonNull)
                .anyMatch(function -> requiredFunctionId.equals(function.getId().toString()));
    }

    private RequirePermission resolveRequirePermission(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission methodAnnotation = method.getAnnotation(RequirePermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        Class<?> targetClass = joinPoint.getTarget().getClass();
        RequirePermission classAnnotation = AnnotationUtils.findAnnotation(targetClass, RequirePermission.class);
        if (classAnnotation != null) {
            return classAnnotation;
        }

        return AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequirePermission.class);
    }

    private ServletRequestAttributes getServletRequestAttributes() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new IllegalStateException("Servlet request attributes not found");
        }
        return servletRequestAttributes;
    }
}
