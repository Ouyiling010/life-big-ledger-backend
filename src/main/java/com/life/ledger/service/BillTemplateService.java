package com.life.ledger.service;

import com.life.ledger.dto.BillTemplateDTO;
import com.life.ledger.vo.BillTemplateVO;

import java.util.List;

/**
 * 快捷记账模板服务
 */
public interface BillTemplateService {

    /** 当前用户的模板列表 */
    List<BillTemplateVO> list();

    /** 新建模板 */
    BillTemplateVO create(BillTemplateDTO dto);

    /** 编辑模板 */
    void update(Long id, BillTemplateDTO dto);

    /** 删除模板 */
    void delete(Long id);

    /** 使用模板：use_count + 1 并返回模板 */
    BillTemplateVO use(Long id);
}
