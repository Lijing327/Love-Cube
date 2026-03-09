# 🎁 恋爱魔方 Love Cube

一个现代化的全栈社交应用，帮助单身人士通过趣味配对游戏找到心仪的伙伴。包含微信小程序前端和 Spring Boot 后端。

[English](./docs/README.EN.md) | [中文](./README.md)

## ✨ 主要特性

- 🎮 **趣味配对游戏** - 通过图片选择和互动游戏进行配对
- 💬 **实时聊天系统** - 支持单人和群组消息，基于 WebSocket 的实时通信
- 🎁 **礼物赠送系统** - 用户间的虚拟礼物互动，支持VIP功能
- 📸 **动态发布** - 分享生活照片和动态，支持点赞和评论
- 👥 **用户搜索** - 按地区、年龄、兴趣等条件快速找到志同道合的人
- 🔐 **安全认证** - 基于微信登录的安全身份验证系统
- 📱 **响应式设计** - 完美适配各类设备

## 🏗️ 技术栈

### 前端
- **框架**: 微信小程序框架
- **语言**: JavaScript
- **特性**: 原生 WXML/WXSS 高效渲染

### 后端
- **框架**: Spring Boot 3.2.3
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **ORM**: MyBatis, JPA
- **认证**: Spring Security + JWT
- **实时通信**: WebSocket
- **对象存储**: Aliyun OSS

### 开发工具
- **构建工具**: Maven 3.6+
- **版本控制**: Git
- **API 文档**: 见 `docs/api-spec.md`

## 📋 项目结构

```
Love-Cube/
├── frontend/                    # 微信小程序前端
│   ├── pages/                   # 页面目录
│   │   ├── index/              # 首页 - 配对游戏
│   │   ├── chat/               # 聊天页面
│   │   ├── dynamic/            # 动态页面
│   │   ├── profile/            # 用户资料
│   │   ├── match/              # 配对页面
│   │   ├── gift/               # 礼物系统
│   │   ├── vip/                # VIP 功能
│   │   └── ...                 # 其他页面
│   ├── components/              # 可复用组件
│   ├── utils/                  # 工具函数
│   ├── images/                 # 静态资源
│   ├── app.js                  # 应用主文件
│   └── package.json            # 依赖配置
│
├── backend/                     # Spring Boot 后端
│   ├── src/
│   │   ├── main/java/com/lovecube/  # Java 源代码
│   │   │   ├── controller/      # 控制器层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   ├── mapper/          # 数据访问层
│   │   │   ├── entity/          # 数据实体
│   │   │   ├── config/          # 配置类
│   │   │   └── util/            # 工具类
│   │   ├── main/resources/      # 配置和SQL脚本
│   │   │   ├── application.yml  # 通用配置
│   │   │   ├── application-dev.yml   # 开发环境配置
│   │   │   ├── application-prod.yml  # 生产环境配置
│   │   │   ├── lovecube.sql     # 表结构脚本
│   │   │   └── data.sql         # 初始化数据
│   │   └── test/                # 测试代码
│   ├── uploads/                 # 文件上传目录
│   ├── pom.xml                  # Maven 配置
│   └── mvnw                     # Maven 包装脚本
│
├── docs/                        # 文档目录
│   ├── api-spec.md             # API 文档
│   ├── deployment-guide.md      # 部署指南
│   ├── aliyun-deployment-steps.md  # 阿里云部署步骤
│   └── architecture.md          # 架构设计文档
│
├── QUICK_START.md              # 快速开始指南
├── CONFIG_GUIDE.md             # 配置指南
├── README.md                   # 本文件
├── LICENSE                     # MIT 许可证
├── CONTRIBUTING.md             # 贡献指南
├── CODE_OF_CONDUCT.md          # 行为准则
└── .gitignore                  # Git 忽略文件
```

## 🚀 快速开始

