package com.liuyang.myblog.controller;

import com.liuyang.myblog.common.ApiResponse;
import com.liuyang.myblog.domain.dto.LocalImageImportDTO;
import com.liuyang.myblog.domain.po.BlogFile;
import com.liuyang.myblog.domain.vo.LocalImageImportVO;
import com.liuyang.myblog.mapper.BlogFileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/admin/files")
/**
 * 本地文件辅助管理控制器
 */
public class LocalFileController {

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "svg", "ico", "bmp"
    );

    private final BlogFileMapper blogFileMapper;

    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;

    public LocalFileController(BlogFileMapper blogFileMapper) {
        this.blogFileMapper = blogFileMapper;
    }

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

    /**
     * 从本地路径直接读取并一键转存导入图片
     */
    @PostMapping("/import-local-images")
    public ApiResponse<List<LocalImageImportVO>> importLocalImages(@RequestBody LocalImageImportDTO request) {
        if (request == null || request.getImagePaths() == null || request.getImagePaths().isEmpty()) {
            return ApiResponse.ok(Collections.emptyList());
        }

        String folderPath = request.getFolderPath();
        List<LocalImageImportVO> results = new ArrayList<>();
        String datePath = LocalDate.now().toString();

        for (String rawPath : request.getImagePaths()) {
            if (!StringUtils.hasText(rawPath)) continue;

            File targetFile = resolveLocalFile(rawPath, folderPath);
            if (targetFile == null || !targetFile.exists() || !targetFile.isFile()) {
                results.add(new LocalImageImportVO(rawPath, getFilenameFromPath(rawPath), null, false, "本地文件不存在"));
                continue;
            }

            String filename = targetFile.getName();
            int dotIdx = filename.lastIndexOf('.');
            String ext = dotIdx > 0 ? filename.substring(dotIdx + 1).toLowerCase(Locale.ROOT) : "";
            if (!ALLOWED_IMAGE_EXTENSIONS.contains(ext)) {
                results.add(new LocalImageImportVO(rawPath, filename, null, false, "不支持的图片格式: ." + ext));
                continue;
            }

            try {
                String newFilename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
                Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(datePath);
                Files.createDirectories(targetDir);
                Path destination = targetDir.resolve(newFilename).normalize();

                Files.copy(targetFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                String url = "/api/files/" + datePath + "/" + newFilename;

                BlogFile blogFile = new BlogFile();
                blogFile.setOriginalName(filename);
                blogFile.setFileName(newFilename);
                blogFile.setUrl(url);
                blogFile.setFileType(ext);
                blogFile.setFileSize(targetFile.length());
                blogFileMapper.insert(blogFile);

                results.add(new LocalImageImportVO(rawPath, filename, url, true, null));
            } catch (Exception e) {
                results.add(new LocalImageImportVO(rawPath, filename, null, false, "转存失败: " + e.getMessage()));
            }
        }

        return ApiResponse.ok(results);
    }

    private File resolveLocalFile(String rawPath, String folderPath) {
        if (!StringUtils.hasText(rawPath)) return null;

        // 1. 进行 URL 解码（处理 %20、中文等）并去除可能的引号与 file:/// 前缀
        String decodedPath = rawPath.trim();
        try {
            decodedPath = java.net.URLDecoder.decode(decodedPath, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception ignored) {}

        if (decodedPath.startsWith("file:///")) {
            decodedPath = decodedPath.substring(8);
        } else if (decodedPath.startsWith("file://")) {
            decodedPath = decodedPath.substring(7);
        }
        if ((decodedPath.startsWith("\"") && decodedPath.endsWith("\"")) || (decodedPath.startsWith("'") && decodedPath.endsWith("'"))) {
            decodedPath = decodedPath.substring(1, decodedPath.length() - 1).trim();
        }

        // 2. 如果解码后本身就是存在的绝对路径
        File direct = new File(decodedPath);
        if (direct.exists() && direct.isFile()) {
            return direct;
        }

        // 3. 如果提供了 folderPath
        if (StringUtils.hasText(folderPath)) {
            String cleanFolder = folderPath.trim();
            if ((cleanFolder.startsWith("\"") && cleanFolder.endsWith("\"")) || (cleanFolder.startsWith("'") && cleanFolder.endsWith("'"))) {
                cleanFolder = cleanFolder.substring(1, cleanFolder.length() - 1).trim();
            }

            // 3.1 尝试 folderPath + decodedPath
            File combined = new File(cleanFolder, decodedPath);
            if (combined.exists() && combined.isFile()) {
                return combined;
            }

            // 3.2 尝试 folderPath + 纯文件名
            String cleanFilename = getFilenameFromPath(decodedPath);
            File byFilename = new File(cleanFolder, cleanFilename);
            if (byFilename.exists() && byFilename.isFile()) {
                return byFilename;
            }

            // 3.3 尝试在 folderPath 及其子目录中递归查找该文件名 (最多递归 4 层)
            File foundRecursive = findFileRecursively(new File(cleanFolder), cleanFilename, 0, 4);
            if (foundRecursive != null && foundRecursive.isFile()) {
                return foundRecursive;
            }
        }

        return null;
    }

    private File findFileRecursively(File dir, String targetFilename, int depth, int maxDepth) {
        if (dir == null || !dir.exists() || !dir.isDirectory() || depth > maxDepth) {
            return null;
        }
        File[] files = dir.listFiles();
        if (files == null) return null;

        for (File f : files) {
            if (f.isFile() && f.getName().equalsIgnoreCase(targetFilename)) {
                return f;
            }
        }
        for (File f : files) {
            if (f.isDirectory() && !f.getName().startsWith(".")) {
                File found = findFileRecursively(f, targetFilename, depth + 1, maxDepth);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String getFilenameFromPath(String path) {
        if (!StringUtils.hasText(path)) return "";
        String clean = path.replace('\\', '/');
        int lastSlash = clean.lastIndexOf('/');
        return lastSlash >= 0 ? clean.substring(lastSlash + 1) : clean;
    }
}
