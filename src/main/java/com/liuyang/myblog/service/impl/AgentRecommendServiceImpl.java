package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.po.BlogArticleTag;
import com.liuyang.myblog.domain.po.BlogCategory;
import com.liuyang.myblog.domain.po.BlogTag;
import com.liuyang.myblog.domain.vo.ArticleSimpleVO;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogArticleTagMapper;
import com.liuyang.myblog.mapper.BlogCategoryMapper;
import com.liuyang.myblog.mapper.BlogTagMapper;
import com.liuyang.myblog.service.AgentRecommendService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能体文章推荐服务实现类
 *
 * 遵循 SQL 性能优先原则：单表直查与轻量条件查询，所有相似度交叉比对与衰减打分在内存中完成。
 */
@Service
public class AgentRecommendServiceImpl implements AgentRecommendService {
    private final BlogArticleMapper blogArticleMapper;
    private final BlogArticleTagMapper blogArticleTagMapper;
    private final BlogTagMapper blogTagMapper;
    private final BlogCategoryMapper blogCategoryMapper;

    public AgentRecommendServiceImpl(BlogArticleMapper blogArticleMapper,
                                    BlogArticleTagMapper blogArticleTagMapper,
                                    BlogTagMapper blogTagMapper,
                                    BlogCategoryMapper blogCategoryMapper) {
        this.blogArticleMapper = blogArticleMapper;
        this.blogArticleTagMapper = blogArticleTagMapper;
        this.blogTagMapper = blogTagMapper;
        this.blogCategoryMapper = blogCategoryMapper;
    }

    @Override
    public List<ArticleSimpleVO> getRelatedArticles(Long articleId, Integer limit) {
        if (articleId == null) {
            return Collections.emptyList();
        }
        int maxLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);

        BlogArticle currentArticle = blogArticleMapper.selectById(articleId);
        if (currentArticle == null || Boolean.FALSE.equals(currentArticle.getStatus())) {
            return Collections.emptyList();
        }

