-- 为users表添加height字段
ALTER TABLE users ADD COLUMN IF NOT EXISTS height INT NULL COMMENT '用户身高(厘米)';

-- 检查字段是否添加成功
DESCRIBE users; 