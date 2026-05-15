package com.liuyang.myblog.domain.dto;

import lombok.Data;

@Data
public class ConfigDTO {
    private String configKey;
    private String configValue;
    private String remark;
}
