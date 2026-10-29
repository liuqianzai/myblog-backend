package com.liuyang.myblog.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 智能体小宠物气泡输出对象
 *
 * 计算/逻辑/其他：bubbleText, petMood, autoDismissSec, suggestActions, relatedArticle, contextToken, cooldownSec
 */
@Data
public class AgentPetBubbleVO {
    /**
     * 气泡显示的文本内容(精简生动，通常 45 字以内)
     */
    private String bubbleText;

    /**
     * 宠物神态状态: HAPPY(高兴/大吉), THINKING(思考/转机), ENERGETIC(活力), IDLE(待机), GUIDE(指引)
     */
    private String petMood;

    /**
     * 气泡自动消失倒计时(秒)，默认 8 秒，0 表示常驻不自动关闭
     */
    private Integer autoDismissSec;

    /**
     * 推荐的纯文本按钮列表(严格无 Emoji/无装饰符号)
     */
    private List<AgentSuggestActionVO> suggestActions;

    /**
     * 关联推荐的文章摘要(若有)
     */
    private ArticleSimpleVO relatedArticle;

    /**
     * 会话上下文 Token(点击宠物展开深聊时用于继承上文)
     */
    private String contextToken;

    /**
     * 建议前端频控防打扰冷却时长(秒)
     */
    private Integer cooldownSec;
}
