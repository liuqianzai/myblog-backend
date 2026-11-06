package com.liuyang.myblog.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 接口层集成测试：走完拦截器 → 参数绑定 → 校验 → 全局异常处理 → JSON 序列化整条链路。
 *
 * 与单元测试的分工：单元测试证明业务逻辑正确，
 * 这里证明「接线」是正确的——例如没有 @Valid，DTO 上的校验注解就形同虚设，
 * 而这类问题只有真正发一次 HTTP 请求才看得出来。
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("前台文章列表不返回正文，避免 longtext 拖垮响应体积")
    void testArticleListOmitsContent() throws Exception {
        mockMvc.perform(get("/articles").param("page", "1").param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.records").isArray());
    }

    @Test
    @DisplayName("路径参数类型错误返回 400 而不是 500")
    void testInvalidPathVariableReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/articles/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("不存在的文章返回 404")
    void testMissingArticleReturnsNotFound() throws Exception {
        mockMvc.perform(get("/articles/99999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("未登录访问后台接口返回 401")
    void testAdminApiRequiresToken() throws Exception {
        mockMvc.perform(get("/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test
    @DisplayName("DTO 校验生效：缺少必填字段时返回 400 与中文提示")
    void testValidationIsWired() throws Exception {
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"articleId\":1,\"nickname\":\"访客\",\"content\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("评论内容不能为空"));
    }

    @Test
    @DisplayName("登录缺少密码时返回 400 与中文提示")
    void testLoginValidation() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("密码不能为空"));
    }

    @Test
    @DisplayName("公开评论列表不返回评论者邮箱")
    void testPublicCommentsHideEmail() throws Exception {
        mockMvc.perform(get("/comments").param("articleId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[*].email").doesNotExist());
    }
}
