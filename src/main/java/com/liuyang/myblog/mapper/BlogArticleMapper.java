package com.liuyang.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liuyang.myblog.domain.po.BlogArticle;
import com.liuyang.myblog.domain.vo.ArchiveVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BlogArticleMapper extends BaseMapper<BlogArticle> {
    @Select("""
            SELECT DATE_FORMAT(create_time, '%Y-%m') AS month, COUNT(*) AS count
            FROM blog_article
            WHERE status = 1
            GROUP BY DATE_FORMAT(create_time, '%Y-%m')
            ORDER BY month DESC
            """)
    List<ArchiveVO> selectArchives();
}
