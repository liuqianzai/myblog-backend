package com.liuyang.myblog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagDTO {
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过 50 个字符")
    private String name;

    // 颜色值会直接写进前端样式，限定为 #RGB / #RRGGBB，避免写入任意字符串
    @Pattern(regexp = "^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$", message = "颜色需为 #RGB 或 #RRGGBB 格式")
    private String color;
}
