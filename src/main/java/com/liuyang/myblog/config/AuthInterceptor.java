package com.liuyang.myblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.AuthContext;
import com.liuyang.myblog.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("DEBUG - Method: " + request.getMethod() 
                + ", ServletPath: " + request.getServletPath() 
                + ", RequestURI: " + request.getRequestURI() 
                + ", ContextPath: " + request.getContextPath()
                + ", isPublic: " + isPublicRequest(request));
        if (isPublicRequest(request)) {
            return true;
        }

        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        Long userId = authService.verifyToken(token);
        if (userId != null) {
            request.setAttribute(AuthContext.TOKEN_ATTRIBUTE, token);
            request.setAttribute(AuthContext.USER_ID_ATTRIBUTE, userId);
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(401, "unauthorized")));
        return false;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        if (path.equals("/comments") && HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        if (!HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        return path.equals("/articles")
                || path.startsWith("/articles/")
                || path.equals("/archives")
                || path.equals("/article-tags")
                || path.equals("/tags")
                || path.startsWith("/tags/")
                || path.equals("/categories")
                || path.startsWith("/categories/")
                || path.equals("/configs")
                || path.startsWith("/configs/")
                || path.startsWith("/files/")
                || path.equals("/comments")
                || path.equals("/friend-links")
                || path.startsWith("/pages/");
    }
}
