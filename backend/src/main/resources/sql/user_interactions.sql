-- 用户互动表
CREATE TABLE `user_interactions` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL COMMENT '发起互动的用户ID',
  `to_user_id` bigint NOT NULL COMMENT '接收互动的用户ID',
  `interaction_type` varchar(50) NOT NULL COMMENT '互动类型：LIKE, COMMENT, FOLLOW, GIFT, SUPER_LIKE',
  `target_id` bigint NULL COMMENT '目标对象ID（如动态ID、照片ID等）',
  `target_type` varchar(50) NULL COMMENT '目标对象类型：PROFILE, PHOTO, DYNAMIC, USER',
  `content` text NULL COMMENT '互动内容（评论内容、礼物信息等）',
  `gift_id` bigint NULL COMMENT '礼物ID（如果是礼物类型）',
  `gift_count` int NULL COMMENT '礼物数量',
  `is_read` boolean NOT NULL DEFAULT FALSE COMMENT '是否已读',
  `created_at` datetime(6) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(6) NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_to_user_id` (`to_user_id`) USING BTREE,
  KEY `idx_from_user_id` (`from_user_id`) USING BTREE,
  KEY `idx_interaction_type` (`interaction_type`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE,
  KEY `idx_is_read` (`is_read`) USING BTREE,
  UNIQUE KEY `uk_interaction` (`from_user_id`, `to_user_id`, `interaction_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户互动表';

-- 注意：这是表结构创建文件，不包含测试数据
-- 实际数据将通过应用程序正常使用产生 