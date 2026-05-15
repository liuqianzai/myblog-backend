package com.liuyang.myblog.service;

import com.liuyang.myblog.domain.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadVO uploadImage(MultipartFile file);
}
