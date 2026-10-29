package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.common.AuthContext;
import com.liuyang.myblog.domain.dto.AgentEventDTO;
import com.liuyang.myblog.domain.vo.AgentSuggestActionVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 后台管理智能体操作联想控制器
 */
@RestController
@RequestMapping("/admin/agent")
public class AdminAgentController {

    /**
     * 根据后台事件上下文获取下一步操作联想列表
     */
    @PostMapping("/actions/suggest")
    public ApiResponse<List<AgentSuggestActionVO>> suggestAdminActions(@RequestBody AgentEventDTO eventDTO,
                                                                      HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute(AuthContext.USER_ID_ATTRIBUTE);
        if (currentUserId == null) {
            return ApiResponse.ok(Collections.emptyList());
        }

        String scene = eventDTO.getScene() != null ? eventDTO.getScene().trim().toUpperCase() : "GENERAL";
        Long targetId = eventDTO.getTargetId();

        List<AgentSuggestActionVO> actions = new ArrayList<>();

        if ("ARTICLE_EDIT".equals(scene) || "ADMIN_POST_SAVED".equals(scene)) {
            if (targetId != null) {
                Map<String, Object> pushParams = new HashMap<>();
                pushParams.put("articleId", targetId);
                actions.add(new AgentSuggestActionVO("PUSH_QQ", "推送QQ群", pushParams));

                Map<String, Object> pinParams = new HashMap<>();
                pinParams.put("articleId", targetId);
                actions.add(new AgentSuggestActionVO("PIN_TOP", "设为置顶", pinParams));

                Map<String, Object> viewParams = new HashMap<>();
                viewParams.put("articleId", targetId);
                actions.add(new AgentSuggestActionVO("VIEW_PAGE", "查看前台", viewParams));
            }
        } else if ("DASHBOARD".equals(scene)) {
            actions.add(new AgentSuggestActionVO("NEW_ARTICLE", "写新文章", Collections.emptyMap()));
            actions.add(new AgentSuggestActionVO("REVIEW_COMMENTS", "审核评论", Collections.emptyMap()));
            actions.add(new AgentSuggestActionVO("CHECK_EXPLORE", "查看前台热门", Collections.emptyMap()));
        }

        return ApiResponse.ok(actions);
    }
}
