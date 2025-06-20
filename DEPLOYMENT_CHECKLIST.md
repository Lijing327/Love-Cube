# 🚀 阿里云部署检查清单

## ✅ 前置准备（已完成）
- [x] 购买ECS云服务器
- [x] 购买RDS数据库实例
- [x] 购买OSS对象存储
- [x] 购买域名（如果需要）

## 📋 部署步骤

### 1. 服务器连接和基础配置
```bash
# 连接到ECS服务器
ssh root@你的ECS公网IP

# 创建应用用户（建议不使用root）
adduser lovecube
usermod -aG sudo lovecube
su - lovecube
```

### 2. 上传和运行部署脚本
```bash
# 上传部署脚本到服务器
scp deploy-to-aliyun.sh lovecube@你的ECS公网IP:~/

# 在服务器上运行
chmod +x deploy-to-aliyun.sh
./deploy-to-aliyun.sh
```

### 3. 准备配置信息
在运行脚本时，你需要提供以下信息：

#### 📊 数据库信息
- **RDS地址**: 在阿里云RDS控制台获取
- **数据库名**: lovecube
- **用户名**: 你创建的数据库用户
- **密码**: 数据库密码

#### 💾 OSS信息
- **AccessKey ID**: 在RAM控制台创建
- **AccessKey Secret**: 在RAM控制台创建
- **Endpoint**: 如 oss-cn-beijing.aliyuncs.com
- **Bucket名**: 如 love-cube-files
- **CDN域名**: 可选，如果配置了CDN

#### 🌐 应用信息
- **域名**: 如 https://yourdomain.com

### 4. 验证部署结果
```bash
# 检查应用状态
sudo systemctl status lovecube

# 查看应用日志
tail -f ~/app/logs/app.log

# 测试API访问
curl http://你的域名/admin/api/health
```

### 5. 配置域名解析
在你的域名服务商处，添加A记录：
- **主机记录**: @ 或 www
- **记录值**: 你的ECS公网IP

### 6. 配置SSL证书（推荐）
```bash
# 安装certbot
sudo apt install certbot python3-certbot-nginx

# 申请SSL证书
sudo certbot --nginx -d yourdomain.com

# 验证自动续期
sudo certbot renew --dry-run
```

### 7. 修改小程序配置
更新 `frontend/utils/config.js`:
```javascript
const config = {
  baseUrl: "https://yourdomain.com/admin/api",
  wsBaseUrl: "wss://yourdomain.com/admin",
  
  images: {
    baseUrl: 'https://yourdomain.com/admin',
  }
};
```

### 8. 微信小程序配置
1. 登录微信公众平台
2. 开发 → 开发管理 → 开发设置
3. 服务器域名配置：
   - **request合法域名**: yourdomain.com
   - **socket合法域名**: yourdomain.com
   - **uploadFile合法域名**: yourdomain.com
   - **downloadFile合法域名**: yourdomain.com

## 🔧 常用运维命令

### 应用管理
```bash
# 启动应用
~/app/start-app.sh

# 停止应用
~/app/stop-app.sh

# 重启应用
~/app/restart-app.sh

# 查看应用状态
sudo systemctl status lovecube
```

### 日志查看
```bash
# 查看应用日志
tail -f ~/app/logs/app.log

# 查看Nginx日志
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# 查看系统日志
sudo journalctl -u lovecube -f
```

### 性能监控
```bash
# 查看系统资源
htop

# 查看磁盘使用
df -h

# 查看内存使用
free -h

# 查看端口占用
netstat -tulpn | grep :8090
```

## 🚨 常见问题排查

### 1. 应用无法启动
```bash
# 检查Java版本
java -version

# 检查端口占用
sudo netstat -tulpn | grep :8090

# 查看详细错误日志
tail -f ~/app/logs/app.log
```

### 2. 数据库连接失败
- 检查RDS白名单是否添加ECS内网IP
- 检查数据库用户名密码是否正确
- 检查数据库是否已创建

### 3. OSS图片无法显示
- 检查OSS Bucket权限是否为"公共读"
- 检查CORS配置是否正确
- 检查AccessKey权限

### 4. 域名无法访问
- 检查域名DNS解析是否正确
- 检查ECS安全组是否开放80/443端口
- 检查Nginx配置是否正确

## 💡 优化建议

### 1. 性能优化
```bash
# 调整JVM参数
vim ~/app/start-app.sh
# 修改 -Xmx2g -Xms1g（根据服务器内存调整）
```

### 2. 安全优化
```bash
# 修改SSH端口
sudo vim /etc/ssh/sshd_config
# Port 22 → Port 2222

# 禁用密码登录，使用密钥认证
# PasswordAuthentication no
```

### 3. 备份策略
```bash
# 创建定时备份
crontab -e
# 添加：0 2 * * * ~/app/backup.sh
```

## 📊 监控告警

### 1. 应用监控
```bash
# 创建监控脚本
vim ~/app/monitor.sh

#!/bin/bash
if ! ps -p $(cat ~/app/app.pid) > /dev/null; then
    echo "应用已停止，正在重启..."
    ~/app/restart-app.sh
    # 可以添加邮件或钉钉通知
fi

# 添加到crontab
crontab -e
# */5 * * * * ~/app/monitor.sh
```

### 2. 磁盘监控
```bash
# 磁盘使用率监控
df -h | awk '$5 > 80 {print $0}' | mail -s "磁盘空间告警" your@email.com
```

---

## 🎯 部署成功标志

当你看到以下信息时，说明部署成功：

1. ✅ 应用日志显示 "Started BackendApplication"
2. ✅ 浏览器访问 `http://yourdomain.com/admin/api/health` 返回 "OK"
3. ✅ 小程序可以正常登录和使用
4. ✅ 图片可以正常上传和显示

**恭喜！你的恋爱魔方已成功部署到阿里云！** 🎉 