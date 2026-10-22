package com.liuyang.myblog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 本地图片导入结果
 *
 * 数据来源：
 * 【blog_file】：filename, url
 * 【计算/逻辑/其他】：rawPath, success, errorMessage
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocalImageImportVO {
    private String rawPath;
    private String filename;
    private String url;
    private Boolean success;
    private String errorMessage;
}
