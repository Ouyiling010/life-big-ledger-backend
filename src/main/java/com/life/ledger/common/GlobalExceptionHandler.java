package com.life.ledger.common;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 全局异常处理
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBiz(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLogin(NotLoginException e) {
        return Result.fail(401, "未登录或登录已过期");
    }

    @ExceptionHandler({NotPermissionException.class, NotRoleException.class})
    public Result<?> handleNoPermission(Exception e) {
        return Result.fail(403, "无权限");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.fail(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.fail(400, msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleConstraint(ConstraintViolationException e) {
        return Result.fail(400, e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<?> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.fail(400, "缺少参数：" + e.getParameterName());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public Result<?> handleMissingPathVar(MissingPathVariableException e) {
        return Result.fail(400, "缺少路径参数：" + e.getVariableName());
    }

    /** 查询参数类型转换失败，例如 accountId=<integer> 这类非法数字 */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<?> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String value = e.getValue() == null ? "" : String.valueOf(e.getValue());
        String expect = expectedTypeName(e.getRequiredType());
        log.warn("参数类型转换失败: {}={}", e.getName(), value);
        return Result.fail(400, "参数格式不正确：" + e.getName() + " 应为" + expect + "，当前值：" + value);
    }

    /** 请求体不是合法 JSON，或字段类型无法反序列化 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMostSpecificCause().getMessage());
        return Result.fail(400, "请求体格式不正确，请检查 JSON 结构与字段类型");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<?> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.fail(405, "请求方法不支持：" + e.getMethod());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<?> handleUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(400, "上传文件过大");
    }

    /** 把 Java 类型翻译成前端能看懂的中文提示 */
    private String expectedTypeName(Class<?> type) {
        if (type == null) {
            return "正确的格式";
        }
        if (type == Integer.class || type == int.class || type == Long.class || type == long.class
                || type == Short.class || type == short.class) {
            return "整数";
        }
        if (type == BigDecimal.class || type == Double.class || type == double.class) {
            return "数字";
        }
        if (type == Boolean.class || type == boolean.class) {
            return "true 或 false";
        }
        if (type == LocalDate.class) {
            return "日期（yyyy-MM-dd）";
        }
        if (type == LocalDateTime.class) {
            return "时间（yyyy-MM-dd HH:mm:ss）";
        }
        return type.getSimpleName();
    }

    /** 静态资源不存在（如浏览器自动请求 favicon.ico）：正常返回 404，不刷错误日志 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<?> handleNoResource(NoResourceFoundException e) {
        return Result.fail(404, "资源不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<?> handleAll(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统异常，请稍后再试");
    }
}
