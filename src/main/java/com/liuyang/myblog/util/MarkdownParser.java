package com.liuyang.myblog.util;

import lombok.Data;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 文档解析工具类
 */
public class MarkdownParser {

    private static final Pattern FRONT_MATTER_PATTERN = Pattern.compile("^\\s*---\\r?\\n([\\s\\S]*?)\\r?\\n---\\r?\\n?");

    @Data
    public static class ParsedArticle {
        private String title;
        private String summary;
        private String content;
        private String category;
        private List<String> tags = new ArrayList<>();
        private Boolean isTop;
        private String cover;
    }

    public static ParsedArticle parse(String rawText, String originalFilename) {
        ParsedArticle article = new ParsedArticle();
        if (!StringUtils.hasText(rawText)) {
            article.setTitle(getDefaultTitle(originalFilename));
            article.setContent("");
            article.setSummary("");
            article.setIsTop(false);
            return article;
        }

        // 清除 UTF-8 BOM 头
        String text = rawText.startsWith("\uFEFF") ? rawText.substring(1) : rawText;
        String content = text;

        // 1. 尝试解析 Front-matter (YAML 元数据头)
        Matcher frontMatterMatcher = FRONT_MATTER_PATTERN.matcher(text);
        if (frontMatterMatcher.find()) {
            String yamlContent = frontMatterMatcher.group(1);
            content = text.substring(frontMatterMatcher.end()).trim();
            parseYamlMetadata(yamlContent, article);
        }

        // 2. 识别标题：若 Front-matter 未指定，则提取首行一级标题；否则降级为文件名
        if (!StringUtils.hasText(article.getTitle())) {
            String[] lines = content.split("\\r?\\n");
            int h1LineIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    if (line.startsWith("# ")) {
                        article.setTitle(line.substring(2).trim());
                        h1LineIndex = i;
                    }
                    break;
                }
            }

            // 若在正文首部提取到一级标题，从正文中移除该行，避免文章标题与正文重复显示
            if (h1LineIndex != -1) {
                StringBuilder newContent = new StringBuilder();
                for (int i = 0; i < lines.length; i++) {
                    if (i != h1LineIndex) {
                        newContent.append(lines[i]).append("\n");
                    }
                }
                content = newContent.toString().trim();
            } else {
                article.setTitle(getDefaultTitle(originalFilename));
            }
        }

        article.setContent(content);

        // 3. 识别摘要：若 Front-matter 未指定，自动提取正文纯文本前 150 个字符
        if (!StringUtils.hasText(article.getSummary()) && StringUtils.hasText(content)) {
            article.setSummary(extractSummaryFromContent(content));
        }

        if (article.getIsTop() == null) {
            article.setIsTop(false);
        }

        return article;
    }

    private static void parseYamlMetadata(String yamlContent, ParsedArticle article) {
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(yamlContent);
            if (!(loaded instanceof Map<?, ?> map)) {
                return;
            }

            // 标题
            Object titleObj = map.get("title");
            if (titleObj != null && StringUtils.hasText(titleObj.toString())) {
                article.setTitle(titleObj.toString().trim());
            }

            // 摘要
            Object summaryObj = map.get("summary");
            if (summaryObj == null) {
                summaryObj = map.get("description");
            }
            if (summaryObj == null) {
                summaryObj = map.get("desc");
            }
            if (summaryObj != null && StringUtils.hasText(summaryObj.toString())) {
                article.setSummary(summaryObj.toString().trim());
            }

            // 分类
            Object categoryObj = map.get("category");
            if (categoryObj == null) {
                categoryObj = map.get("categories");
            }
            if (categoryObj != null) {
                if (categoryObj instanceof List<?> list && !list.isEmpty()) {
                    article.setCategory(list.get(0).toString().trim());
                } else if (StringUtils.hasText(categoryObj.toString())) {
                    article.setCategory(categoryObj.toString().trim());
                }
            }

            // 标签
            Object tagsObj = map.get("tags");
            if (tagsObj == null) {
                tagsObj = map.get("tag");
            }
            if (tagsObj != null) {
                List<String> tags = new ArrayList<>();
                if (tagsObj instanceof List<?> list) {
                    for (Object item : list) {
                        if (item != null && StringUtils.hasText(item.toString())) {
                            tags.add(item.toString().trim());
                        }
                    }
                } else {
                    String[] split = tagsObj.toString().split("[,，]");
                    for (String s : split) {
                        if (StringUtils.hasText(s)) {
                            tags.add(s.trim());
                        }
                    }
                }
                article.setTags(tags);
            }

            // 置顶
            Object topObj = map.get("isTop");
            if (topObj == null) {
                topObj = map.get("top");
            }
            if (topObj instanceof Boolean b) {
                article.setIsTop(b);
            } else if (topObj != null) {
                article.setIsTop(Boolean.parseBoolean(topObj.toString()));
            }

            // 封面
            Object coverObj = map.get("cover");
            if (coverObj != null && StringUtils.hasText(coverObj.toString())) {
                article.setCover(coverObj.toString().trim());
            }

        } catch (Exception ignored) {
            // 解析 YAML 出错时降级处理，不影响正文解析
        }
    }

    private static String extractSummaryFromContent(String content) {
        // 过滤代码块、图片、链接、标题标记等 Markdown 语法
        String plain = content.replaceAll("```[\\s\\S]*?```", " ")
                .replaceAll("!\\[.*?\\]\\(.*?\\)", " ")
                .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1")
                .replaceAll("^#+\\s+", " ")
                .replaceAll("[*`>_~\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (plain.length() <= 150) {
            return plain;
        }
        return plain.substring(0, 150) + "...";
    }

    private static String getDefaultTitle(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "未命名文章";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            return originalFilename.substring(0, dotIndex);
        }
        return originalFilename;
    }
}
