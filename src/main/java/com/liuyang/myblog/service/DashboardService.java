package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.vo.DashboardStatsVO;

/**
 * 仪表盘数据统计业务逻辑接口
 */
public interface DashboardService {/**
 * 获取统计数据
 */

    DashboardStatsVO getStats();
}
