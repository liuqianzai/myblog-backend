package com.liuyang.myblog.domain.vo;

import lombok.Data;

/**
 * 公开站点配置视图对象
 *
 * 只暴露前台渲染确实需要的站点级配置，
 * 避免把可能存在的密钥类配置（如第三方 API Key）随列表接口一起公开出去。
 * 管理端仍使用 BlogConfig 实体，可以看到全部配置项。
 */
@Data
public class PublicConfigVO {
    private String configKey;
    private String configValue;
}
