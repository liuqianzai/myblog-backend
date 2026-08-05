package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件业务逻辑接口
 */
public interface FileService {/**
 * 上传图片
 */

    FileUploadVO uploadImage(MultipartFile file);
}
