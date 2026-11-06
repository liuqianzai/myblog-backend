package com.liuyang.myblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liuyang.myblog.common.AuthContext;
import com.liuyang.myblog.domain.po.BlogUser;
import com.liuyang.myblog.mapper.BlogUserMapper;
import com.liuyang.myblog.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthInterceptorTest {

    private static final String TOKEN = "valid-token";
    private static final Long USER_ID = 2L;

    @Mock
    private AuthService authService;

    @Mock
    private BlogUserMapper blogUserMapper;

    private AuthInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new AuthInterceptor(authService, blogUserMapper, new ObjectMapper());
    }

    @Test
    @DisplayName("无令牌访问公开读接口放行，访问后台接口返回 401")
    void testAnonymousRequests() throws Exception {
        when(authService.verifyToken(any())).thenReturn(null);

        MockHttpServletResponse ok = new MockHttpServletResponse();
        assertTrue(interceptor.preHandle(get("/articles"), ok, new Object()));

        MockHttpServletResponse reject = new MockHttpServletResponse();
        when(authService.verifyToken(any())).thenReturn(null);
        assertFalse(interceptor.preHandle(get("/admin/dashboard/stats"), reject, new Object()));
        assertEquals(401, reject.getStatus());
    }

    @Test
    @DisplayName("普通角色用户访问后台接口返回 403")
    void testNonAdminCannotAccessAdminApi() throws Exception {
        when(authService.verifyToken(TOKEN)).thenReturn(USER_ID);
        when(blogUserMapper.selectById(USER_ID)).thenReturn(user("editor", 1));

        MockHttpServletRequest request = get("/admin/dashboard/stats");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("管理员访问后台接口放行并写入当前用户 ID")
    void testAdminCanAccessAdminApi() throws Exception {
        when(authService.verifyToken(TOKEN)).thenReturn(USER_ID);
        when(blogUserMapper.selectById(USER_ID)).thenReturn(user("admin", 1));

        MockHttpServletRequest request = get("/admin/dashboard/stats");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals(USER_ID, request.getAttribute(AuthContext.USER_ID_ATTRIBUTE));
    }

    @Test
    @DisplayName("普通角色用户不能在公开写接口上修改站点数据")
    void testNonAdminCannotWriteSiteData() throws Exception {
        when(authService.verifyToken(TOKEN)).thenReturn(USER_ID);
        when(blogUserMapper.selectById(USER_ID)).thenReturn(user("editor", 1));

        MockHttpServletRequest request = post("/configs");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(403, response.getStatus());
    }

    @Test
    @DisplayName("普通角色用户仍可发表评论、修改自己的密码")
    void testNonAdminCanUseSelfServiceApis() throws Exception {
        when(authService.verifyToken(TOKEN)).thenReturn(USER_ID);
        when(blogUserMapper.selectById(USER_ID)).thenReturn(user("editor", 1));

        MockHttpServletRequest commentRequest = post("/comments");
        commentRequest.addHeader("Authorization", "Bearer " + TOKEN);
        assertTrue(interceptor.preHandle(commentRequest, new MockHttpServletResponse(), new Object()));

        MockHttpServletRequest passwordRequest = post("/auth/password");
        passwordRequest.addHeader("Authorization", "Bearer " + TOKEN);
        assertTrue(interceptor.preHandle(passwordRequest, new MockHttpServletResponse(), new Object()));
    }

    @Test
    @DisplayName("令牌有效但账号已停用时拒绝访问")
    void testDisabledUserIsRejected() throws Exception {
        when(authService.verifyToken(TOKEN)).thenReturn(USER_ID);
        when(blogUserMapper.selectById(USER_ID)).thenReturn(user("admin", 0));

        MockHttpServletRequest request = get("/articles");
        request.addHeader("Authorization", "Bearer " + TOKEN);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertFalse(interceptor.preHandle(request, response, new Object()));
        assertEquals(401, response.getStatus());
    }

    private MockHttpServletRequest get(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", path);
        request.setServletPath(path);
        return request;
    }

    private MockHttpServletRequest post(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", path);
        request.setServletPath(path);
        return request;
    }

    private BlogUser user(String role, int status) {
        BlogUser user = new BlogUser();
        user.setId(USER_ID);
        user.setUsername("tester");
        user.setRole(role);
        user.setStatus(status);
        return user;
    }
}
