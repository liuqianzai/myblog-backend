package com.liuyang.myblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.po.BlogComment;
import com.liuyang.myblog.domain.po.BlogTag;
import com.liuyang.myblog.domain.vo.DashboardStatsVO;
import com.liuyang.myblog.mapper.BlogArticleMapper;
import com.liuyang.myblog.mapper.BlogCategoryMapper;
import com.liuyang.myblog.mapper.BlogCommentMapper;
import com.liuyang.myblog.mapper.BlogTagMapper;
import com.liuyang.myblog.service.DashboardService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardServiceImpl implements DashboardService {
    private final BlogArticleMapper blogArticleMapper;
    private final BlogTagMapper blogTagMapper;
    private final BlogCommentMapper blogCommentMapper;
    private final BlogCategoryMapper blogCategoryMapper;

    public DashboardServiceImpl(BlogArticleMapper blogArticleMapper,
                                BlogTagMapper blogTagMapper,
                                BlogCommentMapper blogCommentMapper,
                                BlogCategoryMapper blogCategoryMapper) {
        this.blogArticleMapper = blogArticleMapper;
        this.blogTagMapper = blogTagMapper;
        this.blogCommentMapper = blogCommentMapper;
        this.blogCategoryMapper = blogCategoryMapper;
    }

    @Override
    public DashboardStatsVO getStats() {
        long totalArticles = blogArticleMapper.selectCount(null);
        long publishedArticles = blogArticleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, Boolean.TRUE));
        long hiddenArticles = blogArticleMapper.selectCount(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, Boolean.FALSE));
        long totalTags = blogTagMapper.selectCount(null);
        long totalCategories = blogCategoryMapper.selectCount(null);
        long totalComments = blogCommentMapper.selectCount(null);
        long pendingComments = blogCommentMapper.selectCount(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getApproved, Boolean.FALSE));

        List<Object> sumResult = blogArticleMapper.selectObjs(new QueryWrapper<BlogArticle>()
                .select("COALESCE(SUM(view_count), 0)"));
        long totalViews = 0L;
        if (!sumResult.isEmpty() && sumResult.get(0) != null) {
            totalViews = Long.parseLong(sumResult.get(0).toString());
        }

        return new DashboardStatsVO(
                totalArticles,
                publishedArticles,
                hiddenArticles,
                totalTags,
                totalCategories,
                totalComments,
                pendingComments,
                totalViews
        );
    }
}
