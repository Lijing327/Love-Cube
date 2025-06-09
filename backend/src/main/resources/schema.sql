-- 创建轮播图表
CREATE TABLE IF NOT EXISTS banners (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_url VARCHAR(255) NOT NULL,
    link_url VARCHAR(255),
    title VARCHAR(100),
    sort INT,
    is_active BOOLEAN DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME
);

-- 创建用户资料表
CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    avatar VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    city VARCHAR(50),
    province VARCHAR(50),
    tag VARCHAR(100),
    has_house BOOLEAN,
    has_car BOOLEAN,
    annual_income INT,
    education VARCHAR(50),
    has_overseas_experience BOOLEAN,
    is_single BOOLEAN,
    last_active_time DATETIME,
    is_newcomer BOOLEAN DEFAULT TRUE,
    create_time DATETIME,
    update_time DATETIME
);

-- 创建用户标签表
CREATE TABLE IF NOT EXISTS user_tags (
    user_id BIGINT,
    tag VARCHAR(50),
    PRIMARY KEY (user_id, tag),
    FOREIGN KEY (user_id) REFERENCES user_profiles(id)
); 