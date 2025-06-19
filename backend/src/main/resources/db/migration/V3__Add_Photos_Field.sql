-- 添加photos字段到users表
ALTER TABLE users ADD COLUMN photos TEXT COMMENT '生活照片JSON数组';

-- 更新现有用户的测试数据
UPDATE users SET photos = '["http://192.168.1.158:8090/admin/uploads/photos/life1.jpg", "http://192.168.1.158:8090/admin/uploads/photos/life2.jpg"]' WHERE userid = 2;
UPDATE users SET photos = '["http://192.168.1.158:8090/admin/uploads/photos/life3.jpg"]' WHERE userid = 3;
UPDATE users SET photos = '["http://192.168.1.158:8090/admin/uploads/photos/life4.jpg", "http://192.168.1.158:8090/admin/uploads/photos/life5.jpg", "http://192.168.1.158:8090/admin/uploads/photos/life6.jpg"]' WHERE userid = 4; 