# 阿里云部署详细步骤

## 🛒 1. 购买阿里云服务

### 1.1 ECS云服务器
**推荐配置：**
- **CPU**: 2核心
- **内存**: 4GB
- **存储**: 40GB SSD
- **带宽**: 3Mbps
- **操作系统**: Ubuntu 20.04 LTS
- **地域**: 选择离用户最近的区域

**估算费用**: 约200-300元/月

### 1.2 RDS数据库（推荐）
**配置：**
- **数据库类型**: MySQL 8.0
- **规格**: 1核心2GB内存
- **存储**: 20GB
- **地域**: 与ECS同一区域

**估算费用**: 约150-200元/月

### 1.3 OSS对象存储
**配置：**
- **存储类型**: 标准存储
- **读写权限**: 公共读
- **区域**: 与ECS同一区域

**估算费用**: 约50-100元/月（根据使用量）

### 1.4 域名和SSL证书
- **域名**: 如 yourdomain.com
- **SSL证书**: 免费证书或购买

## 📦 2. 服务器环境配置

### 2.1 登录服务器
```bash
ssh root@你的服务器IP
```

### 2.2 安装Java环境
```bash
# 更新系统
apt update && apt upgrade -y

# 安装OpenJDK 17
apt install openjdk-17-jdk -y

# 验证安装
java -version
```

### 2.3 安装Git
```bash
apt install git -y
```

### 2.4 创建应用用户
```bash
# 创建专用用户
useradd -m -s /bin/bash lovecube
usermod -aG sudo lovecube

# 切换到应用用户
su - lovecube
```

## 🗄️ 3. 数据库配置

### 3.1 配置RDS连接
在阿里云RDS控制台：
1. 创建数据库实例
2. 设置白名单（添加ECS内网IP）
3. 创建数据库账号
4. 创建数据库：`lovecube`

### 3.2 导入数据
```bash
# 在服务器上安装MySQL客户端
sudo apt install mysql-client -y

# 导入数据库结构
mysql -h你的RDS地址 -u用户名 -p lovecube < lovecube.sql
```

## 💾 4. OSS配置

### 4.1 创建OSS Bucket
1. 登录阿里云OSS控制台
2. 创建Bucket：`love-cube-files`
3. 设置读写权限：公共读
4. 配置CORS规则（允许跨域访问图片）

### 4.2 创建RAM子账号
1. 进入RAM访问控制台
2. 创建用户：`love-cube-oss`
3. 授权：`AliyunOSSFullAccess`
4. 创建AccessKey

## 🚀 5. 应用部署

### 5.1 克隆代码
```bash
cd /home/lovecube
git clone https://gitee.com/你的仓库/love-cube-project.git
cd love-cube-project
```

### 5.2 配置环境变量
```bash
# 创建环境变量文件
vim ~/.bashrc

# 添加以下内容：
export OSS_ACCESS_KEY_ID="你的AccessKeyID"
export OSS_ACCESS_KEY_SECRET="你的AccessKeySecret"
export OSS_CDN_DOMAIN="你的CDN域名"
export DB_HOST="你的RDS地址"
export DB_USERNAME="数据库用户名"
export DB_PASSWORD="数据库密码"

# 生效环境变量
source ~/.bashrc
```

### 5.3 修改生产配置
```yaml
# backend/src/main/resources/application-prod.yml
server:
  port: 8090
  servlet:
    context-path: /admin

app:
  base-url: https://yourdomain.com/admin
  oss:
    enabled: true
    access-key-id: ${OSS_ACCESS_KEY_ID}
    access-key-secret: ${OSS_ACCESS_KEY_SECRET}
    endpoint: oss-cn-beijing.aliyuncs.com
    bucket-name: love-cube-files
    cdn-domain: ${OSS_CDN_DOMAIN:}

spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://${DB_HOST}:3306/lovecube?useSSL=true&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### 5.4 打包应用
```bash
cd backend
./mvnw clean package -DskipTests
```

### 5.5 创建启动脚本
```bash
# 创建启动脚本
vim /home/lovecube/start-app.sh

#!/bin/bash
cd /home/lovecube/love-cube-project/backend
nohup java -jar -Dspring.profiles.active=prod target/backend-0.0.1-SNAPSHOT.jar > ../app.log 2>&1 &
echo $! > ../app.pid

