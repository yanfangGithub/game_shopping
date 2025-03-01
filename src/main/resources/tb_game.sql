/*
 Navicat Premium Data Transfer

 Source Server         : testconn
 Source Server Type    : MySQL
 Source Server Version : 80100
 Source Host           : localhost:3306
 Source Schema         : tb_game

 Target Server Type    : MySQL
 Target Server Version : 80100
 File Encoding         : 65001

 Date: 20/06/2024 17:56:10
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for exchange_code
-- ----------------------------
DROP TABLE IF EXISTS `exchange_code`;
CREATE TABLE `exchange_code`
(
    `id`       bigint(0)                                                     NOT NULL COMMENT '主键id',
    `good_id`  bigint(0)                                                     NULL DEFAULT NULL COMMENT '商品id',
    `code`     varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '兑换码',
    `resource` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '商品',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `good_id` (`good_id`) USING BTREE,
    CONSTRAINT `exchange_code_ibfk_1` FOREIGN KEY (`good_id`) REFERENCES `good` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow`
(
    `id`           bigint(0)                                                     NOT NULL AUTO_INCREMENT COMMENT 'id',
    `follower_id`  bigint(0)                                                     NULL DEFAULT NULL COMMENT '关注者id',
    `following_id` bigint(0)                                                     NULL DEFAULT NULL COMMENT '被关注者id',
    `follow_time`  varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '关注时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `follower_id` (`follower_id`) USING BTREE,
    INDEX `following_id` (`following_id`) USING BTREE,
    CONSTRAINT `follow_ibfk_1` FOREIGN KEY (`follower_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `follow_ibfk_2` FOREIGN KEY (`following_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  AUTO_INCREMENT = 1788864216167587841
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for good
-- ----------------------------
DROP TABLE IF EXISTS `good`;
CREATE TABLE `good`
(
    `id`          bigint(0)                                                      NOT NULL COMMENT '商品独立生成',
    `user_id`     bigint(0)                                                      NULL DEFAULT NULL COMMENT '发布用户id',
    `title`       varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci  NULL DEFAULT NULL COMMENT '商品标题',
    `status`      int(0)                                                         NULL DEFAULT NULL COMMENT '商品状态 -1下架 0 在售 1 售罄',
    `stock`       bigint(0)                                                      NULL DEFAULT NULL COMMENT '数量',
    `use_price`   int(0)                                                         NULL DEFAULT NULL COMMENT '建议价格 -1偏低 0 正常 1 偏高',
    `price`       double                                                         NULL DEFAULT NULL COMMENT '价格',
    `tags`        varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签，以逗号分隔开',
    `images`      varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `description` text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci          NULL COMMENT '商品描述',
    `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci  NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `good_fk_userid` (`user_id`) USING BTREE,
    CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for good_comment
-- ----------------------------
DROP TABLE IF EXISTS `good_comment`;
CREATE TABLE `good_comment`
(
    `id`          bigint(0)                                                     NOT NULL COMMENT 'id',
    `good_id`     bigint(0)                                                     NULL DEFAULT NULL COMMENT '商品id',
    `comment`     text CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci         NULL COMMENT '评价内容',
    `level`       int(0)                                                        NULL DEFAULT NULL COMMENT '等级',
    `user_id`     bigint(0)                                                     NULL DEFAULT NULL COMMENT '评价用户id',
    `create_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '评价时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `good_id` (`good_id`) USING BTREE,
    INDEX `good_user` (`user_id`) USING BTREE,
    CONSTRAINT `good_comment_ibfk_1` FOREIGN KEY (`good_id`) REFERENCES `good` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `fk_userid` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for good_vote
-- ----------------------------
DROP TABLE IF EXISTS `good_vote`;
CREATE TABLE `good_vote`
(
    `id`      int(10) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT COMMENT 'id',
    `good_id` bigint(0)                 NULL DEFAULT NULL COMMENT '商品id',
    `user_id` bigint(0)                 NULL DEFAULT NULL COMMENT '投票用户id',
    `number`  int(0)                    NULL DEFAULT NULL COMMENT '投票的值',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `vote_user_id` (`user_id`) USING BTREE,
    INDEX `vote_id` (`good_id`) USING BTREE,
    CONSTRAINT `vote_id` FOREIGN KEY (`good_id`) REFERENCES `good` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`
(
    `id`          bigint(0)                                                     NOT NULL COMMENT '订单ID',
    `good_id`     bigint(0)                                                     NULL DEFAULT NULL COMMENT '商品ID',
    `user_id`     bigint(0)                                                     NULL DEFAULT NULL COMMENT '卖家ID',
    `buyer_id`    bigint(0)                                                     NULL DEFAULT NULL COMMENT '买家ID',
    `price`       double                                                        NULL DEFAULT NULL COMMENT '交易金额',
    `status`      int(0)                                                        NULL DEFAULT NULL COMMENT '交易状态',
    `create_time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易时间',
    `images`      varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    `title`       varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `good_id` (`good_id`) USING BTREE,
    INDEX `user_id` (`user_id`) USING BTREE,
    INDEX `buyer_id` (`buyer_id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for shopping_car
-- ----------------------------
DROP TABLE IF EXISTS `shopping_car`;
CREATE TABLE `shopping_car`
(
    `id`          bigint(0)                                                     NOT NULL COMMENT '购物车id',
    `user_id`     bigint(0)                                                     NOT NULL COMMENT '用户id',
    `good_id`     bigint(0)                                                     NOT NULL COMMENT '商品id',
    `images`      varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
    `price`       decimal(10, 2)                                                NULL DEFAULT NULL,
    `title`       varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
    `create_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `user_id` (`user_id`) USING BTREE,
    INDEX `good_id` (`good_id`) USING BTREE,
    CONSTRAINT `shopping_car_ibfk_2` FOREIGN KEY (`good_id`) REFERENCES `good` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`          bigint(0)                                                     NOT NULL COMMENT 'id',
    `email`       varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL COMMENT '邮箱',
    `password`    varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '密码',
    `nick_name`   varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '昵称',
    `icon`        varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT '' COMMENT '头像',
    `name`        varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '实名姓名',
    `id_number`   varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '省份证号',
    `balance`     bigint(0)                                                     NULL DEFAULT NULL COMMENT '余额',
    `create_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '资料更新时间',
    PRIMARY KEY (`id`, `email`) USING BTREE,
    INDEX `id` (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1803128410744262657
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for userinfo
-- ----------------------------
DROP TABLE IF EXISTS `userinfo`;
CREATE TABLE `userinfo`
(
    `id`          bigint(0)                                                     NOT NULL COMMENT 'id',
    `email`       varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '邮箱',
    `introduce`   varchar(128) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '个人介绍',
    `fans`        int(0)                                                        NULL DEFAULT NULL COMMENT '粉丝数',
    `followee`    int(0)                                                        NULL DEFAULT NULL COMMENT '关注数',
    `sex`         tinyint(0)                                                    NULL DEFAULT NULL COMMENT '性别',
    `birthday`    date                                                          NULL DEFAULT NULL COMMENT '生日',
    `nick_name`   varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '昵称',
    `icon`        varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '头像',
    `create_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '创建时间',
    `update_time` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb3
  COLLATE = utf8mb3_general_ci
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
