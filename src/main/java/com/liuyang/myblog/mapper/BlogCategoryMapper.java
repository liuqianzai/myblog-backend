package com.liuyang.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liuyang.myblog.domain.po.BlogCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogCategoryMapper extends BaseMapper<BlogCategory> {
}
