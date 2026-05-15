package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.ConfigDTO;
import com.liuyang.myblog.domain.po.BlogConfig;
import com.liuyang.myblog.service.ConfigService;
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
public class ConfigController {
    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public ApiResponse<List<BlogConfig>> listConfigs() {
        return ApiResponse.ok(configService.listConfigs());
    }

    @GetMapping("/{key}")
    public ApiResponse<BlogConfig> getConfig(@PathVariable String key) {
        return ApiResponse.ok(configService.getConfig(key));
    }

    @PostMapping
    public ApiResponse<Void> saveConfig(@RequestBody ConfigDTO configDTO) {
        configService.saveConfig(configDTO);
        return ApiResponse.ok();
    }

    @DeleteMapping("/{key}")
    public ApiResponse<Void> deleteConfig(@PathVariable String key) {
        configService.deleteConfig(key);
        return ApiResponse.ok();
    }
}
