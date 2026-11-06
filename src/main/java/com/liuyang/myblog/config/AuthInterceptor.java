package com.liuyang.myblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.AuthContext;
import com.liuyang.myblog.domain.po.BlogUser;
import com.liuyang.myblog.mapper.BlogUserMapper;
import com.liuyang.myblog.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final BlogUserMapper blogUserMapper;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, BlogUserMapper blogUserMapper, ObjectMapper objectMapper) {
        this.authService = authService;
        this.blogUserMapper = blogUserMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean isPublic = isPublicRequest(request);

        String header = request.getHeader("Authorization");
        String token = null;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        }
        Long userId = authService.verifyToken(token);
        if (userId != null) {
            // 先确认账号仍然有效：令牌签发后账号可能已被停用，
            // 只验签不查状态会让停用操作在令牌过期前完全失效。
            BlogUser user = blogUserMapper.selectById(userId);
            if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                return reject(response, 401, "unauthorized");
            }
            // 后台管理接口（以及配置的写操作）必须是管理员角色。
            // 原来只要令牌有效，任何登录用户都能调用 /admin/**，等于越权。
            if (requiresAdmin(request) && !isAdmin(user)) {
                return reject(response, 403, "forbidden");
            }
            request.setAttribute(AuthContext.TOKEN_ATTRIBUTE, token);
            request.setAttribute(AuthContext.USER_ID_ATTRIBUTE, userId);
            return true;
        }

        if (isPublic) {
            return true;
        }

        return reject(response, 401, "unauthorized");
    }

    /**
     * 哪些请求必须管理员身份。
     */
    private boolean requiresAdmin(HttpServletRequest request) {
        String path = resolvePath(request);
        if (path.startsWith("/admin/")) {
            return true;
        }
        if (HttpMethod.GET.matches(request.getMethod())) {
            return false;
        }
        // 除下面这些「登录用户自己就能做的事」之外，所有写操作都要求管理员：
        // 分类、标签、文章标签关系、站点配置、文件上传都属于站点数据维护，
        // 不该让任何一个能登录的账号都能改。
        return !path.equals("/auth/logout")
                && !path.equals("/auth/password")
                && !path.equals("/comments")
                && !path.equals("/agent/pet/bubble");
    }

    /**
     * 取出去掉 context-path 的请求路径。
     *
     * 不能只用 getServletPath()：在 MockMvc 等不走完整 Servlet 容器派发的场景下
     * 它可能是空串，导致所有请求都被判定成「非公开路径」而统一返回 401。
     * 这里以 getRequestURI 减去 contextPath 为准，getServletPath 作为兜底。
     */
    private String resolvePath(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        if (servletPath != null && !servletPath.isEmpty()) {
            return servletPath;
        }
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (requestUri == null) {
            return "";
        }
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }

    private boolean isAdmin(BlogUser user) {
        return user.getRole() != null && "admin".equalsIgnoreCase(user.getRole().trim());
    }

    private boolean reject(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(code);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.fail(code, message)));
        return false;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        String path = resolvePath(request);
        if (path.equals("/comments") && HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }
        // 小宠物气泡行为上报接口允许访客免密调用
        if (path.equals("/agent/pet/bubble") && HttpMethod.POST.matches(request.getMethod())) {
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
                || path.startsWith("/pages/")
                || path.startsWith("/agent/recommend/");
    }
}
