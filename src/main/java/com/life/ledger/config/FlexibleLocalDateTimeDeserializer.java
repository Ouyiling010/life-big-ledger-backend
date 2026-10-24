package com.life.ledger.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * LocalDateTime 宽松反序列化器。
 * 依次兼容以下写法：
 * 1. yyyy-MM-dd HH:mm:ss（可带毫秒）
 * 2. ISO-8601 本地时间：2026-10-24T12:00:00（可带毫秒）
 * 3. 带时区的 ISO-8601：2026-10-24T02:15:24.846Z、2026-10-24T10:15:24+08:00（换算为服务器时区）
 * 4. 纯日期：2026-10-24（按当天 00:00:00 处理）
 */
public class FlexibleLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter SPACE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter SPACE_FMT_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (text == null) {
            return null;
        }
        text = text.trim();
        if (text.isEmpty()) {
            return null;
        }

        // 纯日期
        if (text.length() == 10) {
            try {
                return LocalDate.parse(text).atStartOfDay();
            } catch (DateTimeParseException ignored) {
                // 继续尝试其它格式
            }
        }

        // 带时区（Z 或 ±HH:mm），按瞬时换算到服务器时区
        try {
            return OffsetDateTime.parse(text)
                    .atZoneSameInstant(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (DateTimeParseException ignored) {
            // 继续尝试其它格式
        }

        // ISO-8601 本地时间，支持 T 分隔符与可选毫秒
        try {
            return LocalDateTime.parse(text);
        } catch (DateTimeParseException ignored) {
            // 继续尝试其它格式
        }

        // 空格分隔
        for (DateTimeFormatter fmt : new DateTimeFormatter[]{SPACE_FMT, SPACE_FMT_MS}) {
            try {
                return LocalDateTime.parse(text, fmt);
            } catch (DateTimeParseException ignored) {
                // 继续尝试其它格式
            }
        }

        // 兜底：能解析成瞬时即可（如纯数字时间戳）
        try {
            return LocalDateTime.ofInstant(Instant.parse(text), ZoneId.systemDefault());
        } catch (DateTimeParseException ignored) {
            // 所有格式均失败
        }

        throw ctxt.weirdStringException(text, LocalDateTime.class,
                "时间格式不正确，应为 yyyy-MM-dd HH:mm:ss 或 ISO-8601（如 2026-10-24T12:00:00），纯日期也支持 yyyy-MM-dd");
    }
}
