package com.liuyang.myblog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigDTO {
    @NotBlank(message = "配置键不能为空")
    @Size(max = 50, message = "配置键长度不能超过 50 个字符")
    private String configKey;

    private String configValue;

    @Size(max = 255, message = "备注长度不能超过 255 个字符")
    private String remark;
}
