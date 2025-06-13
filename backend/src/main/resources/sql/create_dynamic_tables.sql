-- 创建动态表
CREATE TABLE IF NOT EXISTS `dynamics` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `content` text NOT NULL COMMENT '动态内容',
  `image_urls` text COMMENT '图片URL列表(JSON格式)',
  `like_count` int NOT NULL DEFAULT '0' COMMENT '点赞数',
  `comment_count` int NOT NULL DEFAULT '0' COMMENT '评论数',
  `share_count` int NOT NULL DEFAULT '0' COMMENT '分享数',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态表';

-- 创建动态点赞表
CREATE TABLE IF NOT EXISTS `dynamic_likes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `dynamic_id` bigint NOT NULL COMMENT '动态ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dynamic_user` (`dynamic_id`, `user_id`),
  KEY `idx_dynamic_id` (`dynamic_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态点赞表';

-- 插入测试数据
INSERT INTO `dynamics` (`user_id`, `content`, `image_urls`, `like_count`, `comment_count`, `share_count`) VALUES
(2, '今天天气真好，出来散散步~', '["http://192.168.1.158:8090/admin/images/dynamic1.jpg"]', 5, 2, 1),
(3, '分享一下今天的美食，超级好吃！', '["http://192.168.1.158:8090/admin/images/dynamic2.jpg", "http://192.168.1.158:8090/admin/images/dynamic3.jpg"]', 8, 3, 2),
(4, '工作之余，来一杯咖啡放松一下', NULL, 3, 1, 0),
(2, '周末的阳光真的太舒服了', '["http://192.168.1.158:8090/admin/images/dynamic4.jpg"]', 12, 5, 3);

-- 插入点赞测试数据
INSERT INTO `dynamic_likes` (`dynamic_id`, `user_id`) VALUES
(1, 3), (1, 4),
(2, 2), (2, 4),
(3, 2), (3, 3),
(4, 3), (4, 4); 