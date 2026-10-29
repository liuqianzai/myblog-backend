package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.vo.ArticleSimpleVO;

import java.util.List;

/**
 * 智能体文章推荐服务接口
 */
public interface AgentRecommendService {
    /**
     * 获取与指定文章最相关的推荐列表(Item-to-Item)
     *
     * @param articleId 目标文章ID
     * @param limit 条数
     * @return 推荐文章列表
     */
    List<ArticleSimpleVO> getRelatedArticles(Long articleId, Integer limit);

    /**
     * 获取探索/热度精选推荐列表(冷启动/首页时间衰减推荐)
     *
     * @param limit 条数
     * @return 推荐文章列表
     */
    List<ArticleSimpleVO> getExploreArticles(Integer limit);

    /**
     * 根据业务语义标签获取最匹配的推荐文章(如运势关键词、测验结果标签)
     *
     * @param metaTags 语义标签列表
     * @param excludeArticleId 排除的文章ID
     * @param limit 条数
     * @return 推荐文章列表
     */
    List<ArticleSimpleVO> getArticlesByMetaTags(List<String> metaTags, Long excludeArticleId, Integer limit);
}
