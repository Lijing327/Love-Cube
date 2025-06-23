-- 添加缺失的字段到users表
-- 为用户表添加身高字段

-- 添加height字段（如果不存在）
ALTER TABLE users ADD COLUMN IF NOT EXISTS height INT NULL COMMENT '用户身高(厘米)';

-- 添加photos字段（如果不存在）  
ALTER TABLE users ADD COLUMN IF NOT EXISTS photos TEXT NULL COMMENT '生活照片JSON数组';

-- 为现有用户添加默认值
UPDATE users SET height = NULL WHERE height IS NULL;
UPDATE users SET photos = NULL WHERE photos IS NULL; 