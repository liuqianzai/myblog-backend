package com.liuyang.myblog.domain.dto;

import lombok.Data;

@Data
public class CategoryDTO {
    private String name;
    private String slug;
    private String description;
    private Integer sort;
}
