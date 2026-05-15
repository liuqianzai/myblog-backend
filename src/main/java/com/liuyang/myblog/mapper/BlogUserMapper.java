package com.liuyang.myblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liuyang.myblog.domain.po.BlogUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BlogUserMapper extends BaseMapper<BlogUser> {
}