### 前置要求
- Java 17+
- MySQL 8.0+
- Node.js 16+（仅用于前端开发，非必需）
- Maven 3.6+
- 微信开发者工具（用于前端开发）

### 后端启动

1. **克隆仓库**
```bash
git clone https://github.com/yourusername/Love-Cube.git
cd Love-Cube/backend
```

2. **配置数据库**
```bash
# 创建数据库
mysql -u root -p < src/main/resources/lovecube.sql
mysql -u root -p < src/main/resources/data.sql
```

3. **配置环境变量**
编辑 `src/main/resources/application-dev.yml`：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lovecube?useSSL=false&serverTimezone=UTC
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

4. **启动应用**
```bash
./mvnw spring-boot:run
```

应用将在 `http://localhost:8090/admin` 启动

### 前端启动

1. **进入前端目录**
```bash
cd Love-Cube/frontend
```

2. **配置服务器地址**
编辑 `global-config.js`：
```javascript
server: {
  ip: "127.0.0.1",
  port: "8090",
  basePath: "/admin"
}
```

3. **在微信开发者工具中打开**
- 打开微信开发者工具
- 选择"导入项目"
- 选择 `frontend` 文件夹
- 扫码登录微信账号
- 点击"预览"或"真机调试"

更多详细信息请查看 [QUICK_START.md](./QUICK_START.md)

## 📖 文档

- **[API 文档](./docs/api-spec.md)** - RESTful API 规范
- **[部署指南](./docs/deployment-guide.md)** - 生产环境部署步骤
- **[阿里云部署](./docs/aliyun-deployment-steps.md)** - 在阿里云上部署
- **[配置指南](./CONFIG_GUIDE.md)** - IP地址等配置说明
- **[快速开始](./QUICK_START.md)** - 新手入门指南

## 🔧 开发

### 代码规范
请遵循以下规范进行开发：
- Java: Google Java Style Guide
- JavaScript: Common.js

### 本地开发流程

1. **创建功能分支**
```bash
git checkout -b feature/your-feature-name
```

2. **提交代码**
```bash
git commit -m "feat: add amazing feature"
```

3. **推送分支**
```bash
git push origin feature/your-feature-name
```

4. **提交 Pull Request**
在 GitHub 上创建 PR，描述你的功能和改动

## 🐛 已知问题和改进

- 微信小程序在某些低版本系统上的兼容性需要测试
- 大数据量的动态加载需要优化
- 可以考虑升级到前端框架（如 uni-app 或 taro）以支持多端

## 📦 部署

### 开发环境
```bash
cd backend
./mvnw clean package -Dspring.profiles.active=dev
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

### 生产环境
```bash
cd backend
./mvnw clean package -Dspring.profiles.active=prod
java -jar target/backend-0.0.1-SNAPSHOT.jar
```

查看 [部署指南](./docs/deployment-guide.md) 了解更多信息。

## 🤝 贡献指南

欢迎各种形式的贡献！请阅读 [CONTRIBUTING.md](./CONTRIBUTING.md) 了解:
- 如何提交 Issue
- 如何提交 PR
- 代码风格要求
- 提交信息格式

## 📝 许可证

本项目采用 [MIT License](./LICENSE) 开源许可证。

## 💬 联系方式

- **Issue**: 使用 [GitHub Issues](https://github.com/yourusername/Love-Cube/issues) 报告问题
- **讨论**: 使用 [GitHub Discussions](https://github.com/yourusername/Love-Cube/discussions) 进行讨论
- **邮件**: contact@example.com

## 🙏 致谢

感谢所有贡献者的支持和帮助！

## 🎯 路线图

- [ ] 视频聊天功能
- [ ] AI 推荐引擎优化
- [ ] 支付系统集成（微信支付）
- [ ] 数据统计和分析后台
- [ ] 移动应用适配（Flutter/React Native）
- [ ] 国际化语言支持

---

**⭐ 如果这个项目对你有帮助，请给个 Star 吧！**