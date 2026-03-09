# 架构设计文档

## 系统架构概述

```
┌─────────────────────────────────────────────────────────────────┐
│                      用户侧（客户端层）                          │
├─────────────────────────────────────────────────────────────────┤
│                    微信小程序前端                                │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │  页面管理    │  │  组件系统    │  │  工具函数    │          │
│  │  (Pages)     │  │(Components)  │  │  (Utils)     │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                            ↓ HTTPS/WebSocket
┌─────────────────────────────────────────────────────────────────┐
│                      应用层（服务器）                            │
├─────────────────────────────────────────────────────────────────┤
│                   Spring Boot 应用                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  控制器层 (Controller)                                  │  │
│  │  ├─ UserController      │ 用户管理                    │  │
│  │  ├─ ChatController      │ 聊天功能                    │  │
│  │  ├─ DynamicController   │ 动态功能                    │  │
│  │  ├─ GiftController      │ 礼物系统                    │  │
│  │  └─ VIPController       │ VIP 会员                    │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            ↓                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  业务逻辑层 (Service)                                   │  │
│  │  ├─ UserService         │ 用户业务逻辑                │  │
│  │  ├─ ChatService         │ 聊天业务逻辑                │  │
│  │  ├─ DynamicService      │ 动态业务逻辑                │  │
│  │  ├─ GiftService         │ 礼物业务逻辑                │  │
│  │  ├─ FileService         │ 文件上传管理                │  │
│  │  └─ WeChatService       │ 微信接口集成                │  │
│  └──────────────────────────────────────────────────────────┘  │
│                            ↓                                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  数据访问层 (Repository/Mapper)                         │  │
│  │  ├─ JPA Repository      │ 基于 JPA 的数据访问        │  │
│  │  └─ MyBatis Mapper      │ 基于 MyBatis 的数据访问    │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────────┐
│                    数据存储层 & 外部服务                         │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │   MySQL      │  │  Redis Cache │  │ Aliyun OSS   │          │
│  │  (主数据库)  │  │  (可选缓存)   │  │ (文件存储)   │          │
│  └──────────────┘  └──────────────┘  └──────────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

## 核心模块详解

### 1. 用户模块 (User Module)

#### 功能
- 用户注册和登录（微信登录）
- 用户资料管理（头像、昵称、年龄、地区等）
- 用户搜索和筛选

#### 关键类
```
com.lovecube.user
├── entity/User.java              用户实体
├── controller/UserController.java  用户 API
├── service/UserService.java       用户业务逻辑
├── mapper/UserMapper.java         数据访问
└── dto/UserDTO.java               数据传输对象
```

#### 数据库表
```sql
users          -- 用户基本信息
user_profiles  -- 用户详细资料
user_visitors  -- 访客记录
```

### 2. 聊天模块 (Chat Module)

#### 功能
- 单人聊天
- 群组聊天
- 实时消息推送（WebSocket）
- 消息加载历史

#### 关键类
```
com.lovecube.chat
├── entity/Message.java           消息实体
├── controller/ChatController.java 聊天 API
├── service/ChatService.java       聊天业务逻辑
├── websocket/WebSocketHandler.java WebSocket 处理
└── mapper/MessageMapper.java       数据访问
```

#### WebSocket 架构
```
Client 1 ─┐
          ├─→ WebSocket Server ─→ Message Broker ─→ Message Handler
Client 2 ─┘
          │
          └─→ 消息存储到数据库
