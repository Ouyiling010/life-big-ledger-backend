package com.life.ledger.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量删除请求体 {"ids": [1,2,3]}
 */
@Data
public class BillBatchDeleteDTO implements Serializable {

    private List<Long> ids;
}
