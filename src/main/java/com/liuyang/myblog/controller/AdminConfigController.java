package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.po.BlogConfig;
import com.liuyang.myblog.service.ConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/configs")
/**
 * 后台站点配置管理控制器
 *
 * 与公开的 /configs 分开：这里返回全部配置项（含备注等后台字段），
 * 由 AuthInterceptor 强制校验管理员角色。
 */
public class AdminConfigController {
    private final ConfigService configService;

    public AdminConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * 管理员查看全部配置
     */
    @GetMapping
    public ApiResponse<List<BlogConfig>> listAllConfigs() {
        return ApiResponse.ok(configService.listConfigs());
    }
}
