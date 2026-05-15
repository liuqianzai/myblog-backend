package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.ConfigDTO;
import com.liuyang.myblog.domain.po.BlogConfig;
import com.liuyang.myblog.mapper.BlogConfigMapper;
import com.liuyang.myblog.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ConfigServiceImpl implements ConfigService {
    private final BlogConfigMapper blogConfigMapper;

    public ConfigServiceImpl(BlogConfigMapper blogConfigMapper) {
        this.blogConfigMapper = blogConfigMapper;
    }

    @Override
    public List<BlogConfig> listConfigs() {
        return blogConfigMapper.selectList(new LambdaQueryWrapper<BlogConfig>().orderByAsc(BlogConfig::getConfigKey));
    }

    @Override
    public BlogConfig getConfig(String key) {
        BlogConfig config = blogConfigMapper.selectById(key);
        if (config == null) {
            throw BusinessException.notFound("config not found");
        }
        return config;
    }

    @Override
    public void saveConfig(ConfigDTO configDTO) {
        if (configDTO == null || !StringUtils.hasText(configDTO.getConfigKey())) {
            throw BusinessException.badRequest("config key is required");
        }
        BlogConfig config = new BlogConfig();
        config.setConfigKey(configDTO.getConfigKey());
        config.setConfigValue(configDTO.getConfigValue());
        config.setRemark(configDTO.getRemark());
        if (blogConfigMapper.selectById(config.getConfigKey()) == null) {
            blogConfigMapper.insert(config);
        } else {
            blogConfigMapper.updateById(config);
        }
    }

    @Override
    public void deleteConfig(String key) {
        if (blogConfigMapper.deleteById(key) == 0) {
            throw BusinessException.notFound("config not found");
        }
    }
}
