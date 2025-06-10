SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 插入用户数据
INSERT INTO users (userid, age, bio, birth_date, created_at, email, gender, location, occupation, openid, password_hash, phone_number, profile_photo, updated_at, username) 
VALUES 
(2, 21, '热爱旅行，喜欢摄影', '2003-01-15 00:00:00.000000', '2025-03-12 11:36:02.000000', 'zhangwei@example.com', 1, '北京', '程序员', 'wx_001', 'hashed_password', '18812345678', 'http://192.168.1.158:8090/admin/images/avatar1.jpg', '2025-03-12 11:36:02.000000', '张伟'),
(3, 25, '健身达人，喜欢尝试新事物', '1999-05-20 00:00:00.000000', '2025-03-12 11:36:02.000000', 'lina@example.com', 2, '上海', '医生', 'wx_002', 'hashed_password', '18887654321', 'http://192.168.1.158:8090/admin/images/avatar2.jpg', '2025-03-12 11:36:02.000000', '李娜'),
(4, 28, NULL, NULL, NULL, NULL, 1, '上海', NULL, 'oD_yL7Wkbg4v_Cmp5aaEMXEqPNzc', NULL, '5555555555', 'http://192.168.1.158:8090/admin/images/avatar3.jpg', NULL, '用户3636');

-- 插入聊天消息数据
INSERT INTO chatmessages (id, content, is_read, receiver_id, sender_id, timestamp) 
VALUES 
(1, '123', b'0', 2, 4, 1747790059424),
(2, '3456', b'0', 2, 4, 1747790064069),
(3, '12421', b'0', 2, 4, 1747793533369),
(4, '3594', b'0', 2, 4, 1747794406474),
(5, '333', b'0', 2, 4, 1747994449866);

-- 插入匹配记录数据
INSERT INTO matchrecords (id, created_at, match_score, matched_user_id, user_id) 
VALUES 
(1, '2025-05-20 18:05:05.105000', 0.6849219440694392, 2, 4),
(2, '2025-05-21 08:43:50.555000', 0.4201941178352545, 2, 4),
(3, '2025-05-21 09:05:13.569000', 0.5736446332422682, 2, 4);

-- 插入轮播图数据
INSERT INTO banners (image_url, link_url, title, sort, is_active, create_time, update_time)
VALUES 
('http://192.168.1.158:8090/admin/images/banner1.jpg', 'http://example.com/1', '恋爱交友', 1, true, NOW(), NOW()),
('http://192.168.1.158:8090/admin/images/banner2.jpg', 'http://example.com/2', '缘分匹配', 2, true, NOW(), NOW()),
('http://192.168.1.158:8090/admin/images/banner3.jpg', 'http://example.com/3', '甜蜜约会', 3, true, NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1; 