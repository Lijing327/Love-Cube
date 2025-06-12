-- 用户访客表
CREATE TABLE `user_visitors` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `visitor_user_id` bigint NOT NULL COMMENT '访客用户ID',
  `visited_user_id` bigint NOT NULL COMMENT '被访问的用户ID',
  `visit_type` varchar(50) NOT NULL COMMENT '访问类型：PROFILE, PHOTO, DETAIL, QUICK_VIEW',
  `visit_source` varchar(50) NULL COMMENT '访问来源：SEARCH, RECOMMEND, MATCH, LINK, DISCOVER',
  `duration_seconds` int NULL COMMENT '访问时长（秒）',
  `is_new_visitor` boolean NOT NULL DEFAULT TRUE COMMENT '是否是新访客（首次访问）',
  `ip_address` varchar(45) NULL COMMENT '访问IP地址',
  `device_info` varchar(500) NULL COMMENT '设备信息',
  `created_at` datetime(6) NOT NULL COMMENT '创建时间',
  `updated_at` datetime(6) NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_visited_user_id` (`visited_user_id`) USING BTREE,
  KEY `idx_visitor_user_id` (`visitor_user_id`) USING BTREE,
  KEY `idx_created_at` (`created_at`) USING BTREE,
  KEY `idx_visit_type` (`visit_type`) USING BTREE,
  KEY `idx_is_new_visitor` (`is_new_visitor`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户访客表';

-- 初始化一些测试数据
INSERT INTO `user_visitors` 
(`visitor_user_id`, `visited_user_id`, `visit_type`, `visit_source`, `duration_seconds`, `is_new_visitor`, `created_at`, `updated_at`)
VALUES
(1, 2, 'PROFILE', 'RECOMMEND', 45, TRUE, NOW(), NOW()),
(3, 2, 'DETAIL', 'SEARCH', 120, TRUE, NOW(), NOW()),
(4, 2, 'PHOTO', 'DISCOVER', 30, TRUE, NOW(), NOW()),
(1, 2, 'PROFILE', 'RECOMMEND', 60, FALSE, DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY));
