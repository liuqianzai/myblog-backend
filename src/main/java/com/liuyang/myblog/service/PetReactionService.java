package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.AgentEventDTO;
import com.liuyang.myblog.domain.vo.AgentPetBubbleVO;

/**
 * 智能体小宠物行为感知与气泡反应服务接口
 */
public interface PetReactionService {
    /**
     * 响应前端行为事件，生成小宠物气泡与操作建议
     *
     * @param eventDTO 事件上报入参
     * @param currentUserId 当前登录用户ID (若为访客则为 null)
     * @param clientIp 客户端请求IP
     * @return 气泡展示与下一步操作载荷 (若处于防扰静默期则可能返回 null)
     */
    AgentPetBubbleVO reactToEvent(AgentEventDTO eventDTO, Long currentUserId, String clientIp);
}
