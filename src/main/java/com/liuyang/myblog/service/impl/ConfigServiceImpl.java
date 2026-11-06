package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.dto.ConfigDTO;
import com.liuyang.myblog.domain.po.BlogConfig;
import com.liuyang.myblog.domain.vo.PublicConfigVO;
import com.liuyang.myblog.mapper.BlogConfigMapper;
import com.liuyang.myblog.service.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
/**
 * 系统配置业务逻辑接口实现类
 */
public class ConfigServiceImpl implements ConfigService {

    /**
     * 允许公开读取的配置键前缀。
     *
     * 采用白名单而不是黑名单：将来新增任何配置项（例如第三方 API Key、
     * 对象存储密钥）只要不在这个前缀范围内，就不会被公开接口带出去。
     * 新增前台需要的配置项时，记得把对应前缀加到这里。
     */
    private static final Set<String> PUBLIC_KEY_PREFIXES = Set.of(
            "site_",      // 站点标题、SEO 描述与关键词
            "home_",      // 首页文案
            "profile_",   // 个人简介
            "about_",     // 关于页面内容
            "social_"     // 社交链接
    );

    private final BlogConfigMapper blogConfigMapper;

    public ConfigServiceImpl(BlogConfigMapper blogConfigMapper) {
        this.blogConfigMapper = blogConfigMapper;
    }
    /**
     * 列表查询配置
     */

    @Override
    public List<BlogConfig> listConfigs() {
        return blogConfigMapper.selectList(new LambdaQueryWrapper<BlogConfig>().orderByAsc(BlogConfig::getConfigKey));
    }
    /**
     * 获取配置
     */

    @Override
    public BlogConfig getConfig(String key) {
        BlogConfig config = blogConfigMapper.selectById(key);
        if (config == null) {
            throw BusinessException.notFound("config not found");
        }
        return config;
    }
    /**
     * 创建配置
     */

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
    /**
     * 删除配置
     */

    @Override
    public void deleteConfig(String key) {
        if (blogConfigMapper.deleteById(key) == 0) {
            throw BusinessException.notFound("config not found");
        }
    }

    /**
     * 公开配置列表（供前台渲染使用，已过滤非公开项）
     */
    @Override
    public List<PublicConfigVO> listPublicConfigs() {
        return listConfigs().stream()
                .filter(config -> isPublicKey(config.getConfigKey()))
                .map(this::toPublicVO)
                .toList();
    }

    /**
     * 获取单个公开配置
     */
    @Override
    public PublicConfigVO getPublicConfig(String key) {
        if (!isPublicKey(key)) {
            // 用 404 而不是 403：不向匿名访问者确认某个配置键是否存在
            throw BusinessException.notFound("config not found");
        }
        String normalized = key.trim();
        BlogConfig config = blogConfigMapper.selectById(normalized);
        if (config == null) {
            // 兼容库中的脏数据：部分键带有尾随制表符（如 "about_content\t"），
            // 直接按主键查不到，改为在列表里按 trim 后比较。
            config = listConfigs().stream()
                    .filter(item -> item.getConfigKey() != null && item.getConfigKey().trim().equals(normalized))
                    .findFirst()
                    .orElseThrow(() -> BusinessException.notFound("config not found"));
        }
        return toPublicVO(config);
    }

    /**
     * 判断某个配置键是否允许公开读取。
     * 注意对键做了 trim：库中存在 "about_content\t" 这类带尾随制表符的脏数据。
     */
    private boolean isPublicKey(String key) {
        if (!StringUtils.hasText(key)) {
            return false;
        }
        String normalized = key.trim().toLowerCase();
        return PUBLIC_KEY_PREFIXES.stream().anyMatch(normalized::startsWith);
    }

    private PublicConfigVO toPublicVO(BlogConfig config) {
        PublicConfigVO vo = new PublicConfigVO();
        // 返回 trim 后的键名，避免脏数据把制表符带到前端
        vo.setConfigKey(config.getConfigKey() == null ? null : config.getConfigKey().trim());
        vo.setConfigValue(config.getConfigValue());
        return vo;
    }
}
