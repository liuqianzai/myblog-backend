package com.liuyang.myblog.domain.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 智能体行为感知事件传输对象
 *
 * 计算/逻辑/其他：eventType, scene, metaTags, summary, targetId, visitorSessionId, payload
 */
@Data
public class AgentEventDTO {
    /**
     * 事件类型: ACTION_CUSTOM(自定义互动如算卦), ARTICLE_READ(文章阅读), PAGE_ENTER(页面进入), ADMIN_POST_SAVED(管理员发文)
     */
    private String eventType;

    /**
     * 所处场景: FORTUNE_PAGE, ARTICLE_DETAIL, HOME, ADMIN_DASHBOARD 等
     */
    private String scene;

    /**
     * 业务语义抽象标签: 如 ["事业", "转机", "自强不息"] 或 ["Redis", "高并发"]
     */
    private List<String> metaTags;

    /**
     * 事件摘要文本: 如 "摇得未济卦，事未成，充满转机" 或 "正在阅读《Redis深度优化》"
     */
    private String summary;

    /**
     * 关联目标 ID (如文章 ID)
     */
    private Long targetId;

    /**
     * 访客会话唯一标识(前端生成并维护于 sessionStorage 中)
     */
    private String visitorSessionId;

    /**
     * 灵活业务数据载荷(如阅读时长 readDurationSec、卦象名称 divinationName 等)
     */
    private Map<String, Object> payload;
}
