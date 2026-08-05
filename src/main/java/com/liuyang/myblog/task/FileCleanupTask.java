package com.liuyang.myblog.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.domain.po.*;
import com.liuyang.myblog.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class FileCleanupTask {

    private final BlogFileMapper blogFileMapper;
    private final BlogArticleMapper blogArticleMapper;
    private final BlogPageMapper blogPageMapper;
    private final BlogFriendLinkMapper blogFriendLinkMapper;
    private final BlogConfigMapper blogConfigMapper;

    @Value("${blog.upload.dir:uploads}")
    private String uploadDir;

    public FileCleanupTask(BlogFileMapper blogFileMapper,
                           BlogArticleMapper blogArticleMapper,
                           BlogPageMapper blogPageMapper,
                           BlogFriendLinkMapper blogFriendLinkMapper,
                           BlogConfigMapper blogConfigMapper) {
        this.blogFileMapper = blogFileMapper;
        this.blogArticleMapper = blogArticleMapper;
        this.blogPageMapper = blogPageMapper;
        this.blogFriendLinkMapper = blogFriendLinkMapper;
        this.blogConfigMapper = blogConfigMapper;
    }

    /**
     * 每天凌晨 2:00 运行，清理无主图片和文档
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOrphanedFiles() {
        log.info("Starting scheduled cleanup task for orphaned files...");

        // 1. 查询 24 小时之前创建的文件列表
        LocalDateTime limitTime = LocalDateTime.now().minusHours(24);
        LambdaQueryWrapper<BlogFile> fileWrapper = new LambdaQueryWrapper<BlogFile>()
                .lt(BlogFile::getCreateTime, limitTime);
        List<BlogFile> files = blogFileMapper.selectList(fileWrapper);

        if (files == null || files.isEmpty()) {
            log.info("No files found to check for cleanup.");
            return;
        }

        int deleteCount = 0;

        for (BlogFile file : files) {
            String url = file.getUrl();
            if (url == null || !url.startsWith("/api/files/")) {
                continue;
            }

            // 2. 检查引用情况
            boolean isReferenced = false;

            // 2.1 检查文章内容和封面
            Long articleCount = blogArticleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                    .like(BlogArticle::getContent, url)
                    .or()
                    .eq(BlogArticle::getCover, url));
            if (articleCount > 0) {
                isReferenced = true;
            }

            // 2.2 检查独立页面
            if (!isReferenced) {
                Long pageCount = blogPageMapper.selectCount(new LambdaQueryWrapper<BlogPage>()
                        .like(BlogPage::getContent, url));
                if (pageCount > 0) {
                    isReferenced = true;
                }
            }

            // 2.3 检查友情链接头像
            if (!isReferenced) {
                Long linkCount = blogFriendLinkMapper.selectCount(new LambdaQueryWrapper<BlogFriendLink>()
                        .eq(BlogFriendLink::getAvatar, url));
                if (linkCount > 0) {
                    isReferenced = true;
                }
            }

            // 2.4 检查系统配置
            if (!isReferenced) {
                Long configCount = blogConfigMapper.selectCount(new LambdaQueryWrapper<BlogConfig>()
                        .like(BlogConfig::getConfigValue, url));
                if (configCount > 0) {
                    isReferenced = true;
                }
            }

            // 3. 如果没有任何地方引用，则进行删除
            if (!isReferenced) {
                try {
                    // 从 URL 提取出路径部分，例如 /api/files/2026-07-13/xxx.png -> 2026-07-13/xxx.png
                    String relativePath = url.substring("/api/files/".length());
                    Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(relativePath);
                    
                    // 物理删除文件
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Successfully deleted orphaned physical file: {}", filePath);
                    } else {
                        log.warn("Physical file not found but database record existed: {}", filePath);
                    }

                    // 数据库删除记录
                    blogFileMapper.deleteById(file.getId());
                    log.info("Successfully deleted orphaned database record: ID={}, URL={}", file.getId(), url);
                    
                    deleteCount++;
                } catch (IOException e) {
                    log.error("Failed to delete physical file for URL: " + url, e);
                } catch (Exception e) {
                    log.error("Failed to delete database record for ID: " + file.getId(), e);
                }
            }
        }

        log.info("Scheduled cleanup task finished. Total orphaned files deleted: {}", deleteCount);
    }
}
