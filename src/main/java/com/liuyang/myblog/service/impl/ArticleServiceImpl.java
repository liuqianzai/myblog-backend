package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.common.PageResult;
import com.liuyang.myblog.domain.dto.ArticleDTO;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.po.BlogArticleTag;
import com.liuyang.myblog.domain.po.BlogCategory;
import com.liuyang.myblog.domain.po.BlogTag;
import com.liuyang.myblog.domain.vo.ArchiveVO;
import com.liuyang.myblog.domain.vo.ArticleVO;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogArticleTagMapper;
import com.liuyang.myblog.mapper.BlogCategoryMapper;
import com.liuyang.myblog.mapper.BlogTagMapper;
import com.liuyang.myblog.service.ArticleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
/**
 * 文章业务逻辑接口实现类
 */
public class ArticleServiceImpl implements ArticleService {
    private final BlogArticleMapper blogArticleMapper;
    private final BlogArticleTagMapper blogArticleTagMapper;
    private final BlogTagMapper blogTagMapper;
    private final BlogCategoryMapper blogCategoryMapper;

    public ArticleServiceImpl(BlogArticleMapper blogArticleMapper,
                              BlogArticleTagMapper blogArticleTagMapper,
                              BlogTagMapper blogTagMapper,
                              BlogCategoryMapper blogCategoryMapper) {
        this.blogArticleMapper = blogArticleMapper;
        this.blogArticleTagMapper = blogArticleTagMapper;
        this.blogTagMapper = blogTagMapper;
        this.blogCategoryMapper = blogCategoryMapper;
    }
    /**
     * 分页查询文章
     */

    @Override
    public PageResult<ArticleVO> pageArticles(Long page, Long size, String keyword, Boolean status, Long categoryId, Long tagId) {
        long pageNo = page == null || page < 1 ? 1 : page;
        long pageSize = size == null || size < 1 ? 10 : Math.min(size, 100);
        LambdaQueryWrapper<BlogArticle> wrapper = new LambdaQueryWrapper<BlogArticle>()
                .orderByDesc(BlogArticle::getIsTop)
                .orderByDesc(BlogArticle::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(item -> item.like(BlogArticle::getTitle, keyword)
                    .or()
                    .like(BlogArticle::getSummary, keyword));
        }
        if (status != null) {
            wrapper.eq(BlogArticle::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(BlogArticle::getCategoryId, categoryId);
        }
        if (tagId != null) {
            List<Long> articleIds = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                            .eq(BlogArticleTag::getTagId, tagId))
                    .stream()
                    .map(BlogArticleTag::getArticleId)
                    .distinct()
                    .toList();
            if (articleIds.isEmpty()) {
                return new PageResult<>(0L, pageNo, pageSize, Collections.emptyList());
            }
            wrapper.in(BlogArticle::getId, articleIds);
        }

        Page<BlogArticle> result = blogArticleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        List<ArticleVO> records = fillTags(result.getRecords());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }
    /**
     * 获取文章
     */

