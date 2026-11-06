package com.liuyang.myblog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 是否把异常原始信息回传给客户端。
     * 默认关闭：线上环境一旦把 SQL 异常、堆栈片段透出去，等于对外泄露表结构与内部实现。
     * 仅在 dev 配置文件里显式打开。
     */
    @Value("${blog.api.expose-error-detail:false}")
    private boolean exposeErrorDetail;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        // 业务异常属于预期内的分支，记录 warn 便于排查参数问题，但不打堆栈
        log.warn("业务异常 code={}, message={}", e.getCode(), e.getMessage());
        return ResponseEntity.status(resolveStatus(e.getCode()))
                .body(ApiResponse.fail(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 未预期异常必须落日志并带完整堆栈，否则线上问题没有任何线索可查
        log.error("系统异常: {}", e.getMessage(), e);
        String message = exposeErrorDetail ? e.getMessage() : "服务器内部错误";
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail(500, message));
    }

    /**
     * 客户端请求本身有问题（路径参数类型错误、缺少必填参数、请求体格式错误、上传超限）
     * 统一归为 4xx：这类错误不是服务端故障，不该占用 500 的告警通道。
     */
    @ExceptionHandler({
            org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.http.converter.HttpMessageNotReadableException.class,
            org.springframework.web.multipart.MaxUploadSizeExceededException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleClientRequestException(Exception e) {
        log.warn("请求参数异常: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, "请求参数不合法"));
    }

    /**
     * 把业务码映射为真实的 HTTP 状态码，
     * 这样 Nginx、APM、前端 axios 拦截器都能按状态码识别错误，而不是永远 200。
     */
    private HttpStatus resolveStatus(Integer code) {
        if (code == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        HttpStatus status = HttpStatus.resolve(code);
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
