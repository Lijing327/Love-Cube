# 🚀 快速开始指南

欢迎使用 Love Cube！本指南将帮助你快速上手。

## 📋 前置要求

确保你的系统已安装：
- **Java 17+** - [下载](https://www.oracle.com/java/technologies/downloads/)
- **MySQL 8.0+** - [下载](https://dev.mysql.com/downloads/mysql/)
- **Maven 3.6+** - [下载](https://maven.apache.org/download.cgi)
- **微信开发者工具** - [下载](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)（前端开发可选）
- **Git** - [下载](https://git-scm.com/)

### 验证安装

```bash
java -version
mvn -version
mysql --version
git --version
```

## 🎯 第一步：克隆项目

```bash
git clone https://github.com/yourusername/Love-Cube.git
cd Love-Cube
```

## 🔧 第二步：配置数据库

### 1. 创建数据库

```bash
# 登录 MySQL
mysql -u root -p

# 在 MySQL 命令行中执行
CREATE DATABASE lovecube CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 初始化表结构和数据

```bash
# 从项目根目录执行
cd Love-Cube/backend

# 导入表结构
mysql -u root -p lovecube < src/main/resources/lovecube.sql

# 导入初始数据
mysql -u root -p lovecube < src/main/resources/data.sql
```

**注**：如果 SQL 文件在迁移目录下，执行：
```bash
mysql -u root -p lovecube < src/main/resources/db/migration/V3__Add_Photos_Field.sql
```

## 🔐 第三步：配置后端

### 编辑数据库连接配置

编辑 `backend/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lovecube?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
    username: root              # 换成你的 MySQL 用户名
    password: your_password     # 换成你的 MySQL 密码
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    database-platform: org.hibernate.dialect.MySQL8Dialect

  # WebSocket 配置
  websocket:
    endpoint: /ws

# 应用配置
app:
  base-url: http://localhost:8090/admin
  upload-path: ./uploads/
```

### 配置阿里云 OSS（可选）

如果需要使用文件上传功能，编辑 `application-dev.yml`：

```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com    # 你的 OSS endpoint
    access-key-id: your_access_key            # 替换成你的访问密钥 ID
    access-key-secret: your_access_key_secret # 替换成你的访问密钥密钥
    bucket-name: your-bucket-name             # 替换成你的 bucket 名称
```

## ▶️ 第四步：启动后端应用

```bash
cd backend

# 会自动下载所需的依赖（首次较慢）
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

或者使用 Maven 命令：
```bash
./mvnw clean install
./mvnw spring-boot:run
```

**成功输出**：应该看到类似的日志：
```
2024-03-09 10:30:45.123  INFO 12345 --- [           main] c.lovecube.LoveCubeApplication         : Started LoveCubeApplication in 5.234 seconds (JVM running for 5.632)
```

服务将在 `http://localhost:8090/admin` 启动

### 验证后端

```bash
# 在新的终端中测试 API
curl http://localhost:8090/admin/api/health
```

## 📱 第五步：启动前端（微信小程序）

### 1. 配置服务器地址

编辑 `frontend/global-config.js`：

```javascript
export const SERVER_CONFIG = {
  // 本地开发环境
  development: {
    ip: "127.0.0.1",      // 或你的电脑 IP 地址
    port: "8090",         // 后端端口
    basePath: "/admin"    // API 基础路径
  },
  
  // 生产环境
  production: {
    ip: "your-server-ip",     // 替换为生产服务器 IP
    port: "8090",
    basePath: "/admin"
  }
};
```

### 2. 可选：更新微信小程序配置

如果要在微信开发者工具中测试，编辑 `frontend/project.config.json`：

```json
{
  "appid": "YOUR_WECHAT_APP_ID",
  "projectname": "love-cube",
  "setting": {
    "domain": {
      "request": ["http://127.0.0.1:8090"],
      "download": ["http://127.0.0.1:8090"],
      "upload": ["http://127.0.0.1:8090"]
    }
  }
}
```

### 3. 在微信开发者工具中打开

1. 打开微信开发者工具
2. 点击"导入项目"
3. 选择 `Love-Cube/frontend` 文件夹
4. 输入你的微信 AppID（或使用测试 AppID）
5. 点击"确定"导入
6. 等待项目加载完成

### 4. 预览小程序

- 点击顶部菜单栏的"预览"按钮
- 用微信扫码查看实时预览
- 或点击"真机调试"在手机上测试

## ✅ 成功标志

当你看到以下标志时，说明环境配置成功：

### 后端
- ✓ 控制台显示 "Started LoveCubeApplication"
- ✓ 访问 `http://localhost:8090/admin/api/health` 返回 200
- ✓ 数据库连接成功，无连接错误

### 前端
- ✓ 微信开发者工具编译成功（顶部显示"预览"按钮）
- ✓ 页面可以加载（不报 404）
- ✓ 网络请求成功（无红色错误）

## 📖 下一步

现在你可以：

1. **学习项目结构** - 查看 [README.md](./README.md) 中的项目结构部分
2. **阅读 API 文档** - 查看 [docs/api-spec.md](./docs/api-spec.md)
3. **开始开发** - 查看 [CONTRIBUTING.md](./CONTRIBUTING.md) 的开发流程
4. **查看示例代码** - 在 `frontend/pages` 和 `backend/src/main/java` 中探索

## 🐛 问题排查

### 数据库连接失败

```
com.mysql.cj.jdbc.exceptions.CommunicationsException
```

**解决方案**：
- 检查 MySQL 是否运行：`sudo systemctl status mysql` (Linux) 或在任务管理器中查看 (Windows)
- 检查用户名和密码是否正确
- 检查数据库是否存在：`show databases;`

### 端口被占用

```
Address already in use
```

**解决方案**：
```bash
# 查看占用 8090 端口的进程
lsof -i :8090  # Linux/Mac
netstat -ano | findstr :8090  # Windows

# 杀死进程
kill -9 <PID>  # Linux/Mac
taskkill /PID <PID> /F  # Windows
```

### 微信开发者工具无法连接到服务器

**解决方案**：
1. 确保后端已启动：`curl http://localhost:8090/admin/api/health`
2. 检查防火墙设置
3. 如果使用公网 IP，确保配置了正确的 IP 地址
4. 在微信开发者工具中勾选"不验证合法域名"（仅开发环境）

### 导入项目后看不到文件

**解决方案**：
1. 确保选择了正确的 `frontend` 文件夹
2. 删除 `node_modules` 和 `.miniprogram_npm` 目录再试
3. 在菜单中选择"工具" -> "清缓存" -> "清所有缓存"

## 💬 获取帮助

如果遇到问题：

1. **查看日志** - 检查后端和前端的完整错误信息
2. **搜索 Issue** - [GitHub Issues](https://github.com/yourusername/Love-Cube/issues)
3. **提交 Issue** - 包括错误日志和环境信息
4. **讨论** - [GitHub Discussions](https://github.com/yourusername/Love-Cube/discussions)

## ⚡ 快速命令参考

```bash
# 启动后端
cd Love-Cube/backend
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 构建项目
./mvnw clean package

# 运行测试
./mvnw test

# 进入 MySQL
mysql -u root -p

# 重新创建数据库
mysql -u root -p
DROP DATABASE lovecube;
CREATE DATABASE lovecube CHARACTER SET utf8mb4;
mysql -u root -p lovecube < backend/src/main/resources/lovecube.sql;
mysql -u root -p lovecube < backend/src/main/resources/data.sql;
```

---

祝你开发愉快！🎉 如有任何问题，欢迎提交 Issue。
