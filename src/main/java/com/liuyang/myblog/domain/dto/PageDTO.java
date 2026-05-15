package com.liuyang.myblog.domain.dto;

import lombok.Data;

@Data
public class PageDTO {
    private String title;
    private String slug;
    private String content;
    private Boolean status;
}
