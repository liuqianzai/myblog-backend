package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.AgentEventDTO;
import com.liuyang.myblog.domain.vo.AgentPetBubbleVO;
import com.liuyang.myblog.domain.vo.AgentSuggestActionVO;
import com.liuyang.myblog.domain.vo.ArticleSimpleVO;
import com.liuyang.myblog.service.impl.PetReactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetReactionServiceTest {

    @Mock
    private AgentRecommendService agentRecommendService;

    private PetReactionService petReactionService;

    @BeforeEach
    void setUp() {
        petReactionService = new PetReactionServiceImpl(agentRecommendService);
    }

    @Test
    @DisplayName("测试自定义互动-运势计算场景：生成顺应卦象建议与纯文本按钮")
    void testCustomFortuneDivination() {
        ArticleSimpleVO mockArticle = new ArticleSimpleVO();
        mockArticle.setId(100L);
        mockArticle.setTitle("程序员如何对抗焦虑与技术瓶颈");
        mockArticle.setSummary("面对转机与低谷时的心境修炼指南");

        when(agentRecommendService.getArticlesByMetaTags(anyList(), isNull(), eq(1)))
                .thenReturn(List.of(mockArticle));

        AgentEventDTO event = new AgentEventDTO();
        event.setEventType("ACTION_CUSTOM");
        event.setScene("FORTUNE_PAGE");
        event.setMetaTags(List.of("转机", "自强不息"));
        event.setSummary("摇得未济卦，事未成，充满转机");
        event.setVisitorSessionId("session_test_001");

        Map<String, Object> payload = new HashMap<>();
        payload.put("divinationName", "未济卦");
        payload.put("omen", "未成转机");
        event.setPayload(payload);

        AgentPetBubbleVO bubble = petReactionService.reactToEvent(event, null, "127.0.0.1");

        assertNotNull(bubble);
        assertTrue(bubble.getBubbleText().contains("未济卦"));
        assertEquals("THINKING", bubble.getPetMood());
        assertNotNull(bubble.getRelatedArticle());
        assertEquals(100L, bubble.getRelatedArticle().getId());

        // 验证纯文本无符号按钮规范
        List<AgentSuggestActionVO> actions = bubble.getSuggestActions();
        assertFalse(actions.isEmpty());
        for (AgentSuggestActionVO action : actions) {
            String label = action.getLabel();
            assertNotNull(label);
            // 验证不包含装饰性符号和常用 Emoji
            assertFalse(label.contains("+"));
            assertFalse(label.contains("↗"));
            assertFalse(label.matches(".*[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+.*"), "按钮文案严禁包含 Emoji");
        }

        // 校验按钮文案符合业务
        assertTrue(actions.stream().anyMatch(a -> "顺应卦象".equals(a.getLabel())));
        assertTrue(actions.stream().anyMatch(a -> "与助手聊聊".equals(a.getLabel())));
        assertTrue(actions.stream().anyMatch(a -> "收起气泡".equals(a.getLabel())));
    }

    @Test
    @DisplayName("测试防打扰频控：被动进入页面短时间内不重复弹气泡")
    void testPassiveEventCooldown() {
        ArticleSimpleVO mockArticle = new ArticleSimpleVO();
        mockArticle.setId(1L);
        mockArticle.setTitle("Spring Boot 3 深度解析");

        when(agentRecommendService.getExploreArticles(eq(1)))
                .thenReturn(List.of(mockArticle));

        AgentEventDTO event = new AgentEventDTO();
        event.setEventType("PAGE_ENTER");
        event.setScene("HOME");
        event.setVisitorSessionId("session_cooldown_001");

        // 第一次访问：应该弹出气泡
        AgentPetBubbleVO firstBubble = petReactionService.reactToEvent(event, null, "127.0.0.1");
        assertNotNull(firstBubble);

        // 短时间内第二次被动访问：应当被频控拦截返回 null
        AgentPetBubbleVO secondBubble = petReactionService.reactToEvent(event, null, "127.0.0.1");
        assertNull(secondBubble);
    }

    @Test
    @DisplayName("测试权限隔离：未登录访客严禁触发管理员操作建议")
    void testSecurityIsolationForGuest() {
        AgentEventDTO event = new AgentEventDTO();
        event.setEventType("ADMIN_POST_SAVED");
        event.setScene("ARTICLE_EDIT");
        event.setTargetId(10L);

        // 未登录游客触发发文动作：应该被安全阻断返回 null
        AgentPetBubbleVO guestBubble = petReactionService.reactToEvent(event, null, "127.0.0.1");
        assertNull(guestBubble);

        // 管理员(currentUserId = 1)触发发文动作：返回管理类纯文本按钮
        AgentPetBubbleVO adminBubble = petReactionService.reactToEvent(event, 1L, "127.0.0.1");
        assertNotNull(adminBubble);
        assertTrue(adminBubble.getSuggestActions().stream().anyMatch(a -> "推送QQ群".equals(a.getLabel())));
        assertTrue(adminBubble.getSuggestActions().stream().anyMatch(a -> "设为置顶".equals(a.getLabel())));
        assertTrue(adminBubble.getSuggestActions().stream().anyMatch(a -> "查看前台".equals(a.getLabel())));
    }
}
