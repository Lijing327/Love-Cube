-- 插入轮播图数据
INSERT INTO banners (image_url, link_url, title, sort, is_active, create_time, update_time) VALUES
('https://example.com/banner1.jpg', '/pages/activity/valentine', '情人节活动', 1, true, NOW(), NOW()),
('https://example.com/banner2.jpg', '/pages/activity/spring', '春季相亲会', 2, true, NOW(), NOW()),
('https://example.com/banner3.jpg', '/pages/vip/discount', 'VIP特惠', 3, true, NOW(), NOW());

-- 插入示例用户数据
INSERT INTO user_profiles (nickname, avatar, age, gender, city, province, tag, has_house, has_car, annual_income, education, has_overseas_experience, is_single, last_active_time, is_newcomer, create_time, update_time) VALUES
('小红', 'https://example.com/avatar1.jpg', 25, 'female', '深圳', '广东', '温柔可人', true, true, 300000, '本科', false, true, NOW(), true, NOW(), NOW()),
('阳光', 'https://example.com/avatar2.jpg', 28, 'male', '广州', '广东', '事业有成', true, true, 500000, '硕士', true, true, NOW(), true, NOW(), NOW()),
('美美', 'https://example.com/avatar3.jpg', 24, 'female', '北京', '北京', '活泼开朗', false, true, 200000, '本科', false, true, NOW(), true, NOW(), NOW()),
('大卫', 'https://example.com/avatar4.jpg', 30, 'male', '上海', '上海', '成熟稳重', true, true, 800000, '博士', true, true, NOW(), false, NOW(), NOW()),
('小雨', 'https://example.com/avatar5.jpg', 26, 'female', '成都', '四川', '独立自主', true, false, 250000, '本科', false, true, NOW(), true, NOW(), NOW());

-- 插入用户标签数据
INSERT INTO user_tags (user_id, tag) VALUES
(1, '有房'),
(1, '有车'),
(1, '年收入20w+'),
(2, '有房'),
(2, '有车'),
(2, '年收入20w+'),
(2, '海外经历'),
(3, '有车'),
(3, '年收入20w+'),
(4, '有房'),
(4, '有车'),
(4, '年收入20w+'),
(4, '海外经历'),
(5, '有房'),
(5, '年收入20w+'); 