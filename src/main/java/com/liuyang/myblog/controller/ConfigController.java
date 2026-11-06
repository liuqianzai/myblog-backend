package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.ConfigDTO;
import com.liuyang.myblog.domain.vo.PublicConfigVO;
import com.liuyang.myblog.service.ConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/configs")
/**
 * 站点配置控制器
 *
 * 读接口对外公开，但只返回前台渲染需要的站点级配置；
 * 写接口保留在原路径以兼容已有前端，由 AuthInterceptor 强制要求管理员身份。
 * 管理员查看全部配置请使用 /admin/configs。
 */
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }
    /**
     * 列表查询配置
     */

    @GetMapping
    public ApiResponse<List<PublicConfigVO>> listConfigs() {
        return ApiResponse.ok(configService.listPublicConfigs());
    }
    /**
     * 获取配置
     */

    @GetMapping("/{key}")
    public ApiResponse<PublicConfigVO> getConfig(@PathVariable String key) {
        return ApiResponse.ok(configService.getPublicConfig(key));
    }
    /**
     * 创建配置
     */

    @PostMapping
    public ApiResponse<Void> saveConfig(@Valid @RequestBody ConfigDTO configDTO) {
        configService.saveConfig(configDTO);
        return ApiResponse.ok();
    }
    /**
     * 删除配置
     */

    @DeleteMapping("/{key}")
    public ApiResponse<Void> deleteConfig(@PathVariable String key) {
        configService.deleteConfig(key);
        return ApiResponse.ok();
    }
}
