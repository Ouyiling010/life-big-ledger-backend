/*
 life-big-ledger database init script
 generated: 2026-10-22 (schema + demo data included)
 Execute this file directly: it creates the database, then imports all tables and data.
*/

CREATE DATABASE IF NOT EXISTS `life_big_ledger` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `life_big_ledger`;

-- MySQL dump 10.13  Distrib 8.0.39, for Win64 (x86_64)
--
-- Host: localhost    Database: life_big_ledger
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `account`
--

DROP TABLE IF EXISTS `account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_type` tinyint NOT NULL DEFAULT '1' COMMENT '1个人 2家庭',
  `user_id` bigint NOT NULL COMMENT '归属用户',
  `family_id` bigint DEFAULT NULL COMMENT '家庭ID',
  `creator_id` bigint NOT NULL COMMENT '创建人',
  `belong_member_id` bigint DEFAULT NULL COMMENT '归属成员(family_member.id)',
  `name` varchar(50) NOT NULL COMMENT '账户名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1现金 2储蓄卡 3信用卡 4虚拟',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `init_balance` bigint NOT NULL DEFAULT '0' COMMENT '用户填写的初始余额(分)',
  `balance` bigint NOT NULL DEFAULT '0' COMMENT '记账推算余额(分),非真实资金',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `include_in_total` tinyint NOT NULL DEFAULT '1' COMMENT '计入记账口径总资产',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_belong_member` (`belong_member_id`)
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `account`
--

LOCK TABLES `account` WRITE;
/*!40000 ALTER TABLE `account` DISABLE KEYS */;
INSERT INTO `account` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `belong_member_id`, `name`, `type`, `icon`, `init_balance`, `balance`, `remark`, `include_in_total`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (1,1,1,NULL,1,NULL,'现金',1,NULL,80000,80000,NULL,1,0,'2026-10-20 13:27:13','2026-10-22 13:02:09',0);
INSERT INTO `account` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `belong_member_id`, `name`, `type`, `icon`, `init_balance`, `balance`, `remark`, `include_in_total`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (2,1,1,NULL,1,NULL,'支付宝',4,NULL,500000,3067161,NULL,1,0,'2026-10-20 13:27:13','2026-10-22 13:02:09',0);
INSERT INTO `account` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `belong_member_id`, `name`, `type`, `icon`, `init_balance`, `balance`, `remark`, `include_in_total`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (3,1,1,NULL,1,NULL,'微信',4,NULL,200000,86371,NULL,1,0,'2026-10-20 13:27:13','2026-10-22 13:02:09',0);
INSERT INTO `account` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `belong_member_id`, `name`, `type`, `icon`, `init_balance`, `balance`, `remark`, `include_in_total`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (16,1,7,NULL,7,NULL,'现金',1,NULL,0,0,NULL,1,0,'2026-10-21 00:19:42','2026-10-21 00:19:42',0);
INSERT INTO `account` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `belong_member_id`, `name`, `type`, `icon`, `init_balance`, `balance`, `remark`, `include_in_total`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (17,1,7,NULL,7,NULL,'支付宝',4,NULL,0,0,NULL,1,0,'2026-10-21 00:19:42','2026-10-21 00:19:42',0);
INSERT INTO `account` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `belong_member_id`, `name`, `type`, `icon`, `init_balance`, `balance`, `remark`, `include_in_total`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (18,1,7,NULL,7,NULL,'微信',4,NULL,0,0,NULL,1,0,'2026-10-21 00:19:42','2026-10-21 00:19:42',0);
/*!40000 ALTER TABLE `account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_log`
--

DROP TABLE IF EXISTS `ai_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户',
  `scene` varchar(30) NOT NULL COMMENT '场景:parse_bill/analyze',
  `input_text` text COMMENT '输入',
  `output_text` text COMMENT '输出',
  `model` varchar(50) DEFAULT NULL COMMENT '模型名',
  `tokens` int DEFAULT NULL COMMENT '消耗token',
  `cost_ms` int DEFAULT NULL COMMENT '耗时(毫秒)',
  `success` tinyint NOT NULL DEFAULT '1' COMMENT '是否成功',
  `error_msg` varchar(500) DEFAULT NULL COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_scene` (`user_id`,`scene`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI调用日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_log`
--

