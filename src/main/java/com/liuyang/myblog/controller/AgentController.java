package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.AuthContext;
import com.liuyang.myblog.domain.dto.AgentEventDTO;
import com.liuyang.myblog.domain.vo.AgentPetBubbleVO;
import com.liuyang.myblog.domain.vo.ArticleSimpleVO;
import com.liuyang.myblog.service.AgentRecommendService;
import com.liuyang.myblog.service.PetReactionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前台智能体交互与文章推荐控制器
 */
@RestController
@RequestMapping("/agent")
public class AgentController {
    private final PetReactionService petReactionService;
    private final AgentRecommendService agentRecommendService;

    public AgentController(PetReactionService petReactionService,
                           AgentRecommendService agentRecommendService) {
        this.petReactionService = petReactionService;
        this.agentRecommendService = agentRecommendService;
    }

    /**
     * 前端行为感知事件上报与小宠物气泡响应
     */
    @PostMapping("/pet/bubble")
    public ApiResponse<AgentPetBubbleVO> reactPetBubble(@RequestBody AgentEventDTO eventDTO, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AuthContext.USER_ID_ATTRIBUTE);
        String clientIp = resolveClientIp(request);
        return ApiResponse.ok(petReactionService.reactToEvent(eventDTO, currentUserId, clientIp));
    }

    /**
     * 获取与指定文章最相关的推荐列表(Item-to-Item)
     */
    @GetMapping("/recommend/related")
    public ApiResponse<List<ArticleSimpleVO>> getRelatedArticles(@RequestParam Long articleId,
                                                                 @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(agentRecommendService.getRelatedArticles(articleId, limit));
    }

    /**
     * 获取探索/热度精选推荐列表(首页冷启动/时间衰减热门推荐)
     */
    @GetMapping("/recommend/explore")
    public ApiResponse<List<ArticleSimpleVO>> getExploreArticles(@RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(agentRecommendService.getExploreArticles(limit));
    }

    /**
     * 根据业务语义标签获取推荐文章(如运势、测验结果关键词)
     */
    @GetMapping("/recommend/tags")
    public ApiResponse<List<ArticleSimpleVO>> getArticlesByMetaTags(@RequestParam List<String> tags,
                                                                    @RequestParam(required = false) Long excludeArticleId,
                                                                    @RequestParam(required = false) Integer limit) {
        return ApiResponse.ok(agentRecommendService.getArticlesByMetaTags(tags, excludeArticleId, limit));
    }

    private String resolveClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
