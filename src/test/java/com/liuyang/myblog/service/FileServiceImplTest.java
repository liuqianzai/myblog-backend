package com.liuyang.myblog.service;

import com.liuyang.myblog.common.BusinessException;
import com.liuyang.myblog.mapper.BlogFileMapper;
import com.liuyang.myblog.service.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Mock
    private BlogFileMapper blogFileMapper;

    private FileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        fileService = new FileServiceImpl(blogFileMapper);
        // 指向临时目录，避免测试往真实的 uploads 目录写文件
        ReflectionTestUtils.setField(fileService, "uploadDir",
                System.getProperty("java.io.tmpdir") + "/myblog-test-uploads");
    }

    @ParameterizedTest
    @ValueSource(strings = {"evil.svg", "evil.SVG", "payload.svg.js.svg"})
    @DisplayName("拒绝上传 svg：可内嵌脚本，与站点同源会形成存储型 XSS")
    void testSvgUploadRejected(String filename) {
        MockMultipartFile file = new MockMultipartFile(
                "file", filename, "image/svg+xml", "<svg onload=\"alert(1)\"/>".getBytes());

        BusinessException ex = assertThrows(BusinessException.class, () -> fileService.uploadImage(file));

        assertEquals(400, ex.getCode());
        verify(blogFileMapper, never()).insert(any());
    }

    @Test
    @DisplayName("拒绝没有扩展名的文件")
    void testFileWithoutExtensionRejected() {
        MockMultipartFile file = new MockMultipartFile("file", "noext", "text/plain", new byte[]{1});

        BusinessException ex = assertThrows(BusinessException.class, () -> fileService.uploadImage(file));

        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("空文件直接拒绝")
    void testEmptyFileRejected() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);

        BusinessException ex = assertThrows(BusinessException.class, () -> fileService.uploadImage(file));

        assertEquals(400, ex.getCode());
    }

    @Test
    @DisplayName("普通图片可以正常上传并落库")
    void testPngUploadSucceeds() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.PNG", "image/png", new byte[]{1, 2, 3});

        var result = fileService.uploadImage(file);

        assertNotNull(result.getUrl());
        assertTrue(result.getUrl().startsWith("/api/files/"));
        assertTrue(result.getFilename().endsWith(".png"));
        verify(blogFileMapper).insert(any());
    }
}
