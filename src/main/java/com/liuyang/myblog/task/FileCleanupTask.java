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

    /**
     * 清理任务开关。
     * 该任务会物理删除磁盘文件，属于不可逆操作，因此默认关闭：
     * 必须在确认引用判定逻辑（LIKE 匹配正文）与当前数据完全吻合后再手工开启，
     * 否则一次误判就会把仍在使用的图片删掉。
     */
    @Value("${blog.file.cleanup.enabled:false}")
    private boolean cleanupEnabled;

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
        if (!cleanupEnabled) {
            log.info("文件清理任务未启用（blog.file.cleanup.enabled=false），本次跳过。");
            return;
        }
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
            // 显式用 and(...) 包住 or()：裸 or() 会与 MyBatis-Plus 自动追加的逻辑删除条件
            // （deleted = 0）平级拼接，生成 deleted=0 AND content LIKE ? OR cover = ? 这种
            // 优先级错乱的 SQL，导致已删除文章的封面被误判为仍被引用。
            Long articleCount = blogArticleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                    .and(wrapper -> wrapper
                            .like(BlogArticle::getContent, url)
                            .or()
                            .eq(BlogArticle::getCover, url)));
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
