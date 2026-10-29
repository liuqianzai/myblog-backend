package com.liuyang.myblog.service.impl;

import com.liuyang.myblog.domain.dto.AgentEventDTO;
import com.liuyang.myblog.domain.vo.AgentPetBubbleVO;
import com.liuyang.myblog.domain.vo.AgentSuggestActionVO;
import com.liuyang.myblog.domain.vo.ArticleSimpleVO;
import com.liuyang.myblog.service.AgentRecommendService;
import com.liuyang.myblog.service.PetReactionService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能体小宠物行为感知与气泡反应服务实现类
 *
 * 核心设计：
 * 1. 频控防扰：被动浏览事件实施 3 分钟冷却，主动互动事件（如算卦）即时响应；
 * 2. 身份隔离：严格基于登录态隔离管理员管理类动作与普通访客行为；
 * 3. 纯文本按钮规范：所有操作按钮严禁任何 Emoji 或装饰符号。
 */
@Service
public class PetReactionServiceImpl implements PetReactionService {
    private final AgentRecommendService agentRecommendService;

    // 会话级被动气泡冷却记录 (key: visitorSessionId 或 clientIp, value: 上次弹出毫秒时间戳)
    private final Map<String, Long> passiveCooldownMap = new ConcurrentHashMap<>();

    // 短时会话上下文暂存 (key: contextToken, value: 上下文数据)，供点击展开多轮对话时继承
    private final Map<String, ContextSnapshot> contextStore = new ConcurrentHashMap<>();

    // 被动浏览事件默认冷却时间：180 秒 (3分钟)
    private static final long PASSIVE_COOLDOWN_MS = 180 * 1000L;

    public PetReactionServiceImpl(AgentRecommendService agentRecommendService) {
        this.agentRecommendService = agentRecommendService;
    }

    @Override
    public AgentPetBubbleVO reactToEvent(AgentEventDTO eventDTO, Long currentUserId, String clientIp) {
        if (eventDTO == null || !StringUtils.hasText(eventDTO.getEventType())) {
            return null;
        }

        String eventType = eventDTO.getEventType().trim().toUpperCase();
        String sessionKey = StringUtils.hasText(eventDTO.getVisitorSessionId()) ?
                eventDTO.getVisitorSessionId().trim() : (clientIp != null ? clientIp : "ANONYMOUS");

        // 1. 被动事件频控防扰检查 (如进入页面 PAGE_ENTER)
        if ("PAGE_ENTER".equals(eventType)) {
            Long lastTime = passiveCooldownMap.get(sessionKey);
            long now = System.currentTimeMillis();
            if (lastTime != null && (now - lastTime) < PASSIVE_COOLDOWN_MS) {
                // 处于静默防扰期，不主动弹出气泡
                return null;
            }
            passiveCooldownMap.put(sessionKey, now);
        }

        // 2. 根据事件类型执行对应反应策略
        return switch (eventType) {
            case "ACTION_CUSTOM" -> handleCustomInteractiveAction(eventDTO, sessionKey);
            case "ARTICLE_READ" -> handleArticleReadAction(eventDTO, sessionKey);
            case "ADMIN_POST_SAVED" -> handleAdminPostSavedAction(eventDTO, currentUserId, sessionKey);
            case "PAGE_ENTER" -> handlePageEnterAction(eventDTO, sessionKey);
            default -> handleDefaultExploreAction(eventDTO, sessionKey);
        };
    }

