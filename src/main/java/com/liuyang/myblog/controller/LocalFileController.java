package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/admin/files")
/**
 * 本地文件辅助管理控制器
 */
public class LocalFileController {
    /**
     * 打开本地文件夹
     */

    @PostMapping("/open-folder")
    public ApiResponse<Void> openLocalFolder(@RequestBody Map<String, String> request) {
        String path = request.get("path");
        if (path == null || path.isEmpty()) {
            return ApiResponse.fail(400, "路径不能为空");
        }

        File folder = new File(path);

        if (!folder.exists() || !folder.isDirectory()) {
            return ApiResponse.fail(400, "本地文件夹不存在：" + path);
        }

        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                // Windows 系统下调用 cmd.exe 打开资源管理器并准确定位
                Runtime.getRuntime().exec(new String[]{"cmd.exe", "/c", "start", "\"\"", path});
            } else if (os.contains("mac")) {
                // macOS 系统下调用 open 命令
                Runtime.getRuntime().exec(new String[]{"open", path});
            } else {
                // Linux / Unix 环境下调用 xdg-open
                Runtime.getRuntime().exec(new String[]{"xdg-open", path});
            }
            return ApiResponse.ok();
        } catch (IOException e) {
            return ApiResponse.fail(500, "打开文件夹失败：" + e.getMessage());
        }
    }
}
