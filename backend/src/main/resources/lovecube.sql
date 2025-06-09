/*
 Navicat Premium Data Transfer

 Source Server         : love
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : rm-2zereaqi1k536nd38zo.mysql.rds.aliyuncs.com:3306
 Source Schema         : lovecube

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 09/06/2025 14:30:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for chatmessages
-- ----------------------------
DROP TABLE IF EXISTS `chatmessages`;
CREATE TABLE `chatmessages`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `receiver_id` bigint NULL DEFAULT NULL,
  `sender_id` bigint NULL DEFAULT NULL,
  `timestamp` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chatmessages
-- ----------------------------
INSERT INTO `chatmessages` VALUES (1, '123', b'0', 2, 4, 1747790059424);
INSERT INTO `chatmessages` VALUES (2, '3456', b'0', 2, 4, 1747790064069);
INSERT INTO `chatmessages` VALUES (3, '12421', b'0', 2, 4, 1747793533369);
INSERT INTO `chatmessages` VALUES (4, '3594', b'0', 2, 4, 1747794406474);
INSERT INTO `chatmessages` VALUES (5, '333', b'0', 2, 4, 1747994449866);

-- ----------------------------
-- Table structure for greetings
-- ----------------------------
DROP TABLE IF EXISTS `greetings`;
CREATE TABLE `greetings`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `receiver_id` bigint NOT NULL,
  `sender_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of greetings
-- ----------------------------

-- ----------------------------
-- Table structure for matchfilters
-- ----------------------------
DROP TABLE IF EXISTS `matchfilters`;
CREATE TABLE `matchfilters`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `age_range` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `gender` int NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of matchfilters
-- ----------------------------

-- ----------------------------
-- Table structure for matchrecords
-- ----------------------------
DROP TABLE IF EXISTS `matchrecords`;
CREATE TABLE `matchrecords`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `match_score` double NULL DEFAULT NULL,
  `matched_user_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of matchrecords
-- ----------------------------
INSERT INTO `matchrecords` VALUES (1, '2025-05-20 18:05:05.105000', 0.6849219440694392, 2, 4);
INSERT INTO `matchrecords` VALUES (2, '2025-05-21 08:43:50.555000', 0.4201941178352545, 2, 4);
INSERT INTO `matchrecords` VALUES (3, '2025-05-21 09:05:13.569000', 0.5736446332422682, 2, 4);
INSERT INTO `matchrecords` VALUES (4, '2025-05-21 09:12:13.929000', 0.3012443713715093, 2, 4);
INSERT INTO `matchrecords` VALUES (5, '2025-05-21 09:14:13.291000', 0.42495609327695727, 2, 4);
INSERT INTO `matchrecords` VALUES (6, '2025-05-21 09:16:07.692000', 0.206248954077504, 2, 4);
INSERT INTO `matchrecords` VALUES (7, '2025-05-21 09:17:12.795000', 0.685760930132317, 2, 4);
INSERT INTO `matchrecords` VALUES (8, '2025-05-21 09:19:49.526000', 0.6359763128901212, 2, 4);
INSERT INTO `matchrecords` VALUES (9, '2025-05-21 09:21:14.773000', 0.5406585487412197, 2, 4);
INSERT INTO `matchrecords` VALUES (10, '2025-05-21 10:03:03.076000', 0.8709930355054439, 2, 4);
INSERT INTO `matchrecords` VALUES (11, '2025-05-21 10:08:18.069000', 0.2965714705080803, 2, 4);
INSERT INTO `matchrecords` VALUES (12, '2025-05-21 10:12:09.021000', 0.006594175221045773, 2, 4);
INSERT INTO `matchrecords` VALUES (13, '2025-05-21 10:26:38.930000', 0.4642809377510736, 2, 4);
INSERT INTO `matchrecords` VALUES (14, '2025-05-21 11:19:44.862000', 0.005149612007852911, 2, 4);
INSERT INTO `matchrecords` VALUES (15, '2025-05-21 14:18:04.352000', 0.5701337806845621, 2, 4);
INSERT INTO `matchrecords` VALUES (16, '2025-05-23 18:00:43.952000', 0.23717447697307537, 2, 4);

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `userid` bigint NOT NULL AUTO_INCREMENT,
  `age` int NULL DEFAULT NULL,
  `bio` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `birth_date` datetime(6) NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `gender` int NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `occupation` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `openid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `phone_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `profile_photo` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`userid`) USING BTREE,
  UNIQUE INDEX `UKbadofxhbq3oi2d4u7fj8w1kt8`(`openid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (2, 21, '热爱旅行，喜欢摄影', '2003-01-15 00:00:00.000000', '2025-03-12 11:36:02.000000', 'zhangwei@example.com', 1, '北京', '程序员', 'wx_001', 'hashed_password', '18812345678', 'wxfile://tmp_8ba548485b07d94193c9f11afc444787.jpg', '2025-03-12 11:36:02.000000', '张伟');
INSERT INTO `users` VALUES (3, 25, '健身达人，喜欢尝试新事物', '1999-05-20 00:00:00.000000', '2025-03-12 11:36:02.000000', 'lina@example.com', 2, '上海', '医生', 'wx_002', 'hashed_password', '18887654321', '/images/avatar2.png', '2025-03-12 11:36:02.000000', '李娜');
INSERT INTO `users` VALUES (4, 28, NULL, NULL, NULL, NULL, 1, '上海', NULL, 'oD_yL7Wkbg4v_Cmp5aaEMXEqPNzc', NULL, '5555555555', 'http://192.168.1.5:8090/admin/uploads/avatar/a28b8bc3-d7c1-43f4-9790-9455556331d0.jpg', NULL, '用户3636');

SET FOREIGN_KEY_CHECKS = 1;
