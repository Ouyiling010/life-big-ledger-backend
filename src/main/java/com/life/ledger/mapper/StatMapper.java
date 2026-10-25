package com.life.ledger.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 统计聚合查询
 */
public interface StatMapper {

    @Select("""
            SELECT 
                IFNULL(SUM(CASE WHEN type = 2 THEN amount ELSE 0 END), 0) AS totalIncome, 
                IFNULL(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS totalExpense, 
                IFNULL(SUM(CASE WHEN type = 3 THEN 1 ELSE 0 END), 0) AS transferCount, 
                COUNT(*) AS billCount 
            FROM bill 
            WHERE deleted = 0 AND is_excluded = 0 
              AND owner_type = #{ownerType} 
              AND (family_id = #{familyId} OR (family_id IS NULL AND user_id = #{userId})) 
              AND (#{bookId} IS NULL OR book_id = #{bookId}) 
              AND happened_at >= #{start} AND happened_at < #{end}
            """)
    Map<String, Object> overview(@Param("ownerType") Integer ownerType,
                                 @Param("familyId") Long familyId,
                                 @Param("userId") Long userId,
                                 @Param("bookId") Long bookId,
                                 @Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);

    @Select("""
            SELECT 
                b.category_id AS categoryId, 
                c.name AS categoryName, 
                c.icon AS icon, 
                c.color AS color, 
                IFNULL(SUM(b.amount), 0) AS amount, 
                COUNT(*) AS billCount 
            FROM bill b 
            LEFT JOIN category c ON c.id = b.category_id 
            WHERE b.deleted = 0 AND b.is_excluded = 0 AND b.type = #{type} 
              AND b.owner_type = #{ownerType} 
              AND (b.family_id = #{familyId} OR (b.family_id IS NULL AND b.user_id = #{userId})) 
              AND (#{bookId} IS NULL OR b.book_id = #{bookId}) 
              AND b.happened_at >= #{start} AND b.happened_at < #{end} 
            GROUP BY b.category_id, c.name, c.icon, c.color 
            ORDER BY amount DESC
            """)
    List<Map<String, Object>> categoryStat(@Param("ownerType") Integer ownerType,
                                           @Param("familyId") Long familyId,
                                           @Param("userId") Long userId,
                                           @Param("bookId") Long bookId,
                                           @Param("type") Integer type,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Select("""
            SELECT 
                DATE_FORMAT(happened_at, #{fmt}) AS date, 
                IFNULL(SUM(CASE WHEN type = 2 THEN amount ELSE 0 END), 0) AS income, 
                IFNULL(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS expense 
            FROM bill 
            WHERE deleted = 0 AND is_excluded = 0 
              AND owner_type = #{ownerType} 
              AND (family_id = #{familyId} OR (family_id IS NULL AND user_id = #{userId})) 
              AND (#{bookId} IS NULL OR book_id = #{bookId}) 
              AND happened_at >= #{start} AND happened_at < #{end} 
            GROUP BY DATE_FORMAT(happened_at, #{fmt}) 
            ORDER BY date
            """)
    List<Map<String, Object>> trend(@Param("ownerType") Integer ownerType,
                                    @Param("familyId") Long familyId,
                                    @Param("userId") Long userId,
                                    @Param("bookId") Long bookId,
                                    @Param("fmt") String fmt,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    @Select("""
            SELECT 
                DATE(happened_at) AS date, 
                IFNULL(SUM(CASE WHEN type = 1 THEN amount ELSE 0 END), 0) AS expense 
            FROM bill 
            WHERE deleted = 0 AND is_excluded = 0 
              AND owner_type = #{ownerType} 
              AND (family_id = #{familyId} OR (family_id IS NULL AND user_id = #{userId})) 
              AND (#{bookId} IS NULL OR book_id = #{bookId}) 
              AND happened_at >= #{start} AND happened_at < #{end} 
            GROUP BY DATE(happened_at) 
            ORDER BY date
            """)
    List<Map<String, Object>> calendar(@Param("ownerType") Integer ownerType,
                                       @Param("familyId") Long familyId,
                                       @Param("userId") Long userId,
                                       @Param("bookId") Long bookId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Select("""
            SELECT 
                a.id AS accountId, 
                a.name AS accountName, 
                a.type AS accountType, 
                a.icon AS icon, 
                a.init_balance AS initBalance, 
                a.include_in_total AS includeInTotal, 
                a.init_balance 
                + IFNULL(SUM(CASE WHEN b.type = 2 AND b.account_id = a.id THEN b.amount ELSE 0 END), 0) 
                - IFNULL(SUM(CASE WHEN b.type = 1 AND b.account_id = a.id THEN b.amount ELSE 0 END), 0) 
                - IFNULL(SUM(CASE WHEN b.type = 3 AND b.account_id = a.id THEN b.amount ELSE 0 END), 0) 
                + IFNULL(SUM(CASE WHEN b.type = 3 AND b.to_account_id = a.id THEN b.amount ELSE 0 END), 0) AS balance 
            FROM account a 
            LEFT JOIN bill b ON (b.account_id = a.id OR b.to_account_id = a.id) AND b.deleted = 0 
            WHERE a.deleted = 0 AND a.owner_type = #{ownerType} 
              AND (a.family_id = #{familyId} OR (a.family_id IS NULL AND a.user_id = #{userId})) 
            GROUP BY a.id, a.name, a.type, a.icon, a.init_balance, a.include_in_total
            """)
    List<Map<String, Object>> accountBalance(@Param("ownerType") Integer ownerType,
                                             @Param("familyId") Long familyId,
                                             @Param("userId") Long userId);

    @Select("""
            SELECT 
                bk.id AS bookId, 
                bk.name AS bookName, 
                bk.icon AS icon, 
                IFNULL(SUM(CASE WHEN x.type = 1 THEN x.amount ELSE 0 END), 0) AS expense, 
                IFNULL(SUM(CASE WHEN x.type = 2 THEN x.amount ELSE 0 END), 0) AS income, 
                COUNT(x.id) AS billCount 
            FROM book bk 
            LEFT JOIN bill x ON x.book_id = bk.id AND x.deleted = 0 AND x.is_excluded = 0 
            WHERE bk.deleted = 0 AND bk.owner_type = #{ownerType} 
              AND (bk.family_id = #{familyId} OR (bk.family_id IS NULL AND bk.user_id = #{userId})) 
            GROUP BY bk.id, bk.name, bk.icon 
            ORDER BY expense DESC
            """)
    List<Map<String, Object>> bookStat(@Param("ownerType") Integer ownerType,
                                       @Param("familyId") Long familyId,
                                       @Param("userId") Long userId);

    @Select("""
            SELECT 
                fm.id AS memberId, 
                fm.nickname_in_family AS memberName, 
                u.nickname AS nickname, 
                u.avatar AS avatar, 
                IFNULL(SUM(b.amount), 0) AS totalExpense 
            FROM family_member fm 
            LEFT JOIN user u ON u.id = fm.user_id 
            LEFT JOIN bill b ON b.member_id = fm.id AND b.family_id = fm.family_id AND b.type = 1 
              AND b.deleted = 0 AND b.is_excluded = 0 
              AND b.happened_at >= #{start} AND b.happened_at < #{end} 
            WHERE fm.family_id = #{familyId} AND fm.status = 1 
            GROUP BY fm.id, fm.nickname_in_family, u.nickname, u.avatar 
            ORDER BY totalExpense DESC
            """)
    List<Map<String, Object>> memberStat(@Param("familyId") Long familyId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Select("""
            SELECT 
                c.id AS categoryId, 
                c.name AS categoryName, 
                IFNULL(SUM(b.amount), 0) AS amount, 
                COUNT(b.id) AS billCount 
            FROM category c 
            LEFT JOIN bill b ON b.category_id = c.id AND b.deleted = 0 AND b.is_excluded = 0 
              AND b.type = 1 AND b.owner_type = #{ownerType} 
              AND (b.family_id = #{familyId} OR (b.family_id IS NULL AND b.user_id = #{userId})) 
              AND b.happened_at >= #{start} AND b.happened_at < #{end} 
            WHERE c.deleted = 0 
              AND (c.owner_type = 0 OR (c.owner_type = #{ownerType} AND (c.family_id = #{familyId} OR (c.family_id IS NULL AND c.user_id = #{userId})))) 
            GROUP BY c.id, c.name 
            ORDER BY amount DESC 
            LIMIT 5
            """)
    List<Map<String, Object>> topCategories(@Param("ownerType") Integer ownerType,
                                            @Param("familyId") Long familyId,
                                            @Param("userId") Long userId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Select("""
            SELECT 
                b.id AS billId, 
                b.amount AS amount, 
                b.remark AS remark, 
                b.happened_at AS happenedAt, 
                c.name AS categoryName 
            FROM bill b 
            LEFT JOIN category c ON c.id = b.category_id 
            WHERE b.deleted = 0 AND b.is_excluded = 0 AND b.type = 1 
              AND b.owner_type = #{ownerType} 
              AND (b.family_id = #{familyId} OR (b.family_id IS NULL AND b.user_id = #{userId})) 
              AND b.happened_at >= #{start} AND b.happened_at < #{end} 
            ORDER BY b.amount DESC 
            LIMIT 1
            """)
    Map<String, Object> maxSingleExpense(@Param("ownerType") Integer ownerType,
                                         @Param("familyId") Long familyId,
                                         @Param("userId") Long userId,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
}