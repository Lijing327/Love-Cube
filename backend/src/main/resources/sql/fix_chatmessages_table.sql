-- 修复chatmessages表结构
-- 添加缺失的字段

-- 检查并添加created_at字段
SET @dbname = DATABASE();
SET @tablename = "chatmessages";
SET @columnname = "created_at";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 1",
  "ALTER TABLE chatmessages ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 检查并添加message_type字段
SET @columnname = "message_type";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 1",
  "ALTER TABLE chatmessages ADD COLUMN message_type VARCHAR(20) DEFAULT 'chat'"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 确保timestamp字段存在且有默认值
SET @columnname = "timestamp";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      TABLE_SCHEMA = @dbname
      AND TABLE_NAME = @tablename
      AND COLUMN_NAME = @columnname
  ) > 0,
  "SELECT 1",
  "ALTER TABLE chatmessages ADD COLUMN timestamp BIGINT DEFAULT (UNIX_TIMESTAMP() * 1000)"
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- 更新现有记录的timestamp（如果为空）
UPDATE chatmessages SET timestamp = (UNIX_TIMESTAMP() * 1000) WHERE timestamp IS NULL;

-- 更新现有记录的message_type（如果为空）
UPDATE chatmessages SET message_type = 'chat' WHERE message_type IS NULL;

-- 更新现有记录的created_at（如果为空）
UPDATE chatmessages SET created_at = FROM_UNIXTIME(timestamp/1000) WHERE created_at IS NULL;

-- 显示表结构
DESCRIBE chatmessages; 