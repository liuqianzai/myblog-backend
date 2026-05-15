package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.ConfigDTO;
import com.liuyang.myblog.domain.po.BlogConfig;

import java.util.List;

public interface ConfigService {
    List<BlogConfig> listConfigs();

    BlogConfig getConfig(String key);

    void saveConfig(ConfigDTO configDTO);

    void deleteConfig(String key);
}