# 赋予执行权限
chmod +x /home/lovecube/start-app.sh
```

### 5.6 创建停止脚本
```bash
vim /home/lovecube/stop-app.sh

#!/bin/bash
if [ -f /home/lovecube/app.pid ]; then
    kill $(cat /home/lovecube/app.pid)
    rm -f /home/lovecube/app.pid
    echo "应用已停止"
else
    echo "未找到运行的应用"
fi

chmod +x /home/lovecube/stop-app.sh
```

## 🌐 6. 配置反向代理（Nginx）

### 6.1 安装Nginx
```bash
sudo apt install nginx -y
```

### 6.2 配置Nginx
```bash
sudo vim /etc/nginx/sites-available/lovecube

# 配置内容：
server {
    listen 80;
    server_name yourdomain.com;
    
    # 重定向到HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com;
    
    # SSL证书配置
    ssl_certificate /path/to/your/cert.pem;
    ssl_certificate_key /path/to/your/key.pem;
    
    # API代理
    location /admin/ {
        proxy_pass http://localhost:8090/admin/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # 静态文件代理
    location / {
        root /home/lovecube/love-cube-project/frontend;
        try_files $uri $uri/ /index.html;
    }
}

# 创建软链接
sudo ln -s /etc/nginx/sites-available/lovecube /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## 🔧 7. 配置开机自启

### 7.1 创建systemd服务
```bash
sudo vim /etc/systemd/system/lovecube.service

[Unit]
Description=Love Cube Backend Service
After=network.target

[Service]
Type=forking
User=lovecube
WorkingDirectory=/home/lovecube/love-cube-project/backend
ExecStart=/home/lovecube/start-app.sh
ExecStop=/home/lovecube/stop-app.sh
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target

# 启用服务
sudo systemctl enable lovecube
sudo systemctl start lovecube
```

## 📱 8. 小程序配置调整

### 8.1 修改配置文件
```javascript
// frontend/utils/config.js
const config = {
  baseUrl: "https://yourdomain.com/admin/api",
  wsBaseUrl: "wss://yourdomain.com/admin",
  
  images: {
    baseUrl: 'https://yourdomain.com/admin',
    // OSS启用后，这个baseUrl会被OSS域名替代
  }
};
```

### 8.2 重新上传小程序代码
1. 使用微信开发者工具
2. 修改request合法域名（微信公众平台）
3. 添加你的域名到服务器域名列表

## 🔒 9. 安全配置

### 9.1 配置防火墙
```bash
sudo ufw enable
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443
sudo ufw allow 8090  # 应用端口（可选，如果通过Nginx代理可以不开放）
```

### 9.2 定期备份
```bash
# 创建备份脚本
vim /home/lovecube/backup.sh

#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -h你的RDS地址 -u用户名 -p密码 lovecube > /home/lovecube/backups/db_$DATE.sql
# 可以配置crontab定期执行
```

## 📊 10. 监控和日志

### 10.1 查看应用日志
```bash
tail -f /home/lovecube/app.log
```

### 10.2 查看系统资源
```bash
htop  # 需要先安装：sudo apt install htop
df -h  # 磁盘使用情况
free -h  # 内存使用情况
```

## 🎯 部署检查清单

- [ ] ECS服务器购买并配置
- [ ] RDS数据库创建并导入数据
- [ ] OSS存储配置并测试
- [ ] 域名解析配置
- [ ] SSL证书安装
- [ ] 应用打包并部署
- [ ] Nginx反向代理配置
- [ ] 服务自启动配置
- [ ] 小程序域名配置
- [ ] 安全和监控配置

## 💰 总成本预估

| 服务 | 月费用 | 说明 |
|------|-------|------|
| ECS | 200-300元 | 2核4G配置 |
| RDS | 150-200元 | 1核2G MySQL |
| OSS | 50-100元 | 根据存储和流量 |
| 带宽 | 包含在ECS | 3Mbps |
| **总计** | **约500元/月** | 可根据实际调整 |

## 🚨 常见问题

1. **数据库连接失败**：检查RDS白名单设置
2. **图片无法显示**：检查OSS CORS配置
3. **小程序请求失败**：检查域名配置和SSL证书
4. **服务启动失败**：检查日志和端口占用

按照这个指南，你就能成功部署到阿里云了！🎉 