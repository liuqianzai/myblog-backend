package com.liuyang.myblog.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章批量导入响应对象
 *
 * 数据来源：
 * 【计算/逻辑/其他】：totalCount, successCount, failCount, records
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleImportVO {
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private List<ArticleImportItemVO> records;
}
