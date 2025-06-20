# 恋爱魔方项目部署指南

## 📁 文件存储方案

### 开发环境 vs 生产环境

| 环境 | 存储方案 | 优点 | 缺点 |
|------|----------|------|------|
| **开发环境** | 本地文件系统 | 简单快速，无需额外配置 | 不适合生产，文件易丢失 |
| **生产环境** | 阿里云OSS + CDN | 高可用、快速、成本低 | 需要配置OSS服务 |

## 🚀 阿里云OSS部署步骤

### 1. 创建OSS Bucket

1. 登录阿里云控制台
2. 进入对象存储OSS服务
3. 创建新的Bucket：
   - 名称：`love-cube-files`（或你自定义的名称）
   - 区域：选择离你服务器最近的区域
   - 存储类型：标准存储
   - 读写权限：公共读（允许直接访问图片）
   - 版本控制：关闭（可选）

### 2. 配置跨域访问（CORS）

在OSS控制台设置CORS规则：
```xml
<CORSRule>
    <AllowedOrigin>*</AllowedOrigin>
    <AllowedMethod>GET</AllowedMethod>
    <AllowedMethod>POST</AllowedMethod>
    <AllowedMethod>PUT</AllowedMethod>
    <AllowedMethod>DELETE</AllowedMethod>
    <AllowedHeader>*</AllowedHeader>
    <ExposeHeader>ETag</ExposeHeader>
    <MaxAgeSeconds>3600</MaxAgeSeconds>
</CORSRule>
```

### 3. 创建RAM子账号（推荐）

为了安全起见，不要直接使用主账号的AccessKey：

1. 进入RAM访问控制
2. 创建新用户
3. 授予以下权限：
   - `AliyunOSSFullAccess`（完整OSS权限）
   - 或自定义策略（仅限指定Bucket）

### 4. 配置CDN加速（可选但推荐）

1. 开通阿里云CDN服务
2. 添加域名，源站选择OSS域名
3. 获取CDN域名，配置到应用中

### 5. 环境变量配置

在生产服务器设置以下环境变量：

```bash
# OSS配置
export OSS_ACCESS_KEY_ID="你的AccessKeyID"
export OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"
export OSS_CDN_DOMAIN="你的CDN域名"  # 可选
```

或者在应用配置文件中直接配置（不推荐，安全性低）

## 📋 配置示例

### application-prod.yml
```yaml
app:
  base-url: https://yourdomain.com/admin
  oss:
    enabled: true
    access-key-id: ${OSS_ACCESS_KEY_ID}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}
    endpoint: oss-cn-beijing.aliyuncs.com
    bucket-name: love-cube-files
    cdn-domain: ${OSS_CDN_DOMAIN:}
    avatar-folder: avatars/
    photos-folder: photos/
```

## 💰 成本预估

### OSS存储成本（华北2-北京）
- 标准存储：0.12元/GB/月
- 外网流出流量：0.5元/GB
- PUT请求：0.01元/万次

### 示例计算
假设：
- 1万用户，每人5张照片，每张1MB
- 总存储：50GB
- 月流量：100GB

**月成本约：50GB × 0.12 + 100GB × 0.5 = 56元**

加上CDN成本约70-80元/月，相比自建文件服务器极具优势。

## 🔧 切换到OSS的步骤

### 1. 开发阶段
```yaml
# application-dev.yml
app:
  oss:
    enabled: false  # 继续使用本地存储
```

### 2. 测试阶段
```yaml
# application-test.yml
app:
  oss:
    enabled: true   # 启用OSS进行测试
```

### 3. 生产环境
```yaml
# application-prod.yml
app:
  oss:
    enabled: true   # 正式启用OSS
```

## 📝 数据迁移

如果已有本地文件需要迁移到OSS：

1. 使用阿里云OSS命令行工具（ossutil）
2. 批量上传现有文件
3. 更新数据库中的文件URL

```bash
# 安装ossutil
wget https://gosspublic.alicdn.com/ossutil/1.7.14/ossutil64
chmod 755 ossutil64

# 配置
./ossutil64 config

# 批量上传
./ossutil64 cp -r uploads/ oss://love-cube-files/ --update
```

## ⚠️ 注意事项

1. **安全性**：
   - 不要在代码中硬编码AccessKey
   - 使用环境变量或密钥管理服务
   - 定期轮换AccessKey

2. **备份**：
   - 启用OSS版本控制
   - 配置跨区域备份
   - 定期备份重要数据

3. **监控**：
   - 配置OSS访问日志
   - 监控存储和流量使用
   - 设置费用预警

4. **性能优化**：
   - 启用CDN加速
   - 合理设置缓存策略
   - 图片压缩和格式优化

## 🔄 回滚方案

如果OSS出现问题，可以快速切换回本地存储：

1. 修改配置：`app.oss.enabled: false`
2. 重启应用
3. 临时使用本地存储，不影响业务

这种架构设计保证了灵活性和可靠性！ 