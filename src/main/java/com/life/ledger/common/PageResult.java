package com.life.ledger.common;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页响应体
 */
@Data
public class PageResult<T> implements Serializable {

    private Long total;
    private Long pages;
    private Long current;
    private Long size;
    private List<T> records;

    public static <T> PageResult<T> of(long total, long current, long size, List<T> records) {
        PageResult<T> r = new PageResult<>();
        r.total = total;
        r.current = current;
        r.size = size;
        r.records = records == null ? new ArrayList<>() : records;
        r.pages = size <= 0 ? 0 : (total + size - 1) / size;
        return r;
    }
}
