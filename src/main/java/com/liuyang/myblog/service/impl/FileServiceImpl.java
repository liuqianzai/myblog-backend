package com.liuyang.myblog.service.impl;

import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.domain.po.BlogFile;
import com.liuyang.myblog.domain.vo.FileUploadVO;
import com.liuyang.myblog.mapper.BlogFileMapper;
import com.liuyang.myblog.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
/**
 * 文件业务逻辑接口实现类
 */
public class FileServiceImpl implements FileService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            // 图片格式
            "jpg", "jpeg", "png", "gif", "webp", "svg", "ico", "bmp",
            // 文档格式
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md",
            // 压缩包格式
            "zip", "rar", "7z", "tar", "gz",
            // 音视频格式
            "mp3", "mp4", "wav", "webm"
    );

    private final BlogFileMapper blogFileMapper;

    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;

    public FileServiceImpl(BlogFileMapper blogFileMapper) {
        this.blogFileMapper = blogFileMapper;
    }
    /**
     * 上传文件
     */

    @Transactional
    @Override
    public FileUploadVO uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.badRequest("file is required");
        }
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw BusinessException.badRequest("unsupported file extension: ." + extension);
        }

        String datePath = LocalDate.now().toString();
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetDir = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(datePath);
        Path targetPath = targetDir.resolve(filename).normalize();
        if (!targetPath.startsWith(targetDir)) {
            throw BusinessException.badRequest("invalid filename");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetPath);
        } catch (IOException e) {
            throw new IllegalStateException("failed to save file", e);
        }

        String url = "/api/files/" + datePath + "/" + filename;

        BlogFile blogFile = new BlogFile();
        blogFile.setOriginalName(originalFilename);
        blogFile.setFileName(filename);
        blogFile.setUrl(url);
        blogFile.setFileType(extension);
        blogFile.setFileSize(file.getSize());
        blogFileMapper.insert(blogFile);

        return new FileUploadVO(url, filename);
    }    /**
     * 获取Extension
     */


    private String getExtension(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            throw BusinessException.badRequest("file extension is required");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
