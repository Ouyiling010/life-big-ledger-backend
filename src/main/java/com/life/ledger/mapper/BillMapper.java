package com.life.ledger.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.life.ledger.entity.Bill;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 账单 Mapper
 * 说明：回收站相关方法需要绕过逻辑删除过滤，故使用自定义 SQL。
 */
public interface BillMapper extends BaseMapper<Bill> {

    /** 账户流水推算：收入 - 支出 - 转出 + 转入（分） */
    @Select("SELECT IFNULL(SUM(CASE WHEN type = 2 AND account_id = #{accountId} THEN amount ELSE 0 END), 0) "
            + "- IFNULL(SUM(CASE WHEN type = 1 AND account_id = #{accountId} THEN amount ELSE 0 END), 0) "
            + "- IFNULL(SUM(CASE WHEN type = 3 AND account_id = #{accountId} THEN amount ELSE 0 END), 0) "
            + "+ IFNULL(SUM(CASE WHEN type = 3 AND to_account_id = #{accountId} THEN amount ELSE 0 END), 0) "
            + "FROM bill WHERE deleted = 0 AND (account_id = #{accountId} OR to_account_id = #{accountId})")
    Long sumAccountFlow(@Param("accountId") Long accountId);

    /** 智能分类：按关键字匹配历史账单最近使用的分类（限定收支类型，避免收入匹配到支出分类） */
    @Select("SELECT category_id FROM bill WHERE deleted = 0 AND category_id IS NOT NULL "
            + "AND owner_type = #{ownerType} AND user_id = #{userId} AND type = #{type} "
            + "AND ((#{familyId} IS NULL AND family_id IS NULL) OR (#{familyId} IS NOT NULL AND family_id = #{familyId})) "
            + "AND remark LIKE CONCAT('%', #{keyword}, '%') "
            + "ORDER BY happened_at DESC LIMIT 1")
    Long selectLastCategoryIdByKeyword(@Param("userId") Long userId,
                                       @Param("ownerType") Integer ownerType,
                                       @Param("familyId") Long familyId,
                                       @Param("keyword") String keyword,
                                       @Param("type") Integer type);

    /** 按指纹查重（CSV 导入去重） */
    @Select("SELECT COUNT(*) FROM bill WHERE deleted = 0 AND user_id = #{userId} AND fingerprint = #{fingerprint}")
    int countByFingerprint(@Param("userId") Long userId, @Param("fingerprint") String fingerprint);

    /** 回收站分页 */
    @Select("SELECT * FROM bill WHERE deleted = 1 AND "
            + "((#{familyId} IS NULL AND user_id = #{userId} AND family_id IS NULL) "
            + " OR (#{familyId} IS NOT NULL AND family_id = #{familyId})) "
            + "ORDER BY update_time DESC")
    IPage<Bill> selectRecycleBin(IPage<Bill> page, @Param("userId") Long userId, @Param("familyId") Long familyId);

    @Select("SELECT * FROM bill WHERE id = #{id} AND deleted = 1")
    Bill selectDeletedById(@Param("id") Long id);

    @Update("UPDATE bill SET deleted = 0, update_time = NOW() WHERE id = #{id} AND deleted = 1")
    int restoreById(@Param("id") Long id);

    @Delete("DELETE FROM bill WHERE id = #{id}")
    int physicalDeleteById(@Param("id") Long id);

    @Delete("DELETE FROM bill WHERE deleted = 1 AND update_time < #{before}")
    int purgeDeletedBefore(@Param("before") LocalDateTime before);

    /** 统计某账本/账户/分类被引用数量（含回收站中的账单，避免恢复后引用已删除对象） */
    @Select("SELECT COUNT(*) FROM bill WHERE book_id = #{bookId}")
    int countByBook(@Param("bookId") Long bookId);

    @Select("SELECT COUNT(*) FROM bill WHERE account_id = #{accountId} OR to_account_id = #{accountId}")
    int countByAccount(@Param("accountId") Long accountId);

    @Select("SELECT COUNT(*) FROM bill WHERE category_id = #{categoryId}")
    int countByCategory(@Param("categoryId") Long categoryId);

    /** 导出用：按条件查询全部账单 */
    @Select("<script>"
            + "SELECT * FROM bill WHERE deleted = 0 "
            + "<if test='userIds != null and userIds.size() > 0'> AND user_id IN "
            + "<foreach collection='userIds' item='uid' open='(' separator=',' close=')'>#{uid}</foreach></if>"
            + " ORDER BY happened_at DESC"
            + "</script>")
    List<Bill> selectForExport(@Param("userIds") List<Long> userIds);
}
