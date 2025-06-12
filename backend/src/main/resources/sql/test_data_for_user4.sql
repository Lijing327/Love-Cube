-- 为用户ID=4添加测试数据

-- 1. 添加互动数据（其他用户对用户4的互动）
INSERT INTO `user_interactions` 
(`from_user_id`, `to_user_id`, `interaction_type`, `target_type`, `target_id`, `content`, `is_read`, `created_at`, `updated_at`)
VALUES
-- 用户1点赞了用户4
(1, 4, 'LIKE', 'PROFILE', 4, NULL, FALSE, NOW(), NOW()),
-- 用户2关注了用户4  
(2, 4, 'FOLLOW', 'USER', NULL, NULL, FALSE, NOW(), NOW()),
-- 用户3给用户4送了礼物
(3, 4, 'GIFT', 'USER', NULL, '送你一束玫瑰花💐', FALSE, NOW(), NOW()),
-- 用户1超级点赞了用户4
(1, 4, 'SUPER_LIKE', 'PROFILE', 4, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
-- 用户2又点赞了用户4（不同时间）
(2, 4, 'LIKE', 'PROFILE', 4, NULL, FALSE, DATE_SUB(NOW(), INTERVAL 30 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE));

-- 2. 添加访客数据（其他用户访问用户4的记录）
INSERT INTO `user_visitors` 
(`visitor_user_id`, `visited_user_id`, `visit_type`, `visit_source`, `duration_seconds`, `is_new_visitor`, `created_at`, `updated_at`)
VALUES
-- 用户1访问了用户4的资料
(1, 4, 'PROFILE', 'RECOMMEND', 45, TRUE, NOW(), NOW()),
-- 用户2详细查看了用户4
(2, 4, 'DETAIL', 'SEARCH', 120, TRUE, DATE_SUB(NOW(), INTERVAL 10 MINUTE), DATE_SUB(NOW(), INTERVAL 10 MINUTE)),
-- 用户3查看了用户4的照片
(3, 4, 'PHOTO', 'DISCOVER', 30, TRUE, DATE_SUB(NOW(), INTERVAL 20 MINUTE), DATE_SUB(NOW(), INTERVAL 20 MINUTE)),
-- 用户1再次访问（不是新访客）
(1, 4, 'PROFILE', 'RECOMMEND', 60, FALSE, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_SUB(NOW(), INTERVAL 1 HOUR)),
-- 用户5也访问了用户4
(5, 4, 'QUICK_VIEW', 'MATCH', 15, TRUE, DATE_SUB(NOW(), INTERVAL 2 HOUR), DATE_SUB(NOW(), INTERVAL 2 HOUR)); 