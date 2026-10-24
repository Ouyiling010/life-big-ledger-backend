package com.life.ledger.service;

import com.life.ledger.dto.AccountAdjustDTO;
import com.life.ledger.dto.AccountDTO;
import com.life.ledger.entity.Account;
import com.life.ledger.vo.AccountVO;

import java.util.List;

/**
 * 账户服务
 * 说明：本接口中「上下文相关」的方法由 Bill 等模块调用，Account 模块可在此接口上继续补充 CRUD 方法。
 */
public interface AccountService {

    /** 当前上下文（个人/家庭）下的账户列表 */
    List<Account> listByCurrentContext();

    /** 校验账户属于当前上下文，返回账户实体 */
    Account getOwnedAccount(Long accountId);

    /** 重算单个账户余额（记账推算口径） */
    void recalcBalance(Long accountId);

    /** 重算所有账户余额（定时任务使用） */
    void recalcAllBalance();

    /** 记账口径总资产（分） */
    long totalAsset();

    /** 当前上下文账户列表（含推算余额） */
    List<AccountVO> listAccounts();

    /** 新建账户 */
    AccountVO createAccount(AccountDTO dto);

    /** 编辑账户 */
    void updateAccount(Long id, AccountDTO dto);

    /** 删除账户（有账单引用时禁止） */
    void deleteAccount(Long id);

    /** 余额校正：差额生成调整账单后重算余额 */
    AccountVO adjustBalance(Long id, AccountAdjustDTO dto);
}
