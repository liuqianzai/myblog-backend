package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.vo.DashboardStatsVO;
import com.liuyang.myblog.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/dashboard")
/**
 * 后台仪表盘数据统计管理控制器
 */
public class AdminDashboardController {
    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    /**
     * 获取统计数据
     */

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsVO> getStats() {
        return ApiResponse.ok(dashboardService.getStats());
    }
}
