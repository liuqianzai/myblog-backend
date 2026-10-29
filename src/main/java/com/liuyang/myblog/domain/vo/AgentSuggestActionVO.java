package com.liuyang.myblog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 智能体建议操作载荷
 *
 * 计算/逻辑/其他：actionCode, label, payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSuggestActionVO {
    /**
     * 操作指令码: NAVIGATE_ARTICLE, OPEN_CHAT, DISMISS_BUBBLE, MUTE_PET, PUSH_QQ, PIN_TOP 等
     */
    private String actionCode;

    /**
     * 前端展示按钮文案(严格遵循纯文本无符号规范，如: 顺应卦象、阅读文章、收起气泡、今日静音、推送QQ群)
     */
    private String label;

    /**
     * 操作携带的上下文参数(如 articleId, contextToken 等)
     */
    private Map<String, Object> payload;
}