        // 1. 获取当前文章的所有关联标签 ID
        List<Long> currentTagIds = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                        .eq(BlogArticleTag::getArticleId, articleId))
                .stream()
                .map(BlogArticleTag::getTagId)
                .toList();

        // 2. 查询拥有相同标签的候选文章 ID
        Set<Long> candidateIds = new HashSet<>();
        if (!CollectionUtils.isEmpty(currentTagIds)) {
            List<BlogArticleTag> tagRelations = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                    .in(BlogArticleTag::getTagId, currentTagIds)
                    .ne(BlogArticleTag::getArticleId, articleId));
            for (BlogArticleTag relation : tagRelations) {
                candidateIds.add(relation.getArticleId());
            }
        }

        // 3. 同时查询同一分类下的文章候选
        if (currentArticle.getCategoryId() != null) {
            List<BlogArticle> sameCategoryArticles = blogArticleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                    .eq(BlogArticle::getCategoryId, currentArticle.getCategoryId())
                    .eq(BlogArticle::getStatus, true)
                    .ne(BlogArticle::getId, articleId)
                    .last("LIMIT 30"));
            for (BlogArticle article : sameCategoryArticles) {
                candidateIds.add(article.getId());
            }
        }

        // 4. 若无强关联文章，退化为精选探索推荐
        if (candidateIds.isEmpty()) {
            return getExploreArticles(maxLimit);
        }

        // 5. 单表直查所有已发布的候选文章并进行内存打分
        List<BlogArticle> candidates = blogArticleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .in(BlogArticle::getId, candidateIds)
                .eq(BlogArticle::getStatus, true));

        // 建立文章到其标签列表的映射
        Map<Long, List<Long>> articleToTagsMap = new HashMap<>();
        if (!candidates.isEmpty()) {
            List<Long> ids = candidates.stream().map(BlogArticle::getId).toList();
            List<BlogArticleTag> allRelations = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                    .in(BlogArticleTag::getArticleId, ids));
            for (BlogArticleTag rel : allRelations) {
                articleToTagsMap.computeIfAbsent(rel.getArticleId(), k -> new ArrayList<>()).add(rel.getTagId());
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<ScoredArticle> scoredList = new ArrayList<>();
        Set<Long> currentTagSet = new HashSet<>(currentTagIds);

        for (BlogArticle article : candidates) {
            double score = 0.0;
            String matchReason = "综合推荐";

            // 标签重合度打分
            List<Long> tags = articleToTagsMap.getOrDefault(article.getId(), Collections.emptyList());
            long overlapCount = tags.stream().filter(currentTagSet::contains).count();
            if (overlapCount > 0) {
                score += overlapCount * 35.0;
                matchReason = "共现标签匹配";
            }

            // 同分类打分
            if (currentArticle.getCategoryId() != null && currentArticle.getCategoryId().equals(article.getCategoryId())) {
                score += 25.0;
                if (overlapCount == 0) {
                    matchReason = "同分类推荐";
                }
            }

            // 阅读量热度微调
            int views = article.getViewCount() == null ? 0 : article.getViewCount();
            score += Math.log1p(views) * 5.0;

            // 时效性微调
            if (article.getCreateTime() != null) {
                long days = Duration.between(article.getCreateTime(), now).toDays();
                if (days <= 30) {
                    score += 10.0;
                } else if (days <= 90) {
                    score += 5.0;
                }
            }

            scoredList.add(new ScoredArticle(article, score, matchReason));
        }

        // 6. 按分数降序截取
        scoredList.sort((a, b) -> Double.compare(b.score, a.score));
        List<BlogArticle> topArticles = scoredList.stream()
                .limit(maxLimit)
                .map(sa -> sa.article)
                .toList();

        Map<Long, String> reasonMap = scoredList.stream()
                .collect(Collectors.toMap(sa -> sa.article.getId(), sa -> sa.reason, (k1, k2) -> k1));

        return assembleSimpleVOList(topArticles, reasonMap);
    }

    @Override
    public List<ArticleSimpleVO> getExploreArticles(Integer limit) {
        int maxLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);

        // 获取近期或高热度的已发布文章候选
        List<BlogArticle> candidates = blogArticleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, true)
                .orderByDesc(BlogArticle::getIsTop)
                .orderByDesc(BlogArticle::getCreateTime)
                .last("LIMIT 40"));

        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        LocalDateTime now = LocalDateTime.now();
        List<ScoredArticle> scoredList = new ArrayList<>();

        for (BlogArticle article : candidates) {
            double score = 0.0;
            // 置顶文章加权
            if (Boolean.TRUE.equals(article.getIsTop())) {
                score += 500.0;
            }

            // 时间衰减热度算法: Score = ViewCount / (Days + 2)^1.2
            int views = article.getViewCount() == null ? 0 : article.getViewCount();
            long days = article.getCreateTime() == null ? 30 : Math.max(0, Duration.between(article.getCreateTime(), now).toDays());
            score += (views + 5.0) / Math.pow(days + 2.0, 1.2);

            String reason = Boolean.TRUE.equals(article.getIsTop()) ? "置顶精选" : "近期热门精选";
            scoredList.add(new ScoredArticle(article, score, reason));
        }

        scoredList.sort((a, b) -> Double.compare(b.score, a.score));
        List<BlogArticle> topArticles = scoredList.stream()
                .limit(maxLimit)
                .map(sa -> sa.article)
                .toList();

        Map<Long, String> reasonMap = scoredList.stream()
                .collect(Collectors.toMap(sa -> sa.article.getId(), sa -> sa.reason, (k1, k2) -> k1));

        return assembleSimpleVOList(topArticles, reasonMap);
    }

    @Override
    public List<ArticleSimpleVO> getArticlesByMetaTags(List<String> metaTags, Long excludeArticleId, Integer limit) {
        int maxLimit = limit == null || limit <= 0 ? 5 : Math.min(limit, 20);

        if (CollectionUtils.isEmpty(metaTags)) {
            return getExploreArticles(maxLimit);
        }

        // 1. 在标签表中寻找语义匹配的标签 ID
        List<String> cleanTags = metaTags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .toList();

        if (cleanTags.isEmpty()) {
            return getExploreArticles(maxLimit);
        }

        // 优先根据标签名精准或模糊命中
        List<BlogTag> allTags = blogTagMapper.selectList(null);
        Set<Long> matchedTagIds = new HashSet<>();
        for (BlogTag tag : allTags) {
            String tagName = tag.getName();
            if (tagName == null) continue;
            for (String meta : cleanTags) {
                if (tagName.equalsIgnoreCase(meta) || tagName.contains(meta) || meta.contains(tagName)) {
                    matchedTagIds.add(tag.getId());
                    break;
                }
            }
        }

        Set<Long> candidateIds = new HashSet<>();
        if (!matchedTagIds.isEmpty()) {
            List<BlogArticleTag> relations = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                    .in(BlogArticleTag::getTagId, matchedTagIds));
            for (BlogArticleTag rel : relations) {
                if (excludeArticleId == null || !rel.getArticleId().equals(excludeArticleId)) {
                    candidateIds.add(rel.getArticleId());
                }
            }
        }

        // 2. 如果标签命中不足，利用关键词模糊搜索标题与摘要进行补齐
        if (candidateIds.size() < maxLimit) {
            LambdaQueryWrapper<BlogArticle> keywordWrapper = new LambdaQueryWrapper<BlogArticle>()
                    .eq(BlogArticle::getStatus, true);
            if (excludeArticleId != null) {
                keywordWrapper.ne(BlogArticle::getId, excludeArticleId);
            }
            keywordWrapper.and(wrapper -> {
                for (int i = 0; i < cleanTags.size(); i++) {
                    String tag = cleanTags.get(i);
                    if (i == 0) {
                        wrapper.like(BlogArticle::getTitle, tag).or().like(BlogArticle::getSummary, tag);
                    } else {
                        wrapper.or().like(BlogArticle::getTitle, tag).or().like(BlogArticle::getSummary, tag);
                    }
                }
            });
            keywordWrapper.last("LIMIT 20");
            List<BlogArticle> keywordArticles = blogArticleMapper.selectList(keywordWrapper);
            for (BlogArticle a : keywordArticles) {
                candidateIds.add(a.getId());
            }
        }

        // 若仍未命中任何文章，退化为全局精选
        if (candidateIds.isEmpty()) {
            return getExploreArticles(maxLimit);
        }

        List<BlogArticle> candidates = blogArticleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .in(BlogArticle::getId, candidateIds)
                .eq(BlogArticle::getStatus, true)
                .last("LIMIT 30"));

        Map<Long, String> reasonMap = new HashMap<>();
        for (BlogArticle a : candidates) {
            reasonMap.put(a.getId(), "匹配心境标签: " + String.join("、", cleanTags));
        }

        return assembleSimpleVOList(candidates.stream().limit(maxLimit).toList(), reasonMap);
    }

    /**
     * 批量组装 ArticleSimpleVO，避免循环单条查询分类与标签
     */
    private List<ArticleSimpleVO> assembleSimpleVOList(List<BlogArticle> articles, Map<Long, String> reasonMap) {
        if (CollectionUtils.isEmpty(articles)) {
            return Collections.emptyList();
        }

        // 1. 批量查询分类信息
        Set<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> categoryNameMap = new HashMap<>();
        if (!categoryIds.isEmpty()) {
            List<BlogCategory> categories = blogCategoryMapper.selectBatchIds(categoryIds);
            for (BlogCategory cat : categories) {
                categoryNameMap.put(cat.getId(), cat.getName());
            }
        }

        // 2. 批量查询标签信息
        List<Long> articleIds = articles.stream().map(BlogArticle::getId).toList();
        List<BlogArticleTag> relations = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .in(BlogArticleTag::getArticleId, articleIds));

        Map<Long, List<Long>> articleToTagIds = new HashMap<>();
        Set<Long> allTagIds = new HashSet<>();
        for (BlogArticleTag rel : relations) {
            articleToTagIds.computeIfAbsent(rel.getArticleId(), k -> new ArrayList<>()).add(rel.getTagId());
            allTagIds.add(rel.getTagId());
        }

        Map<Long, String> tagNameMap = new HashMap<>();
        if (!allTagIds.isEmpty()) {
            List<BlogTag> tags = blogTagMapper.selectBatchIds(allTagIds);
            for (BlogTag tag : tags) {
                tagNameMap.put(tag.getId(), tag.getName());
            }
        }

        // 3. 装配为 ArticleSimpleVO
        List<ArticleSimpleVO> result = new ArrayList<>();
        for (BlogArticle article : articles) {
            ArticleSimpleVO vo = new ArticleSimpleVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSummary(article.getSummary());
            vo.setCover(article.getCover());
            vo.setViewCount(article.getViewCount() == null ? 0 : article.getViewCount());
            vo.setCreateTime(article.getCreateTime());

            if (article.getCategoryId() != null) {
                vo.setCategoryName(categoryNameMap.get(article.getCategoryId()));
            }

            List<Long> tagIds = articleToTagIds.getOrDefault(article.getId(), Collections.emptyList());
            List<String> tagNames = tagIds.stream()
                    .map(tagNameMap::get)
                    .filter(Objects::nonNull)
                    .toList();
            vo.setTags(tagNames);
            vo.setMatchReason(reasonMap.getOrDefault(article.getId(), "精选推荐"));

            result.add(vo);
        }

        return result;
    }

    private static class ScoredArticle {
        final BlogArticle article;
        final double score;
        final String reason;

        ScoredArticle(BlogArticle article, double score, String reason) {
            this.article = article;
            this.score = score;
            this.reason = reason;
        }
    }
}