    /**
     * 场景一：处理自定义交互行为 (如前端算命/计算运势、心理测验、技能测评等)
     */
    private AgentPetBubbleVO handleCustomInteractiveAction(AgentEventDTO eventDTO, String sessionKey) {
        Map<String, Object> payload = eventDTO.getPayload() != null ? eventDTO.getPayload() : Collections.emptyMap();
        String divinationName = payload.get("divinationName") != null ? payload.get("divinationName").toString() : null;
        String omen = payload.get("omen") != null ? payload.get("omen").toString() : null;
        String summary = eventDTO.getSummary();

        // 提取用于文章匹配的语义标签
        List<String> metaTags = new ArrayList<>();
        if (!CollectionUtils.isEmpty(eventDTO.getMetaTags())) {
            metaTags.addAll(eventDTO.getMetaTags());
        }
        if (StringUtils.hasText(divinationName)) {
            metaTags.add(divinationName);
        }
        if (StringUtils.hasText(omen)) {
            metaTags.add(omen);
        }

        // 获取最符合卦象/互动心境的推荐文章
        List<ArticleSimpleVO> articles = agentRecommendService.getArticlesByMetaTags(metaTags, eventDTO.getTargetId(), 1);
        ArticleSimpleVO matchedArticle = articles.isEmpty() ? null : articles.get(0);

        // 判定小宠物神态表情
        String petMood = "THINKING";
        if (StringUtils.hasText(omen)) {
            if (omen.contains("吉") || omen.contains("亨") || omen.contains("利") || omen.contains("顺")) {
                petMood = "HAPPY";
            } else if (omen.contains("未") || omen.contains("变") || omen.contains("待") || omen.contains("蓄")) {
                petMood = "THINKING";
            } else {
                petMood = "ENERGETIC";
            }
        }

        // 组装精炼气泡台词 (控制在 45 字以内)
        String bubbleText;
        if (matchedArticle != null) {
            if (StringUtils.hasText(divinationName)) {
                bubbleText = "算得" + divinationName + "，心境顺应天时。为你推荐这篇《" + matchedArticle.getTitle() + "》助益前行：";
            } else if (StringUtils.hasText(summary)) {
                bubbleText = "顺应当前心境，博主精选好文《" + matchedArticle.getTitle() + "》正合你意：";
            } else {
                bubbleText = "既然有此际遇，推荐一读博主精选的《" + matchedArticle.getTitle() + "》：";
            }
        } else {
            bubbleText = "卦象深远，顺时而动。随时点击我开启对话探讨顺应之道。";
        }

        // 生成多轮对话继承 Token
        String contextToken = UUID.randomUUID().toString().replace("-", "");
        contextStore.put(contextToken, new ContextSnapshot(eventDTO, matchedArticle, System.currentTimeMillis()));

        // 组装纯文本无符号操作按钮
        List<AgentSuggestActionVO> actions = new ArrayList<>();
        if (matchedArticle != null) {
            Map<String, Object> navParams = new HashMap<>();
            navParams.put("articleId", matchedArticle.getId());
            navParams.put("title", matchedArticle.getTitle());
            actions.add(new AgentSuggestActionVO("NAVIGATE_ARTICLE", "顺应卦象", navParams));
        }

        Map<String, Object> chatParams = new HashMap<>();
        chatParams.put("contextToken", contextToken);
        actions.add(new AgentSuggestActionVO("OPEN_CHAT", "与助手聊聊", chatParams));
        actions.add(new AgentSuggestActionVO("DISMISS_BUBBLE", "收起气泡", Collections.emptyMap()));

        AgentPetBubbleVO vo = new AgentPetBubbleVO();
        vo.setBubbleText(bubbleText);
        vo.setPetMood(petMood);
        vo.setAutoDismissSec(10);
        vo.setSuggestActions(actions);
        vo.setRelatedArticle(matchedArticle);
        vo.setContextToken(contextToken);
        vo.setCooldownSec(0);

        return vo;
    }

    /**
     * 场景二：处理访客深度阅读行为 (如停留阅读 > 30s 或滑到底部)
     */
    private AgentPetBubbleVO handleArticleReadAction(AgentEventDTO eventDTO, String sessionKey) {
        Long articleId = eventDTO.getTargetId();
        if (articleId == null) {
            return null;
        }

        List<ArticleSimpleVO> relatedList = agentRecommendService.getRelatedArticles(articleId, 1);
        if (relatedList.isEmpty()) {
            return null;
        }
        ArticleSimpleVO nextArticle = relatedList.get(0);

        String contextToken = UUID.randomUUID().toString().replace("-", "");
        contextStore.put(contextToken, new ContextSnapshot(eventDTO, nextArticle, System.currentTimeMillis()));

        List<AgentSuggestActionVO> actions = new ArrayList<>();
        Map<String, Object> navParams = new HashMap<>();
        navParams.put("articleId", nextArticle.getId());
        navParams.put("title", nextArticle.getTitle());

        actions.add(new AgentSuggestActionVO("NAVIGATE_ARTICLE", "阅读推荐文章", navParams));
        actions.add(new AgentSuggestActionVO("DISMISS_BUBBLE", "稍后再看", Collections.emptyMap()));

        AgentPetBubbleVO vo = new AgentPetBubbleVO();
        vo.setBubbleText("看你研究技术这么专注，博主这篇关联好文《" + nextArticle.getTitle() + "》也很值得一读！");
        vo.setPetMood("GUIDE");
        vo.setAutoDismissSec(8);
        vo.setSuggestActions(actions);
        vo.setRelatedArticle(nextArticle);
        vo.setContextToken(contextToken);
        vo.setCooldownSec(120);

        return vo;
    }

