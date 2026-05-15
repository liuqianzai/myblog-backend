package com.liuyang.myblog.domain.dto;

import lombok.Data;

@Data
public class FriendLinkDTO {
    private String name;
    private String url;
    private String avatar;
    private String description;
    private Integer sort;
    private Boolean status;
}
