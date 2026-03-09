# 变更日志

所有值得注意的项目变化都将被记录在此文件中。

格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/) 和此项目遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### 计划中
- 视频聊天功能
- AI 推荐引擎优化
- 支付系统集成（微信支付）
- 国际化语言支持
- 数据统计和分析后台

## [1.0.0] - 2024-03-09

### 新增
- ✨ 核心的用户配对游戏功能
- ✨ 实时聊天系统（单人聊天和群组聊天）
- ✨ 动态发布和分享功能（支持文本、图片）
- ✨ 用户搜索功能（按地区、年龄、兴趣筛选）
- ✨ 礼物赠送系统
- ✨ VIP 会员功能
- ✨ 用户访客记录
- ✨ 用户资料页面（基础信息、生活照片）
- ✨ 点赞和评论功能
- ✨ 微信登录验证
- ✨ 文件上传到阿里云 OSS
- ✨ 图片处理和压缩功能

### 改进
- 🔧 优化数据库查询性能
- 🔧 改进 WebSocket 连接稳定性
- 🔧 增强图片加载速度
- 🔧 优化前端页面加载时间

### 修复
- 🐛 修复某些浏览器的兼容性问题
- 🐛 修复聊天消息偶尔丢失的问题
- 🐛 修复图片上传大小限制
- 🐛 修复登录后跳转页面问题

### 文档
- 📚 添加 API 文档
- 📚 添加部署指南
- 📚 添加配置说明
- 📚 添加快速开始指南
- 📚 添加开源许可证和贡献指南

### 安全
- 🔒 实现 JWT 令牌认证
- 🔒 添加 CORS 跨域配置
- 🔒 实现数据验证和清理
- 🔒 升级依赖库以修复安全漏洞

---

## 版本说明

### 语义化版本说明
- **主版本号** - 不兼容的 API 修改
- **次版本号** - 向下兼容的功能性新增
- **修订号** - 向下兼容的问题修正

### 标签说明
- ✨ `新增` - 新功能
- 🔧 `改进` - 功能改进
- 🐛 `修复` - Bug 修复
- ⚠️ `弃用` - 即将移除的功能
- 🗑️ `移除` - 已移除的功能
- 📚 `文档` - 文档相关
- 🔒 `安全` - 安全相关

---

## 升级指南

### 从 0.x 升级到 1.0.0

1. **数据库迁移**
   ```bash
   mysql -u root -p lovecube < backend/src/main/resources/lovecube.sql
   mysql -u root -p lovecube < backend/src/main/resources/data.sql
   ```

2. **环境变量更新**
   - 更新 `application-dev.yml` 中的数据库连接
   - 配置阿里云 OSS 密钥

3. **前端配置更新**
   - 更新 `global-config.js` 中的服务器地址
   - 更新 `project.config.json` 中的域名配置

4. **启动应用**
   ```bash
   # 后端
   cd backend && ./mvnw spring-boot:run

   # 前端
   # 在微信开发者工具中打开 frontend 文件夹
   ```

---

## 贡献者

感谢以下贡献者的支持：

- [@username1](https://github.com/username1) - 功能开发
- [@username2](https://github.com/username2) - Bug 修复
- [@username3](https://github.com/username3) - 文档编写

---

更多信息和链接：
- [项目主页](https://github.com/yourusername/Love-Cube)
- [问题追踪](https://github.com/yourusername/Love-Cube/issues)
- [讨论区](https://github.com/yourusername/Love-Cube/discussions)
