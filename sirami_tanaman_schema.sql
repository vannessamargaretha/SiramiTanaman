/*
 Navicat Premium Dump SQL

 Source Server         : MySQL
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3307
 Source Schema         : sirami_tanaman

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 12/10/2025 15:19:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'admin', 'admin123', NULL, '2025-10-10 20:22:02');
INSERT INTO `user` VALUES (2, 'user', 'test123', 'tesst@gmail.com', '2025-10-11 23:33:37');
INSERT INTO `user` VALUES (3, 'user2', 'test123', 'tesst123@gmail.com', '2025-10-11 23:45:45');

-- ----------------------------
-- Table structure for device
-- ----------------------------
DROP TABLE IF EXISTS `device`;
CREATE TABLE `device`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `status` enum('online','offline') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT 'offline',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKk92m2qj36vn62ctp5pgbt4982`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FKk92m2qj36vn62ctp5pgbt4982` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device
-- ----------------------------
INSERT INTO `device` VALUES (1, 'Tanaman Cabai', 'Kebun Belakang', 'online', '2025-10-10 20:22:02', NULL);
INSERT INTO `device` VALUES (2, 'Anggrek', 'Ruang Tamu', 'offline', '2025-10-10 20:22:02', NULL);

-- ----------------------------
-- Table structure for device_command
-- ----------------------------
DROP TABLE IF EXISTS `device_command`;
CREATE TABLE `device_command`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` bigint NOT NULL,
  `command_type` enum('watering','lighting') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `value` float NULL DEFAULT 0,
  `override` tinyint(1) NULL DEFAULT 0,
  `sent` tinyint(1) NULL DEFAULT 0,
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `override_mode` bit(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_command_type`(`device_id` ASC, `command_type` ASC) USING BTREE,
  CONSTRAINT `device_command_ibfk_1` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of device_command
-- ----------------------------
INSERT INTO `device_command` VALUES (1, 1, 'watering', 75, 1, 0, '2025-10-10 20:22:02', NULL);
INSERT INTO `device_command` VALUES (2, 1, 'lighting', 50, 0, 0, '2025-10-10 20:22:02', NULL);
INSERT INTO `device_command` VALUES (3, 1, 'watering', 100, 0, 0, '2025-10-12 13:10:28', b'0');
INSERT INTO `device_command` VALUES (4, 1, 'lighting', 100, 0, 0, '2025-10-12 13:21:36', b'0');

-- ----------------------------
-- Table structure for sensor_data
-- ----------------------------
DROP TABLE IF EXISTS `sensor_data`;
CREATE TABLE `sensor_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `device_id` bigint NOT NULL,
  `soil_moisture` float NULL DEFAULT 0,
  `air_humidity` float NULL DEFAULT 0,
  `light_intensity` float NULL DEFAULT 0,
  `timestamp` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sensor_device_time`(`device_id` ASC, `timestamp` DESC) USING BTREE,
  CONSTRAINT `sensor_data_ibfk_1` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sensor_data
-- ----------------------------
INSERT INTO `sensor_data` VALUES (1, 1, 52.3, 68.5, 340, '2025-10-10 20:22:02');
INSERT INTO `sensor_data` VALUES (2, 1, 48.1, 70, 360, '2025-10-10 20:22:02');
INSERT INTO `sensor_data` VALUES (3, 2, 55, 65, 120, '2025-10-10 20:22:02');



SET FOREIGN_KEY_CHECKS = 1;