    @Override
    public ArticleVO getArticle(Long id, boolean increaseViewCount, Boolean status) {
        BlogArticle article = blogArticleMapper.selectById(id);
        if (article == null) {
            throw BusinessException.notFound("article not found");
        }
        if (status != null && !status.equals(article.getStatus())) {
            throw BusinessException.notFound("article not found");
        }
        if (increaseViewCount) {
            int currentViewCount = article.getViewCount() == null ? 0 : article.getViewCount();
            blogArticleMapper.update(null, new LambdaUpdateWrapper<BlogArticle>()
                    .eq(BlogArticle::getId, id)
                    .set(BlogArticle::getViewCount, currentViewCount + 1));
            article.setViewCount(currentViewCount + 1);
        }
        ArticleVO articleVO = toVO(article);
        articleVO.setTags(getTags(id));
        return articleVO;
    }
    /**
     * 创建文章
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleDTO articleDTO) {
        validateArticle(articleDTO);
        BlogArticle article = new BlogArticle();
        copy(articleDTO, article);
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        blogArticleMapper.insert(article);
        saveArticleTags(article.getId(), articleDTO.getTagIds());
        return article.getId();
    }
    /**
     * 更新文章
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long id, ArticleDTO articleDTO) {
        validateArticle(articleDTO);
        BlogArticle article = blogArticleMapper.selectById(id);
        if (article == null) {
            throw BusinessException.notFound("article not found");
        }
        copy(articleDTO, article);
        blogArticleMapper.updateById(article);
        saveArticleTags(id, articleDTO.getTagIds());
    }
    /**
     * 删除文章
     */

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        if (blogArticleMapper.deleteById(id) == 0) {
            throw BusinessException.notFound("article not found");
        }
        blogArticleTagMapper.delete(new LambdaQueryWrapper<BlogArticleTag>().eq(BlogArticleTag::getArticleId, id));
    }    /**
     * validate文章
     */


    private void validateArticle(ArticleDTO articleDTO) {
        if (articleDTO == null || !StringUtils.hasText(articleDTO.getTitle()) || !StringUtils.hasText(articleDTO.getContent())) {
            throw BusinessException.badRequest("title and content are required");
        }
    }    /**
     * copy
     */


    private void copy(ArticleDTO source, BlogArticle target) {
        if (source.getCategoryId() != null && blogCategoryMapper.selectById(source.getCategoryId()) == null) {
            throw BusinessException.badRequest("category does not exist");
        }
        target.setTitle(source.getTitle());
        target.setSummary(source.getSummary());
        target.setContent(source.getContent());
        target.setCover(source.getCover());
        target.setCategoryId(source.getCategoryId());
        target.setIsTop(Boolean.TRUE.equals(source.getIsTop()));
        target.setStatus(source.getStatus() == null ? Boolean.TRUE : source.getStatus());
    }    /**
     * 创建文章标签
     */


    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        blogArticleTagMapper.delete(new LambdaQueryWrapper<BlogArticleTag>().eq(BlogArticleTag::getArticleId, articleId));
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        List<Long> distinctTagIds = tagIds.stream().distinct().toList();
        if (blogTagMapper.selectBatchIds(distinctTagIds).size() != distinctTagIds.size()) {
            throw BusinessException.badRequest("some tags do not exist");
        }
        for (Long tagId : distinctTagIds) {
            BlogArticleTag relation = new BlogArticleTag();
            relation.setArticleId(articleId);
            relation.setTagId(tagId);
            blogArticleTagMapper.insert(relation);
        }
    }    /**
     * fill标签
     */


    private List<ArticleVO> fillTags(List<BlogArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> articleIds = articles.stream().map(BlogArticle::getId).toList();
        List<BlogArticleTag> relations = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .in(BlogArticleTag::getArticleId, articleIds));
        Map<Long, List<BlogArticleTag>> relationMap = relations.stream()
                .collect(Collectors.groupingBy(BlogArticleTag::getArticleId));
        List<Long> tagIds = relations.stream().map(BlogArticleTag::getTagId).distinct().toList();
        Map<Long, BlogTag> tagMap = tagIds.isEmpty()
                ? Collections.emptyMap()
                : blogTagMapper.selectBatchIds(tagIds).stream().collect(Collectors.toMap(BlogTag::getId, tag -> tag));

        return articles.stream().map(article -> {
            ArticleVO articleVO = toVO(article);
            List<BlogTag> tags = relationMap.getOrDefault(article.getId(), Collections.emptyList()).stream()
                    .map(relation -> tagMap.get(relation.getTagId()))
                    .filter(tag -> tag != null)
                    .toList();
            articleVO.setTags(tags);
            return articleVO;
        }).toList();
    }    /**
     * 获取标签
     */


    private List<BlogTag> getTags(Long articleId) {
        List<BlogArticleTag> relations = blogArticleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .eq(BlogArticleTag::getArticleId, articleId));
        List<Long> tagIds = relations.stream().map(BlogArticleTag::getTagId).distinct().toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return blogTagMapper.selectBatchIds(tagIds);
    }
    /**
     * 列表查询归档
     */

    @Override
    public List<ArchiveVO> listArchives() {
        return blogArticleMapper.selectArchives();
    }    /**
     * toVO
     */


    private ArticleVO toVO(BlogArticle article) {
        ArticleVO articleVO = new ArticleVO();
        articleVO.setId(article.getId());
        articleVO.setTitle(article.getTitle());
        articleVO.setSummary(article.getSummary());
        articleVO.setContent(article.getContent());
        articleVO.setCover(article.getCover());
        articleVO.setCategoryId(article.getCategoryId());
        articleVO.setViewCount(article.getViewCount());
        articleVO.setIsTop(article.getIsTop());
        articleVO.setStatus(article.getStatus());
        articleVO.setCreateTime(article.getCreateTime());
        articleVO.setUpdateTime(article.getUpdateTime());
        if (article.getCategoryId() != null) {
            BlogCategory category = blogCategoryMapper.selectById(article.getCategoryId());
            articleVO.setCategory(category);
        }
        return articleVO;
    }
}
