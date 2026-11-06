package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.dto.ConfigDTO;
import com.liuyang.myblog.domain.po.BlogConfig;
import com.liuyang.myblog.domain.vo.PublicConfigVO;

import java.util.List;

/**
 * 系统配置业务逻辑接口
 */
public interface ConfigService {/**
 * 列表查询配置
 */

    List<BlogConfig> listConfigs();    /**
     * 获取配置
     */


    BlogConfig getConfig(String key);    /**
     * 创建配置
     */


    void saveConfig(ConfigDTO configDTO);    /**
     * 删除配置
     */


    void deleteConfig(String key);    /**
     * 公开配置列表（供前台渲染使用，已过滤非公开项）
     */


    List<PublicConfigVO> listPublicConfigs();    /**
     * 获取单个公开配置
     */


    PublicConfigVO getPublicConfig(String key);
}