LOCK TABLES `ai_log` WRITE;
/*!40000 ALTER TABLE `ai_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bill`
--

DROP TABLE IF EXISTS `bill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_type` tinyint NOT NULL DEFAULT '1' COMMENT '1个人 2家庭',
  `user_id` bigint NOT NULL COMMENT '归属用户',
  `family_id` bigint DEFAULT NULL COMMENT '家庭ID',
  `creator_id` bigint NOT NULL COMMENT '记账人(user_id或family_member.id)',
  `member_id` bigint DEFAULT NULL COMMENT '为谁花的(family_member.id)',
  `book_id` bigint NOT NULL COMMENT '所属账本',
  `account_id` bigint NOT NULL COMMENT '账户',
  `to_account_id` bigint DEFAULT NULL COMMENT '转入账户(仅记录转账用)',
  `category_id` bigint DEFAULT NULL COMMENT '分类',
  `type` tinyint NOT NULL COMMENT '1支出 2收入 3转账',
  `amount` bigint NOT NULL COMMENT '金额(分)',
  `currency` varchar(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `happened_at` datetime NOT NULL COMMENT '账单发生时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `tags` varchar(255) DEFAULT NULL COMMENT '标签(逗号分隔)',
  `image` varchar(255) DEFAULT NULL COMMENT '图片',
  `is_excluded` tinyint NOT NULL DEFAULT '0' COMMENT '不计入统计',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源:1手动 2CSV导入 3AI 4模板',
  `fingerprint` varchar(120) DEFAULT NULL COMMENT 'CSV 导入去重指纹',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`happened_at`),
  KEY `idx_family_time` (`family_id`,`happened_at`),
  KEY `idx_book_time` (`book_id`,`happened_at`),
  KEY `idx_user_cat` (`user_id`,`category_id`),
  KEY `idx_account` (`account_id`),
  KEY `idx_creator` (`creator_id`),
  KEY `idx_member` (`member_id`),
  KEY `idx_type` (`type`),
  KEY `idx_deleted_time` (`deleted`,`happened_at`),
  KEY `idx_fingerprint` (`user_id`,`fingerprint`)
) ENGINE=InnoDB AUTO_INCREMENT=215 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill`
--

LOCK TABLES `bill` WRITE;
/*!40000 ALTER TABLE `bill` DISABLE KEYS */;
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (1,1,1,NULL,1,NULL,1,2,NULL,43,2,1200000,'CNY','2026-07-10 09:30:00','工资',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (2,1,1,NULL,1,NULL,1,2,NULL,24,1,350000,'CNY','2026-07-01 10:00:00','房租','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (3,1,1,NULL,1,NULL,1,2,NULL,25,1,31832,'CNY','2026-07-05 20:00:00','水电燃气','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (4,1,1,NULL,1,NULL,1,2,NULL,26,1,15000,'CNY','2026-07-06 10:00:00','物业费','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (5,1,1,NULL,1,NULL,1,3,NULL,46,2,13852,'CNY','2026-07-20 09:00:00','理财收益','理财',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (6,1,1,NULL,1,NULL,1,3,NULL,12,1,1108,'CNY','2026-07-09 08:03:00','早餐 豆浆油条',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (7,1,1,NULL,1,NULL,1,2,NULL,12,1,808,'CNY','2026-07-31 07:58:00','早餐 肠粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (8,1,1,NULL,1,NULL,1,2,NULL,12,1,830,'CNY','2026-07-26 08:20:00','早餐 肠粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (9,1,1,NULL,1,NULL,1,2,NULL,12,1,1795,'CNY','2026-07-02 07:31:00','早餐 包子豆浆',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (10,1,1,NULL,1,NULL,1,2,NULL,12,1,1485,'CNY','2026-07-27 08:09:00','早餐 豆浆油条',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (11,1,1,NULL,1,NULL,1,3,NULL,12,1,1301,'CNY','2026-07-03 07:43:00','早餐 煎饼果子',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (12,1,1,NULL,1,NULL,1,3,NULL,13,1,1924,'CNY','2026-07-06 11:23:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (13,1,1,NULL,1,NULL,1,2,NULL,13,1,1880,'CNY','2026-07-30 12:26:00','食堂套餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (14,1,1,NULL,1,NULL,1,3,NULL,13,1,1987,'CNY','2026-07-06 13:36:00','公司楼下快餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (15,1,1,NULL,1,NULL,1,3,NULL,13,1,3763,'CNY','2026-07-23 11:27:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (16,1,1,NULL,1,NULL,1,3,NULL,13,1,3960,'CNY','2026-07-27 12:48:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (17,1,1,NULL,1,NULL,1,2,NULL,13,1,2735,'CNY','2026-07-10 12:30:00','牛肉面','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (18,1,1,NULL,1,NULL,1,2,NULL,13,1,2368,'CNY','2026-07-14 12:06:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (19,1,1,NULL,1,NULL,1,2,NULL,13,1,2765,'CNY','2026-07-05 11:39:00','公司楼下快餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (20,1,1,NULL,1,NULL,1,2,NULL,15,1,4286,'CNY','2026-07-26 21:41:00','肯德基',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (21,1,1,NULL,1,NULL,1,3,NULL,15,1,4043,'CNY','2026-07-19 20:31:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (22,1,1,NULL,1,NULL,1,3,NULL,15,1,4417,'CNY','2026-07-08 19:08:00','美团外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (23,1,1,NULL,1,NULL,1,2,NULL,15,1,3491,'CNY','2026-07-18 21:20:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (24,1,1,NULL,1,NULL,1,3,NULL,16,1,2895,'CNY','2026-07-25 16:10:00','便利店零食',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (25,1,1,NULL,1,NULL,1,3,NULL,16,1,2137,'CNY','2026-07-10 20:56:00','便利店零食',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (26,1,1,NULL,1,NULL,1,3,NULL,16,1,1858,'CNY','2026-07-23 18:03:00','水果',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (27,1,1,NULL,1,NULL,1,3,NULL,17,1,751,'CNY','2026-07-23 07:57:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (28,1,1,NULL,1,NULL,1,3,NULL,17,1,743,'CNY','2026-07-10 07:55:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (29,1,1,NULL,1,NULL,1,3,NULL,17,1,679,'CNY','2026-07-11 08:11:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (30,1,1,NULL,1,NULL,1,3,NULL,17,1,524,'CNY','2026-07-14 08:19:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (31,1,1,NULL,1,NULL,1,3,NULL,17,1,830,'CNY','2026-07-25 07:38:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (32,1,1,NULL,1,NULL,1,3,NULL,17,1,818,'CNY','2026-07-12 09:12:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (33,1,1,NULL,1,NULL,1,3,NULL,17,1,752,'CNY','2026-07-31 08:00:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (34,1,1,NULL,1,NULL,1,3,NULL,17,1,525,'CNY','2026-07-21 07:15:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (35,1,1,NULL,1,NULL,1,2,NULL,18,1,2657,'CNY','2026-07-08 22:26:00','出租车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (36,1,1,NULL,1,NULL,1,2,NULL,18,1,2085,'CNY','2026-07-12 19:55:00','滴滴打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (37,1,1,NULL,1,NULL,1,2,NULL,18,1,2261,'CNY','2026-07-02 16:49:00','滴滴打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (38,1,1,NULL,1,NULL,1,3,NULL,21,1,13025,'CNY','2026-07-12 18:51:00','洗衣液',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (39,1,1,NULL,1,NULL,1,3,NULL,21,1,10555,'CNY','2026-07-08 21:52:00','超市日用品',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (40,1,1,NULL,1,NULL,1,2,NULL,21,1,3118,'CNY','2026-07-19 14:20:00','洗衣液',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (41,1,1,NULL,1,NULL,1,2,NULL,22,1,31998,'CNY','2026-07-13 16:12:00','秋装外套',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (42,1,1,NULL,1,NULL,1,2,NULL,27,1,8101,'CNY','2026-07-25 21:50:00','IMAX电影',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (43,1,1,NULL,1,NULL,1,3,NULL,28,1,4245,'CNY','2026-07-14 21:13:00','Steam游戏',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (44,1,1,NULL,1,NULL,1,2,NULL,31,1,2294,'CNY','2026-07-13 18:13:00','感冒药',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (45,1,1,NULL,1,NULL,1,2,NULL,32,1,7792,'CNY','2026-07-07 13:29:00','小说',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (46,1,1,NULL,1,NULL,1,2,NULL,33,1,19095,'CNY','2026-07-25 17:06:00','培训班',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (47,1,1,NULL,1,NULL,1,2,NULL,8,1,32426,'CNY','2026-07-20 12:27:00','同事结婚礼金',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (48,1,1,NULL,1,NULL,1,3,NULL,10,1,18184,'CNY','2026-07-25 14:28:00','猫砂',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (49,1,1,NULL,1,NULL,1,2,NULL,9,1,23337,'CNY','2026-07-25 11:22:00','奶粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (50,1,1,NULL,1,NULL,1,3,NULL,34,1,7358,'CNY','2026-07-03 17:59:00','其他杂项',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (51,1,1,NULL,1,NULL,1,3,NULL,47,2,20000,'CNY','2026-07-18 12:00:00','微信红包',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (52,1,1,NULL,1,NULL,1,2,3,NULL,3,94832,'CNY','2026-07-03 09:00:00','支付宝转入微信','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (53,1,1,NULL,1,NULL,1,3,2,NULL,3,77393,'CNY','2026-07-22 09:00:00','微信转回支付宝','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (54,1,1,NULL,1,NULL,1,2,NULL,43,2,1200000,'CNY','2026-08-10 09:30:00','工资',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (55,1,1,NULL,1,NULL,1,2,NULL,24,1,350000,'CNY','2026-08-01 10:00:00','房租','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (56,1,1,NULL,1,NULL,1,2,NULL,25,1,22694,'CNY','2026-08-05 20:00:00','水电燃气','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (57,1,1,NULL,1,NULL,1,2,NULL,26,1,15000,'CNY','2026-08-06 10:00:00','物业费','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (58,1,1,NULL,1,NULL,1,3,NULL,46,2,17935,'CNY','2026-08-20 09:00:00','理财收益','理财',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (59,1,1,NULL,1,NULL,1,3,NULL,12,1,1611,'CNY','2026-08-02 09:01:00','早餐 三明治',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (60,1,1,NULL,1,NULL,1,2,NULL,12,1,1892,'CNY','2026-08-25 09:35:00','早餐 煎饼果子',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (61,1,1,NULL,1,NULL,1,3,NULL,12,1,1213,'CNY','2026-08-01 07:03:00','早餐 肠粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (62,1,1,NULL,1,NULL,1,2,NULL,12,1,1702,'CNY','2026-08-04 07:46:00','早餐 肠粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (63,1,1,NULL,1,NULL,1,2,NULL,12,1,1283,'CNY','2026-08-15 07:19:00','早餐 三明治',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (64,1,1,NULL,1,NULL,1,3,NULL,12,1,1407,'CNY','2026-08-22 09:29:00','早餐 包子豆浆',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (65,1,1,NULL,1,NULL,1,2,NULL,13,1,3323,'CNY','2026-08-22 13:50:00','轻食沙拉','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (66,1,1,NULL,1,NULL,1,2,NULL,13,1,3112,'CNY','2026-08-22 12:38:00','食堂套餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (67,1,1,NULL,1,NULL,1,3,NULL,13,1,3594,'CNY','2026-08-20 12:17:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (68,1,1,NULL,1,NULL,1,3,NULL,13,1,1922,'CNY','2026-08-28 12:05:00','食堂套餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (69,1,1,NULL,1,NULL,1,2,NULL,13,1,3535,'CNY','2026-08-19 13:26:00','公司楼下快餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (70,1,1,NULL,1,NULL,1,3,NULL,13,1,2287,'CNY','2026-08-27 13:50:00','牛肉面','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (71,1,1,NULL,1,NULL,1,3,NULL,13,1,3805,'CNY','2026-08-28 11:56:00','公司楼下快餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (72,1,1,NULL,1,NULL,1,3,NULL,13,1,1967,'CNY','2026-08-24 13:31:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (73,1,1,NULL,1,NULL,1,2,NULL,15,1,4993,'CNY','2026-08-07 18:06:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (74,1,1,NULL,1,NULL,1,2,NULL,15,1,4492,'CNY','2026-08-19 18:18:00','美团外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (75,1,1,NULL,1,NULL,1,2,NULL,15,1,3866,'CNY','2026-08-05 20:58:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (76,1,1,NULL,1,NULL,1,3,NULL,15,1,4199,'CNY','2026-08-31 19:38:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (77,1,1,NULL,1,NULL,1,3,NULL,16,1,1980,'CNY','2026-08-04 19:46:00','便利店零食',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (78,1,1,NULL,1,NULL,1,3,NULL,16,1,2443,'CNY','2026-08-09 15:29:00','咖啡',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (79,1,1,NULL,1,NULL,1,3,NULL,16,1,2045,'CNY','2026-08-04 17:40:00','便利店零食',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (80,1,1,NULL,1,NULL,1,3,NULL,17,1,746,'CNY','2026-08-19 09:57:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (81,1,1,NULL,1,NULL,1,3,NULL,17,1,486,'CNY','2026-08-04 08:50:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (82,1,1,NULL,1,NULL,1,3,NULL,17,1,440,'CNY','2026-08-09 09:26:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (83,1,1,NULL,1,NULL,1,3,NULL,17,1,658,'CNY','2026-08-31 08:12:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (84,1,1,NULL,1,NULL,1,3,NULL,17,1,505,'CNY','2026-08-03 08:44:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (85,1,1,NULL,1,NULL,1,3,NULL,17,1,641,'CNY','2026-08-31 09:29:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (86,1,1,NULL,1,NULL,1,3,NULL,17,1,822,'CNY','2026-08-17 09:40:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (87,1,1,NULL,1,NULL,1,3,NULL,17,1,667,'CNY','2026-08-28 08:32:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (88,1,1,NULL,1,NULL,1,2,NULL,18,1,1856,'CNY','2026-08-03 15:04:00','滴滴打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (89,1,1,NULL,1,NULL,1,2,NULL,18,1,2408,'CNY','2026-08-15 08:02:00','出租车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (90,1,1,NULL,1,NULL,1,2,NULL,18,1,2982,'CNY','2026-08-14 17:01:00','高德打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (91,1,1,NULL,1,NULL,1,3,NULL,21,1,10695,'CNY','2026-08-19 13:04:00','纸巾',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (92,1,1,NULL,1,NULL,1,2,NULL,21,1,10107,'CNY','2026-08-26 14:39:00','纸巾',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (93,1,1,NULL,1,NULL,1,3,NULL,21,1,7710,'CNY','2026-08-21 17:26:00','纸巾',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (94,1,1,NULL,1,NULL,1,2,NULL,22,1,52067,'CNY','2026-08-13 18:48:00','秋装外套',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (95,1,1,NULL,1,NULL,1,2,NULL,27,1,7808,'CNY','2026-08-25 21:24:00','电影票',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (96,1,1,NULL,1,NULL,1,3,NULL,28,1,5242,'CNY','2026-08-16 21:31:00','手游充值',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (97,1,1,NULL,1,NULL,1,2,NULL,31,1,7327,'CNY','2026-08-30 14:25:00','药店购药',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (98,1,1,NULL,1,NULL,1,2,NULL,32,1,9401,'CNY','2026-08-17 19:18:00','小说',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (99,1,1,NULL,1,NULL,1,2,NULL,33,1,31518,'CNY','2026-08-29 18:17:00','在线课程',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (100,1,1,NULL,1,NULL,1,2,NULL,8,1,22381,'CNY','2026-08-27 13:51:00','同事结婚礼金',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (101,1,1,NULL,1,NULL,1,3,NULL,10,1,9274,'CNY','2026-08-06 15:12:00','宠物疫苗',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (102,1,1,NULL,1,NULL,1,2,NULL,9,1,9222,'CNY','2026-08-27 18:49:00','玩具',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (103,1,1,NULL,1,NULL,1,3,NULL,34,1,8976,'CNY','2026-08-12 21:29:00','其他杂项',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (104,1,1,NULL,1,NULL,1,2,NULL,44,2,200000,'CNY','2026-08-15 10:00:00','绩效奖金',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (105,1,1,NULL,1,NULL,1,2,NULL,29,1,120000,'CNY','2026-08-16 09:00:00','周边自驾游','旅游',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (106,1,1,NULL,1,NULL,1,2,3,NULL,3,88877,'CNY','2026-08-03 09:00:00','支付宝转入微信','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (107,1,1,NULL,1,NULL,1,3,2,NULL,3,63560,'CNY','2026-08-22 09:00:00','微信转回支付宝','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (108,1,1,NULL,1,NULL,1,2,NULL,43,2,1200000,'CNY','2026-09-10 09:30:00','工资',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (109,1,1,NULL,1,NULL,1,2,NULL,24,1,350000,'CNY','2026-09-01 10:00:00','房租','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (110,1,1,NULL,1,NULL,1,2,NULL,25,1,24348,'CNY','2026-09-05 20:00:00','水电燃气','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (111,1,1,NULL,1,NULL,1,2,NULL,26,1,15000,'CNY','2026-09-06 10:00:00','物业费','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (112,1,1,NULL,1,NULL,1,3,NULL,46,2,7278,'CNY','2026-09-20 09:00:00','理财收益','理财',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (113,1,1,NULL,1,NULL,1,2,NULL,12,1,1707,'CNY','2026-09-12 08:57:00','早餐 煎饼果子',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (114,1,1,NULL,1,NULL,1,2,NULL,12,1,1365,'CNY','2026-09-02 08:01:00','早餐 豆浆油条',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (115,1,1,NULL,1,NULL,1,2,NULL,12,1,968,'CNY','2026-09-10 09:30:00','早餐 包子豆浆',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (116,1,1,NULL,1,NULL,1,2,NULL,12,1,967,'CNY','2026-09-24 07:49:00','早餐 煎饼果子',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (117,1,1,NULL,1,NULL,1,2,NULL,12,1,1294,'CNY','2026-09-26 07:34:00','早餐 豆浆油条',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (118,1,1,NULL,1,NULL,1,3,NULL,12,1,1025,'CNY','2026-09-26 08:34:00','早餐 肠粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (119,1,1,NULL,1,NULL,1,2,NULL,13,1,3375,'CNY','2026-09-14 13:35:00','牛肉面','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (120,1,1,NULL,1,NULL,1,3,NULL,13,1,2420,'CNY','2026-09-16 11:12:00','牛肉面','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (121,1,1,NULL,1,NULL,1,3,NULL,13,1,3286,'CNY','2026-09-18 12:02:00','牛肉面','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (122,1,1,NULL,1,NULL,1,2,NULL,13,1,2679,'CNY','2026-09-28 13:34:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (123,1,1,NULL,1,NULL,1,2,NULL,13,1,3019,'CNY','2026-09-27 11:02:00','轻食沙拉','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (124,1,1,NULL,1,NULL,1,3,NULL,13,1,1872,'CNY','2026-09-18 13:39:00','食堂套餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (125,1,1,NULL,1,NULL,1,3,NULL,13,1,3397,'CNY','2026-09-17 12:37:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (126,1,1,NULL,1,NULL,1,3,NULL,13,1,3424,'CNY','2026-09-07 12:25:00','轻食沙拉','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (127,1,1,NULL,1,NULL,1,2,NULL,15,1,4717,'CNY','2026-09-05 20:16:00','美团外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (128,1,1,NULL,1,NULL,1,2,NULL,15,1,2650,'CNY','2026-09-04 21:28:00','美团外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (129,1,1,NULL,1,NULL,1,2,NULL,15,1,3350,'CNY','2026-09-07 20:24:00','美团外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (130,1,1,NULL,1,NULL,1,3,NULL,15,1,4363,'CNY','2026-09-01 21:00:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (131,1,1,NULL,1,NULL,1,3,NULL,16,1,2940,'CNY','2026-09-24 17:07:00','水果',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (132,1,1,NULL,1,NULL,1,3,NULL,16,1,2615,'CNY','2026-09-04 21:08:00','便利店零食',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (133,1,1,NULL,1,NULL,1,3,NULL,16,1,1431,'CNY','2026-09-23 17:23:00','咖啡',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (134,1,1,NULL,1,NULL,1,3,NULL,17,1,767,'CNY','2026-09-08 08:53:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (135,1,1,NULL,1,NULL,1,3,NULL,17,1,492,'CNY','2026-09-11 07:57:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (136,1,1,NULL,1,NULL,1,3,NULL,17,1,592,'CNY','2026-09-26 09:47:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (137,1,1,NULL,1,NULL,1,3,NULL,17,1,458,'CNY','2026-09-01 07:35:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (138,1,1,NULL,1,NULL,1,3,NULL,17,1,652,'CNY','2026-09-08 07:58:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (139,1,1,NULL,1,NULL,1,3,NULL,17,1,531,'CNY','2026-09-10 08:07:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (140,1,1,NULL,1,NULL,1,3,NULL,17,1,516,'CNY','2026-09-03 08:03:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (141,1,1,NULL,1,NULL,1,3,NULL,17,1,550,'CNY','2026-09-26 08:06:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (142,1,1,NULL,1,NULL,1,2,NULL,18,1,4601,'CNY','2026-09-13 14:00:00','滴滴打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (143,1,1,NULL,1,NULL,1,2,NULL,18,1,2990,'CNY','2026-09-05 16:37:00','高德打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (144,1,1,NULL,1,NULL,1,2,NULL,18,1,2740,'CNY','2026-09-07 10:36:00','出租车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (145,1,1,NULL,1,NULL,1,2,NULL,21,1,7666,'CNY','2026-09-08 16:45:00','纸巾',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (146,1,1,NULL,1,NULL,1,2,NULL,21,1,13176,'CNY','2026-09-02 10:46:00','洗衣液',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (147,1,1,NULL,1,NULL,1,3,NULL,21,1,8587,'CNY','2026-09-12 15:38:00','超市日用品',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (148,1,1,NULL,1,NULL,1,2,NULL,22,1,59348,'CNY','2026-09-08 10:00:00','T恤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (149,1,1,NULL,1,NULL,1,2,NULL,27,1,6470,'CNY','2026-09-19 20:32:00','IMAX电影',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (150,1,1,NULL,1,NULL,1,3,NULL,28,1,6230,'CNY','2026-09-21 20:03:00','手游充值',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (151,1,1,NULL,1,NULL,1,2,NULL,31,1,6865,'CNY','2026-09-04 19:11:00','药店购药',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (152,1,1,NULL,1,NULL,1,2,NULL,32,1,6827,'CNY','2026-09-18 20:45:00','技术书籍',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (153,1,1,NULL,1,NULL,1,2,NULL,33,1,27741,'CNY','2026-09-20 11:23:00','培训班',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (154,1,1,NULL,1,NULL,1,2,NULL,8,1,32815,'CNY','2026-09-12 15:26:00','同事结婚礼金',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (155,1,1,NULL,1,NULL,1,3,NULL,10,1,13469,'CNY','2026-09-10 13:19:00','宠物疫苗',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (156,1,1,NULL,1,NULL,1,2,NULL,9,1,21160,'CNY','2026-09-13 17:59:00','绘本',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (157,1,1,NULL,1,NULL,1,3,NULL,34,1,9001,'CNY','2026-09-24 11:49:00','其他杂项',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (158,1,1,NULL,1,NULL,1,2,NULL,44,2,150000,'CNY','2026-09-15 10:00:00','绩效奖金',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (159,1,1,NULL,1,NULL,1,2,3,NULL,3,58108,'CNY','2026-09-03 09:00:00','支付宝转入微信','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (160,1,1,NULL,1,NULL,1,3,2,NULL,3,43501,'CNY','2026-09-22 09:00:00','微信转回支付宝','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (161,1,1,NULL,1,NULL,1,2,NULL,43,2,1200000,'CNY','2026-10-10 09:30:00','工资',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (162,1,1,NULL,1,NULL,1,2,NULL,24,1,350000,'CNY','2026-10-01 10:00:00','房租','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (163,1,1,NULL,1,NULL,1,2,NULL,25,1,28232,'CNY','2026-10-05 20:00:00','水电燃气','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (164,1,1,NULL,1,NULL,1,2,NULL,26,1,15000,'CNY','2026-10-06 10:00:00','物业费','居住',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (165,1,1,NULL,1,NULL,1,3,NULL,46,2,11649,'CNY','2026-10-20 09:00:00','理财收益','理财',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (166,1,1,NULL,1,NULL,1,2,NULL,12,1,844,'CNY','2026-10-03 08:58:00','早餐 包子豆浆',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (167,1,1,NULL,1,NULL,1,2,NULL,12,1,1687,'CNY','2026-10-07 09:22:00','早餐 三明治',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (168,1,1,NULL,1,NULL,1,2,NULL,12,1,1559,'CNY','2026-10-19 09:05:00','早餐 包子豆浆',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (169,1,1,NULL,1,NULL,1,3,NULL,12,1,940,'CNY','2026-10-16 07:31:00','早餐 三明治',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (170,1,1,NULL,1,NULL,1,3,NULL,12,1,1401,'CNY','2026-10-18 08:29:00','早餐 三明治',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (171,1,1,NULL,1,NULL,1,3,NULL,12,1,1004,'CNY','2026-10-12 08:53:00','早餐 煎饼果子',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (172,1,1,NULL,1,NULL,1,2,NULL,13,1,3301,'CNY','2026-10-10 12:55:00','食堂套餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (173,1,1,NULL,1,NULL,1,2,NULL,13,1,3980,'CNY','2026-10-10 11:58:00','公司楼下快餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (174,1,1,NULL,1,NULL,1,2,NULL,13,1,3768,'CNY','2026-10-11 13:00:00','食堂套餐','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (175,1,1,NULL,1,NULL,1,3,NULL,13,1,2609,'CNY','2026-10-11 11:31:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (176,1,1,NULL,1,NULL,1,2,NULL,13,1,2621,'CNY','2026-10-06 12:39:00','牛肉面','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (177,1,1,NULL,1,NULL,1,2,NULL,13,1,2712,'CNY','2026-10-16 12:46:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (178,1,1,NULL,1,NULL,1,3,NULL,13,1,3371,'CNY','2026-10-05 11:57:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (179,1,1,NULL,1,NULL,1,2,NULL,13,1,1802,'CNY','2026-10-12 13:08:00','黄焖鸡米饭','工作餐',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (180,1,1,NULL,1,NULL,1,2,NULL,15,1,3253,'CNY','2026-10-04 21:49:00','麦当劳',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (181,1,1,NULL,1,NULL,1,3,NULL,15,1,4768,'CNY','2026-10-04 21:19:00','麦当劳',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (182,1,1,NULL,1,NULL,1,2,NULL,15,1,4670,'CNY','2026-10-08 19:46:00','麦当劳',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (183,1,1,NULL,1,NULL,1,2,NULL,15,1,4064,'CNY','2026-10-03 18:16:00','饿了么外卖',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (184,1,1,NULL,1,NULL,1,3,NULL,16,1,2200,'CNY','2026-10-09 21:57:00','咖啡',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (185,1,1,NULL,1,NULL,1,3,NULL,16,1,2067,'CNY','2026-10-06 19:12:00','奶茶',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (186,1,1,NULL,1,NULL,1,3,NULL,16,1,1848,'CNY','2026-10-13 16:50:00','水果',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (187,1,1,NULL,1,NULL,1,3,NULL,17,1,769,'CNY','2026-10-09 08:40:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (188,1,1,NULL,1,NULL,1,3,NULL,17,1,443,'CNY','2026-10-05 09:16:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (189,1,1,NULL,1,NULL,1,3,NULL,17,1,530,'CNY','2026-10-02 09:29:00','地铁通勤',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (190,1,1,NULL,1,NULL,1,3,NULL,17,1,784,'CNY','2026-10-19 08:02:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (191,1,1,NULL,1,NULL,1,3,NULL,17,1,783,'CNY','2026-10-14 07:12:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (192,1,1,NULL,1,NULL,1,3,NULL,17,1,486,'CNY','2026-10-17 07:48:00','公交',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (193,1,1,NULL,1,NULL,1,3,NULL,17,1,781,'CNY','2026-10-20 07:47:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (194,1,1,NULL,1,NULL,1,3,NULL,17,1,878,'CNY','2026-10-06 09:10:00','地铁',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (195,1,1,NULL,1,NULL,1,2,NULL,18,1,2425,'CNY','2026-10-20 13:07:00','滴滴打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (196,1,1,NULL,1,NULL,1,2,NULL,18,1,3812,'CNY','2026-10-15 17:20:00','出租车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (197,1,1,NULL,1,NULL,1,2,NULL,18,1,2326,'CNY','2026-10-19 20:01:00','滴滴打车',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (198,1,1,NULL,1,NULL,1,3,NULL,21,1,6249,'CNY','2026-10-10 16:56:00','纸巾',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (199,1,1,NULL,1,NULL,1,2,NULL,21,1,13143,'CNY','2026-10-21 10:19:00','超市日用品',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (200,1,1,NULL,1,NULL,1,3,NULL,21,1,9942,'CNY','2026-10-13 18:48:00','超市日用品',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (201,1,1,NULL,1,NULL,1,2,NULL,22,1,43261,'CNY','2026-10-02 17:11:00','秋装外套',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (202,1,1,NULL,1,NULL,1,2,NULL,27,1,5501,'CNY','2026-10-09 18:44:00','IMAX电影',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (203,1,1,NULL,1,NULL,1,3,NULL,28,1,6929,'CNY','2026-10-20 20:59:00','手游充值',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (204,1,1,NULL,1,NULL,1,2,NULL,31,1,6037,'CNY','2026-10-06 11:05:00','药店购药',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (205,1,1,NULL,1,NULL,1,2,NULL,32,1,11936,'CNY','2026-10-07 17:04:00','小说',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (206,1,1,NULL,1,NULL,1,2,NULL,33,1,36027,'CNY','2026-10-08 20:26:00','在线课程',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (207,1,1,NULL,1,NULL,1,2,NULL,8,1,22071,'CNY','2026-10-02 13:42:00','朋友生日红包',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (208,1,1,NULL,1,NULL,1,3,NULL,10,1,8893,'CNY','2026-10-03 13:01:00','猫砂',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (209,1,1,NULL,1,NULL,1,2,NULL,9,1,14741,'CNY','2026-10-13 17:11:00','奶粉',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (210,1,1,NULL,1,NULL,1,3,NULL,34,1,3153,'CNY','2026-10-11 19:35:00','其他杂项',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (211,1,1,NULL,1,NULL,1,2,NULL,48,2,30000,'CNY','2026-10-12 14:00:00','差旅报销',NULL,NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (212,1,1,NULL,1,NULL,1,2,NULL,30,1,60000,'CNY','2026-10-09 10:00:00','门诊挂号','医疗',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (213,1,1,NULL,1,NULL,1,2,3,NULL,3,93715,'CNY','2026-10-03 09:00:00','支付宝转入微信','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `member_id`, `book_id`, `account_id`, `to_account_id`, `category_id`, `type`, `amount`, `currency`, `happened_at`, `remark`, `tags`, `image`, `is_excluded`, `source`, `fingerprint`, `create_time`, `update_time`, `deleted`) VALUES (214,1,1,NULL,1,NULL,1,3,2,NULL,3,42258,'CNY','2026-10-22 09:00:00','微信转回支付宝','转账',NULL,0,1,NULL,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
/*!40000 ALTER TABLE `bill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `bill_template`
--

DROP TABLE IF EXISTS `bill_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `bill_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户',
  `name` varchar(50) NOT NULL COMMENT '模板名称',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1支出 2收入 3转账',
  `amount` bigint DEFAULT NULL COMMENT '默认金额(分)',
  `category_id` bigint DEFAULT NULL COMMENT '默认分类',
  `account_id` bigint DEFAULT NULL COMMENT '默认账户',
  `book_id` bigint DEFAULT NULL COMMENT '默认账本',
  `remark` varchar(255) DEFAULT NULL COMMENT '默认备注',
  `use_count` int NOT NULL DEFAULT '0' COMMENT '使用次数',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='快捷记账模板表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `bill_template`
--

LOCK TABLES `bill_template` WRITE;
/*!40000 ALTER TABLE `bill_template` DISABLE KEYS */;
INSERT INTO `bill_template` (`id`, `user_id`, `name`, `type`, `amount`, `category_id`, `account_id`, `book_id`, `remark`, `use_count`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (1,1,'早餐',1,1000,12,3,1,'包子豆浆',12,1,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill_template` (`id`, `user_id`, `name`, `type`, `amount`, `category_id`, `account_id`, `book_id`, `remark`, `use_count`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (2,1,'通勤地铁',1,600,17,3,1,'地铁通勤',35,2,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
INSERT INTO `bill_template` (`id`, `user_id`, `name`, `type`, `amount`, `category_id`, `account_id`, `book_id`, `remark`, `use_count`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (3,1,'咖啡',1,2500,16,2,1,'瑞幸咖啡',8,3,'2026-10-22 13:02:09','2026-10-22 13:02:09',0);
/*!40000 ALTER TABLE `bill_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `book` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_type` tinyint NOT NULL DEFAULT '1' COMMENT '1个人 2家庭',
  `user_id` bigint NOT NULL COMMENT '归属用户',
  `family_id` bigint DEFAULT NULL COMMENT '家庭ID',
  `creator_id` bigint NOT NULL COMMENT '创建人',
  `name` varchar(50) NOT NULL COMMENT '账本名称',
  `cover` varchar(255) DEFAULT NULL COMMENT '封面',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `currency` varchar(10) NOT NULL DEFAULT 'CNY' COMMENT '币种',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_family_id` (`family_id`),
  KEY `idx_owner_type` (`owner_type`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='账本表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `name`, `cover`, `icon`, `currency`, `description`, `is_default`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (1,1,1,NULL,1,'日常账本',NULL,NULL,'CNY',NULL,1,0,'2026-10-20 13:27:13','2026-10-20 13:27:13',0);
INSERT INTO `book` (`id`, `owner_type`, `user_id`, `family_id`, `creator_id`, `name`, `cover`, `icon`, `currency`, `description`, `is_default`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (6,1,7,NULL,7,'日常账本',NULL,NULL,'CNY',NULL,1,0,'2026-10-21 00:19:42','2026-10-21 00:19:42',0);
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `owner_type` tinyint NOT NULL DEFAULT '1' COMMENT '0系统 1个人 2家庭',
  `user_id` bigint DEFAULT NULL COMMENT '归属用户',
  `family_id` bigint DEFAULT NULL COMMENT '家庭ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID',
  `name` varchar(30) NOT NULL COMMENT '分类名称',
  `icon` varchar(50) DEFAULT NULL COMMENT '图标',
  `color` varchar(20) DEFAULT NULL COMMENT '颜色',
  `type` tinyint NOT NULL DEFAULT '1' COMMENT '1支出 2收入',
  `is_system` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统预置',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_type` (`user_id`,`type`),
  KEY `idx_family_type` (`family_id`,`type`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB AUTO_INCREMENT=50 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (1,0,NULL,NULL,0,'餐饮','food','#FF6B6B',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (2,0,NULL,NULL,0,'交通','car','#4ECDC4',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (3,0,NULL,NULL,0,'购物','shopping','#FFE66D',1,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (4,0,NULL,NULL,0,'居住','home','#95E1D3',1,1,4,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (5,0,NULL,NULL,0,'娱乐','game','#F38181',1,1,5,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (6,0,NULL,NULL,0,'医疗','hospital','#AA96DA',1,1,6,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (7,0,NULL,NULL,0,'学习','book','#FCBAD3',1,1,7,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (8,0,NULL,NULL,0,'人情','gift','#FFB6B9',1,1,8,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (9,0,NULL,NULL,0,'育儿','baby','#B5EAD7',1,1,9,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (10,0,NULL,NULL,0,'宠物','pet','#C7CEEA',1,1,10,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (11,0,NULL,NULL,0,'其他','more','#CCCCCC',1,1,99,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (12,0,NULL,NULL,1,'早餐','food','#FF6B6B',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (13,0,NULL,NULL,1,'午餐','food','#FF6B6B',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (14,0,NULL,NULL,1,'晚餐','food','#FF6B6B',1,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (15,0,NULL,NULL,1,'外卖','food','#FF6B6B',1,1,4,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (16,0,NULL,NULL,1,'零食','food','#FF6B6B',1,1,5,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (17,0,NULL,NULL,2,'公交地铁','car','#4ECDC4',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (18,0,NULL,NULL,2,'打车','car','#4ECDC4',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (19,0,NULL,NULL,2,'加油','car','#4ECDC4',1,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (20,0,NULL,NULL,2,'停车','car','#4ECDC4',1,1,4,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (21,0,NULL,NULL,3,'日用品','shopping','#FFE66D',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (22,0,NULL,NULL,3,'服饰','shopping','#FFE66D',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (23,0,NULL,NULL,3,'数码','shopping','#FFE66D',1,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (24,0,NULL,NULL,4,'房租','home','#95E1D3',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (25,0,NULL,NULL,4,'水电燃气','home','#95E1D3',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (26,0,NULL,NULL,4,'物业','home','#95E1D3',1,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (27,0,NULL,NULL,5,'电影','game','#F38181',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (28,0,NULL,NULL,5,'游戏','game','#F38181',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (29,0,NULL,NULL,5,'旅游','game','#F38181',1,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (30,0,NULL,NULL,6,'门诊','hospital','#AA96DA',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (31,0,NULL,NULL,6,'药品','hospital','#AA96DA',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (32,0,NULL,NULL,7,'书籍','book','#FCBAD3',1,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (33,0,NULL,NULL,7,'课程','book','#FCBAD3',1,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (34,0,NULL,NULL,11,'其他','more','#CCCCCC',1,1,99,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (43,0,NULL,NULL,0,'工资','salary','#52C41A',2,1,1,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (44,0,NULL,NULL,0,'奖金','award','#52C41A',2,1,2,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (45,0,NULL,NULL,0,'兼职','work','#52C41A',2,1,3,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (46,0,NULL,NULL,0,'理财','money','#52C41A',2,1,4,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (47,0,NULL,NULL,0,'红包','redpack','#52C41A',2,1,5,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (48,0,NULL,NULL,0,'报销','refund','#52C41A',2,1,6,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
INSERT INTO `category` (`id`, `owner_type`, `user_id`, `family_id`, `parent_id`, `name`, `icon`, `color`, `type`, `is_system`, `sort`, `create_time`, `update_time`, `deleted`) VALUES (49,0,NULL,NULL,0,'其他','more','#CCCCCC',2,1,99,'2026-10-20 10:14:07','2026-10-20 10:14:07',0);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `family`
--

DROP TABLE IF EXISTS `family`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '家庭名称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '家庭头像',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `owner_id` bigint NOT NULL COMMENT '创建者用户ID',
  `invite_code` varchar(20) NOT NULL COMMENT '邀请码',
  `invite_expire_time` datetime DEFAULT NULL COMMENT '邀请码过期时间',
  `member_count` int NOT NULL DEFAULT '1' COMMENT '成员数量',
  `max_member` int NOT NULL DEFAULT '10' COMMENT '最大成员数',
  `notice` varchar(500) DEFAULT NULL COMMENT '家庭公告',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invite_code` (`invite_code`),
  KEY `idx_owner_id` (`owner_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家庭组表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family`
--

LOCK TABLES `family` WRITE;
/*!40000 ALTER TABLE `family` DISABLE KEYS */;
/*!40000 ALTER TABLE `family` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `family_member`
--

DROP TABLE IF EXISTS `family_member`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `family_member` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `family_id` bigint NOT NULL COMMENT '家庭ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role` tinyint NOT NULL DEFAULT '3' COMMENT '1OWNER 2ADMIN 3MEMBER',
  `nickname_in_family` varchar(50) DEFAULT NULL COMMENT '家庭内昵称',
  `avatar_in_family` varchar(255) DEFAULT NULL COMMENT '家庭内头像',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0已退出',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_family_user` (`family_id`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='家庭成员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `family_member`
--

LOCK TABLES `family_member` WRITE;
/*!40000 ALTER TABLE `family_member` DISABLE KEYS */;
/*!40000 ALTER TABLE `family_member` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收人',
  `type` varchar(30) NOT NULL COMMENT '类型:family_invite/member_change/new_bill',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` varchar(500) DEFAULT NULL COMMENT '内容',
  `ref_id` bigint DEFAULT NULL COMMENT '关联业务ID',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否已读',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_read` (`user_id`,`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `operation_log`
--

DROP TABLE IF EXISTS `operation_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '操作人',
  `family_id` bigint DEFAULT NULL COMMENT '家庭ID(家庭操作)',
  `module` varchar(30) NOT NULL COMMENT '模块:bill/book/account/family',
  `action` varchar(30) NOT NULL COMMENT '动作:create/update/delete',
  `target_id` bigint DEFAULT NULL COMMENT '目标ID',
  `detail` varchar(1000) DEFAULT NULL COMMENT '详情',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP',
  `cost_ms` int DEFAULT NULL COMMENT '耗时',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`,`create_time`),
  KEY `idx_family_time` (`family_id`,`create_time`),
  KEY `idx_module_action` (`module`,`action`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='操作日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `operation_log`
--

LOCK TABLES `operation_log` WRITE;
/*!40000 ALTER TABLE `operation_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `operation_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码(BCrypt)',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '0未知 1男 2女',
  `default_family_id` bigint DEFAULT NULL COMMENT '默认家庭ID',
  `current_context` tinyint NOT NULL DEFAULT '1' COMMENT '1个人 2家庭',
  `current_family_id` bigint DEFAULT NULL COMMENT '当前家庭ID',
  `has_guided` tinyint NOT NULL DEFAULT '0' COMMENT '是否已完成新手引导',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1正常 0禁用',
  `is_admin` tinyint NOT NULL DEFAULT '0' COMMENT '是否系统管理员',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_is_admin` (`is_admin`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `phone`, `gender`, `default_family_id`, `current_context`, `current_family_id`, `has_guided`, `status`, `is_admin`, `create_time`, `update_time`, `deleted`) VALUES (1,'admin','$2a$10$avMooUxjfHT5XOQ/GSm03ugB9/kpLokJ3kNgovrSa420axXmuaWMq','超级管理员',NULL,'admin@qq.com',NULL,0,NULL,1,NULL,0,1,1,'2026-10-20 13:31:42','2026-10-22 12:47:49',0);
INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `avatar`, `email`, `phone`, `gender`, `default_family_id`, `current_context`, `current_family_id`, `has_guided`, `status`, `is_admin`, `create_time`, `update_time`, `deleted`) VALUES (7,'user','$2a$10$avMooUxjfHT5XOQ/GSm03ugB9/kpLokJ3kNgovrSa420axXmuaWMq','user',NULL,'user@qq.com','15361949587',0,NULL,1,NULL,0,1,0,'2026-10-21 00:19:42','2026-10-21 00:19:42',0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-22 13:04:30