    /**
     * 场景三：处理管理员保存/发布文章行为 (严格限制管理员权限)
     */
    private AgentPetBubbleVO handleAdminPostSavedAction(AgentEventDTO eventDTO, Long currentUserId, String sessionKey) {
        // 安全红线校验：若无管理员登录态，严禁返回管理类动作
        if (currentUserId == null) {
            return null;
        }

        Long articleId = eventDTO.getTargetId();
        List<AgentSuggestActionVO> actions = new ArrayList<>();

        Map<String, Object> pushParams = new HashMap<>();
        pushParams.put("articleId", articleId);
        actions.add(new AgentSuggestActionVO("PUSH_QQ", "推送QQ群", pushParams));

        Map<String, Object> pinParams = new HashMap<>();
        pinParams.put("articleId", articleId);
        actions.add(new AgentSuggestActionVO("PIN_TOP", "设为置顶", pinParams));

        Map<String, Object> viewParams = new HashMap<>();
        viewParams.put("articleId", articleId);
        actions.add(new AgentSuggestActionVO("VIEW_PAGE", "查看前台", viewParams));

        AgentPetBubbleVO vo = new AgentPetBubbleVO();
        vo.setBubbleText("新文章已成功发布！要现在将其推送到外部群组或设为置顶吗？");
        vo.setPetMood("ENERGETIC");
        vo.setAutoDismissSec(0); // 管理员动作提示默认常驻，等待操作
        vo.setSuggestActions(actions);
        vo.setCooldownSec(0);

        return vo;
    }

    /**
     * 场景四：页面进入冷启动 (PAGE_ENTER)
     */
    private AgentPetBubbleVO handlePageEnterAction(AgentEventDTO eventDTO, String sessionKey) {
        List<ArticleSimpleVO> exploreList = agentRecommendService.getExploreArticles(1);
        ArticleSimpleVO topArticle = exploreList.isEmpty() ? null : exploreList.get(0);

        List<AgentSuggestActionVO> actions = new ArrayList<>();
        if (topArticle != null) {
            Map<String, Object> navParams = new HashMap<>();
            navParams.put("articleId", topArticle.getId());
            actions.add(new AgentSuggestActionVO("NAVIGATE_ARTICLE", "探索文章", navParams));
        }
        actions.add(new AgentSuggestActionVO("MUTE_PET", "今日静音", Collections.emptyMap()));
        actions.add(new AgentSuggestActionVO("DISMISS_BUBBLE", "收起气泡", Collections.emptyMap()));

        AgentPetBubbleVO vo = new AgentPetBubbleVO();
        if (topArticle != null) {
            vo.setBubbleText("欢迎光临博主的技术小站！今日推荐探索这篇精选文章：");
            vo.setRelatedArticle(topArticle);
        } else {
            vo.setBubbleText("欢迎来到博客！点击我可以查看推荐文章或随时向我提问。");
        }
        vo.setPetMood("IDLE");
        vo.setAutoDismissSec(8);
        vo.setSuggestActions(actions);
        vo.setCooldownSec(180);

        return vo;
    }

    private AgentPetBubbleVO handleDefaultExploreAction(AgentEventDTO eventDTO, String sessionKey) {
        return handlePageEnterAction(eventDTO, sessionKey);
    }

    /**
     * 内部短时上下文快照对象
     */
    private static class ContextSnapshot {
        final AgentEventDTO eventDTO;
        final ArticleSimpleVO matchedArticle;
        final long createTimestamp;

        ContextSnapshot(AgentEventDTO eventDTO, ArticleSimpleVO matchedArticle, long createTimestamp) {
            this.eventDTO = eventDTO;
            this.matchedArticle = matchedArticle;
            this.createTimestamp = createTimestamp;
        }
    }
}
