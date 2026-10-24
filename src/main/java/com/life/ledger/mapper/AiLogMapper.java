package com.life.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.life.ledger.entity.AiLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

public interface AiLogMapper extends BaseMapper<AiLog> {

    @Delete("DELETE FROM ai_log WHERE create_time < #{before}")
    int deleteBefore(@Param("before") LocalDateTime before);
}
