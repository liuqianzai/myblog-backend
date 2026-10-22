package com.liuyang.myblog.domain.dto;

import lombok.Data;
import java.util.List;

/**
 * 本地图片导入请求参数
 *
 * 数据来源：
 * 【计算/逻辑/其他】：folderPath, imagePaths
 */
@Data
public class LocalImageImportDTO {
    /**
     * 图片所在的本地文件夹路径（可选，当 imagePaths 为相对路径或文件名时结合使用）
     */
    private String folderPath;

    /**
     * 待导入的图片路径列表（可以是完整绝对路径，或相对路径/文件名）
     */
    private List<String> imagePaths;
}
