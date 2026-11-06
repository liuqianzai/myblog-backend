package com.liuyang.myblog.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentReviewDTO {
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
}