```

#### 数据库表
```sql
messages      -- 聊天消息
conversations -- 会话
chat_groups   -- 聊天群组
```

### 3. 动态模块 (Dynamic Module)

#### 功能
- 发布动态（文字、图片）
- 点赞和评论
- 动态列表加载

#### 关键类
```
com.lovecube.dynamic
├── entity/Dynamic.java           动态实体
├── entity/Like.java              点赞
├── entity/Comment.java           评论
├── controller/DynamicController.java
├── service/DynamicService.java
└── mapper/DynamicMapper.java
```

#### 数据库表
```sql
dynamics    -- 动态内容
likes       -- 点赞记录
comments    -- 评论记录
```

### 4. 礼物系统 (Gift System)

#### 功能
- 礼物列表管理
- 礼物赠送
- 礼物领取和通知

#### 关键类
```
com.lovecube.gift
├── entity/Gift.java              礼物定义
├── entity/GiftRecord.java        送礼记录
├── controller/GiftController.java
├── service/GiftService.java
└── mapper/GiftMapper.java
```

#### 业务流程
```
用户浏览礼物 → 选择礼物 → 确认赠送 → 生成记录 → 发送通知 → 接收方查看
```

### 5. VIP 会员系统

#### 功能
- VIP 等级划分
- VIP 特权管理
- VIP 续费管理

#### 关键类
```
com.lovecube.vip
├── entity/VIPLevel.java          VIP 等级
├── entity/VIPSubscription.java    VIP 订阅
├── controller/VIPController.java
├── service/VIPService.java
└── mapper/VIPMapper.java
```

## 技术选型理由

### 后端框架选择

| 技术 | 选择理由 | 替代方案 |
|-----|--------|--------|
| Spring Boot 3.x | 成熟、生态完整、性能好 | Micronaut, Quarkus |
| Spring Security | 认证授权标准方案 | Apache Shiro |
| JWT | 无状态认证，适合小程序 | Session, OAuth2 |
| MyBatis + JPA | 灵活性和便利性平衡 | Hibernate, jOOQ |
| WebSocket | 实时通信标准 | Socket.io, MQTT |

### 数据库选择

| 技术 | 用途 | 特点 |
|-----|------|------|
| MySQL 8.0 | 主关系型数据存储 | 成熟、稳定、生态好 |
| Redis (可选) | 缓存和会话管理 | 高性能、支持丰富数据结构 |

### 文件存储

| 技术 | 用途 | 优势 |
|-----|------|------|
| 本地存储 | 小规模部署 | 简单方便 |
| Aliyun OSS | 生产环境 | 可扩展、有 CDN 支持 |

## 关键设计模式

### 1. 分层架构
- **优点**: 职责清晰、易于测试、易于维护
- **实现**: Controller → Service → Repository

### 2. DTO 模式
- **用途**: 在 API 层和业务层之间传输数据
- **优点**: 隐藏内部实现、安全性高

### 3. 观察者模式
- **应用**: 事件驱动（消息推送、通知）
- **实现**: Spring Event 或自定义 EventBus

### 4. 策略模式
- **应用**: 文件上传策略（本地 vs OSS）

### 5. 单例模式
- **应用**: 连接池、缓存管理器

## 数据流示例

### 用户注册流程

```
1. 前端发送微信登录请求
   Login Request: {code: "xxx"}
   
2. 后端处理登录
   ┌─ WeChatService.getWeChatUserInfo(code)
   ├─ 获取微信用户信息 
   ├─ 创建或更新本地用户
   └─ 生成 JWT Token
   
3. 返回 Token 给前端
   Login Response: {
     token: "eyJhbGc...",
     user: {id, nickname, avatar}
   }
   
4. 前端保存 Token，后续请求使用 Authorization Header
```

### 发送消息流程

```
1. 前端发送消息
   WebSocket Message: {
     type: "message",
     conversationId: 123,
     content: "Hello"
   }
   
2. WebSocket Server 接收
   ├─ 验证用户身份
   ├─ 保存到数据库
   └─ 广播给接收方
   
3. 接收方实时接收
   WebSocket Notification: {
     type: "message",
     from: user,
     content: "Hello"
   }
```

## 安全设计

### 认证流程
```
1. 微信登录 (一次性)
2. 后端验证 Code，获取微信信息
3. 生成 JWT Token
4. 前端存储 Token
5. 后续请求在 Header 中使用 Token
```

### 授权机制
```
Controller
  ├─ @PreAuthorize("hasRole('USER')")  -- 角色权限
  ├─ @PreAuthorize("hasAuthority('READ')")  -- 具体权限
  └─ 自定义注解用于业务权限检查
```

### 数据保护
- SQL 注入防护：参数化查询
- XSS 防护：输入验证和输出编码
- CSRF 防护：Token 验证
- 敏感数据加密：密码使用 bcrypt

## 扩展性设计

### 缓存策略
```
热读取数据
  ├─ 用户信息：Redis
  ├─ 礼物列表：Redis
  ├─ VIP 等级：内存
  └─ 缓存失效策略：TTL + 主动更新
```

### 数据库优化
- 索引设计：频繁查询列（user_id, created_at）
- 分页策略：避免大 OFFSET
- 查询优化：N+1 解决方案

### 异步处理
```
耗时操作 (邮件、第三方 API)
  ├─ 生成消息
  ├─ 放入队列 (可选用 RabbitMQ/Kafka)
  └─ 异步处理
```

## 部署架构

### 开发环境
```
Frontend (本地 npm dev server)
        ↓
Backend (localhost:8090)
        ↓
MySQL (localhost:3306)
```

### 生产环境
```
CDN + 静态资源
        ↓
Load Balancer (可选)
        ↓
Application Servers (多实例)
        ↓
Database Server (MySQL + Backup)
     ↓
OSS (文件存储)
```

## 性能考量

### 响应时间目标
- API 响应: < 200ms
- WebSocket 消息: < 100ms
- 页面加载: < 2s

### 并发能力
- 单机 QPS: 1000+
- 支持用户并发: 10000+
- 水平扩展: 添加更多实例和负载均衡

### 监控指标
- 平均响应时间
- 错误率
- QPS
- CPU/内存使用率
- 数据库连接池状态

## 更新和维护

### 版本升级计划
- 主版本：大功能变更
- 次版本：新增功能
- 补丁版本：bug 修复和安全更新

### 向后兼容性
- 尽可能维持 API 兼容性
- 深思熟虑地进行 Breaking Changes
- 提供迁移指南

---

**最后更新**: 2024-03-09  
**架构师**: Love Cube Team
