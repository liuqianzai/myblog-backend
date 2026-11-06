package com.liuyang.myblog.service;

import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.po.BlogConfig;
import com.liuyang.myblog.domain.vo.PublicConfigVO;
import com.liuyang.myblog.mapper.BlogConfigMapper;
import com.liuyang.myblog.service.impl.ConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigServiceImplTest {

    @Mock
    private BlogConfigMapper blogConfigMapper;

    private ConfigServiceImpl configService;

    @BeforeEach
    void setUp() {
        configService = new ConfigServiceImpl(blogConfigMapper);
    }

    @Test
    @DisplayName("公开配置接口过滤掉非白名单配置项")
    void testListPublicConfigsFiltersSensitiveKeys() {
        when(blogConfigMapper.selectList(any())).thenReturn(List.of(
                config("site_title", "Liu Yang's Blog"),
                config("home_title", "首页标题"),
                // 模拟将来可能出现的私有配置
                config("llm_api_key", "sk-should-not-leak"),
                config("oss_secret", "secret-should-not-leak")
        ));

        List<PublicConfigVO> result = configService.listPublicConfigs();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(vo -> "site_title".equals(vo.getConfigKey())));
        assertTrue(result.stream().anyMatch(vo -> "home_title".equals(vo.getConfigKey())));
        assertFalse(result.stream().anyMatch(vo -> "sk-should-not-leak".equals(vo.getConfigValue())));
        assertFalse(result.stream().anyMatch(vo -> "secret-should-not-leak".equals(vo.getConfigValue())));
    }

    @Test
    @DisplayName("读取非白名单配置返回 404，不暴露其是否存在")
    void testGetNonPublicConfigReturnsNotFound() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> configService.getPublicConfig("llm_api_key"));
        assertEquals(404, ex.getCode());
    }

    @Test
    @DisplayName("带尾随制表符的脏数据（如 about_content\\t）仍可按正常键名读到")
    void testDirtyKeyWithTrailingTabIsReadable() {
        when(blogConfigMapper.selectById("about_content")).thenReturn(null);
        when(blogConfigMapper.selectList(any())).thenReturn(List.of(
                config("about_content\t", "about me")));

        PublicConfigVO vo = configService.getPublicConfig("about_content");

        assertEquals("about_content", vo.getConfigKey());
        assertEquals("about me", vo.getConfigValue());
    }

    private BlogConfig config(String key, String value) {
        BlogConfig config = new BlogConfig();
        config.setConfigKey(key);
        config.setConfigValue(value);
        return config;
    }
}
