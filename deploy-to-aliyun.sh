#!/bin/bash

# 恋爱魔方阿里云部署脚本
# 使用方法：chmod +x deploy-to-aliyun.sh && ./deploy-to-aliyun.sh

echo "🚀 开始部署恋爱魔方到阿里云..."

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查是否为root用户
if [ "$EUID" -eq 0 ]; then
    echo -e "${RED}❌ 请不要使用root用户运行此脚本！${NC}"
    echo "建议创建普通用户：sudo adduser lovecube"
    exit 1
fi

# 1. 更新系统和安装基础软件
echo -e "${BLUE}📦 安装基础软件...${NC}"
sudo apt update && sudo apt upgrade -y
sudo apt install -y openjdk-17-jdk git nginx mysql-client htop unzip curl

# 验证Java安装
echo -e "${BLUE}☕ 验证Java安装...${NC}"
java -version

# 2. 创建应用目录
echo -e "${BLUE}📁 创建应用目录...${NC}"
mkdir -p ~/app/logs
mkdir -p ~/app/backups
cd ~/app

# 3. 克隆代码（如果不存在）
if [ ! -d "love-cube-project" ]; then
    echo -e "${BLUE}📥 克隆代码仓库...${NC}"
    echo "请输入你的Git仓库地址："
    read -p "Git仓库地址: " GIT_REPO
    git clone "$GIT_REPO" love-cube-project
fi

cd love-cube-project

# 4. 配置环境变量
echo -e "${YELLOW}⚙️  配置环境变量${NC}"
echo "请填写以下配置信息："

# 数据库配置
echo -e "${BLUE}数据库配置：${NC}"
read -p "RDS数据库地址: " DB_HOST
read -p "数据库名称 [lovecube]: " DB_NAME
DB_NAME=${DB_NAME:-lovecube}
read -p "数据库用户名: " DB_USERNAME
read -s -p "数据库密码: " DB_PASSWORD
echo

# OSS配置
echo -e "${BLUE}OSS配置：${NC}"
read -p "OSS AccessKey ID: " OSS_ACCESS_KEY_ID
read -s -p "OSS AccessKey Secret: " OSS_ACCESS_KEY_SECRET
echo
read -p "OSS Endpoint [oss-cn-beijing.aliyuncs.com]: " OSS_ENDPOINT
OSS_ENDPOINT=${OSS_ENDPOINT:-oss-cn-beijing.aliyuncs.com}
read -p "OSS Bucket名称 [love-cube-files]: " OSS_BUCKET_NAME
OSS_BUCKET_NAME=${OSS_BUCKET_NAME:-love-cube-files}
read -p "CDN域名 (可选): " OSS_CDN_DOMAIN

# 应用配置
echo -e "${BLUE}应用配置：${NC}"
read -p "应用域名 (例如: https://yourdomain.com): " APP_DOMAIN
APP_BASE_URL="${APP_DOMAIN}/admin"

# 写入环境变量文件
echo -e "${BLUE}💾 保存环境变量...${NC}"
cat > ~/.env << EOF
# 数据库配置
export DB_HOST="$DB_HOST"
export DB_NAME="$DB_NAME"
export DB_USERNAME="$DB_USERNAME"
export DB_PASSWORD="$DB_PASSWORD"

# OSS配置
export OSS_ACCESS_KEY_ID="$OSS_ACCESS_KEY_ID"
export OSS_ACCESS_KEY_SECRET="$OSS_ACCESS_KEY_SECRET"
export OSS_ENDPOINT="$OSS_ENDPOINT"
export OSS_BUCKET_NAME="$OSS_BUCKET_NAME"
export OSS_CDN_DOMAIN="$OSS_CDN_DOMAIN"

# 应用配置
export APP_BASE_URL="$APP_BASE_URL"

# JWT密钥（随机生成）
export JWT_SECRET="$(openssl rand -hex 32)"
EOF

# 添加到.bashrc
if ! grep -q "source ~/.env" ~/.bashrc; then
    echo "source ~/.env" >> ~/.bashrc
fi

# 加载环境变量
source ~/.env

echo -e "${GREEN}✅ 环境变量配置完成${NC}"

# 5. 测试数据库连接
echo -e "${BLUE}🔗 测试数据库连接...${NC}"
mysql -h"$DB_HOST" -u"$DB_USERNAME" -p"$DB_PASSWORD" -e "SELECT 1;" 2>/dev/null
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 数据库连接成功${NC}"
else
    echo -e "${RED}❌ 数据库连接失败，请检查配置${NC}"
    exit 1
fi

# 6. 导入数据库
echo -e "${BLUE}📊 导入数据库...${NC}"
if [ -f "backend/src/main/resources/lovecube.sql" ]; then
    mysql -h"$DB_HOST" -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" < backend/src/main/resources/lovecube.sql
    echo -e "${GREEN}✅ 数据库导入完成${NC}"
else
    echo -e "${YELLOW}⚠️  未找到数据库文件，请手动导入${NC}"
fi

# 7. 打包应용
echo -e "${BLUE}📦 打包应用...${NC}"
cd backend
chmod +x mvnw
./mvnw clean package -DskipTests
if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ 应用打包成功${NC}"
else
    echo -e "${RED}❌ 应用打包失败${NC}"
    exit 1
fi

# 8. 创建启动脚本
echo -e "${BLUE}📝 创建启动脚本...${NC}"
cd ~/app

cat > start-app.sh << 'EOF'
#!/bin/bash
source ~/.env
cd ~/app/love-cube-project/backend

# 检查是否已经在运行
if [ -f ~/app/app.pid ]; then
    PID=$(cat ~/app/app.pid)
    if ps -p $PID > /dev/null; then
        echo "应用已在运行 (PID: $PID)"
        exit 1
    fi
    rm -f ~/app/app.pid
fi

# 启动应用
echo "正在启动应用..."
nohup java -jar -Xmx1g -Xms512m -Dspring.profiles.active=prod target/backend-*.jar > ~/app/logs/app.log 2>&1 &
echo $! > ~/app/app.pid

echo "应用已启动，PID: $(cat ~/app/app.pid)"
echo "查看日志: tail -f ~/app/logs/app.log"
EOF

cat > stop-app.sh << 'EOF'
#!/bin/bash
if [ -f ~/app/app.pid ]; then
    PID=$(cat ~/app/app.pid)
    if ps -p $PID > /dev/null; then
        kill $PID
        echo "应用已停止 (PID: $PID)"
        rm -f ~/app/app.pid
    else
        echo "应用未在运行"
        rm -f ~/app/app.pid
    fi
else
    echo "未找到PID文件"
fi
EOF

cat > restart-app.sh << 'EOF'
#!/bin/bash
./stop-app.sh
sleep 3
./start-app.sh
EOF

# 赋予执行权限
chmod +x start-app.sh stop-app.sh restart-app.sh

# 9. 配置Nginx
echo -e "${BLUE}🌐 配置Nginx...${NC}"
sudo tee /etc/nginx/sites-available/lovecube > /dev/null << EOF
server {
    listen 80;
    server_name $(echo $APP_DOMAIN | sed 's|https\?://||');
    
    # 临时使用HTTP（后续配置SSL）
    location /admin/ {
        proxy_pass http://localhost:8090/admin/;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        
        # 设置超时
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # 健康检查
    location /health {
        return 200 'OK';
        add_header Content-Type text/plain;
    }
}
EOF

# 启用站点
sudo ln -sf /etc/nginx/sites-available/lovecube /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx

# 10. 配置systemd服务
echo -e "${BLUE}⚙️  配置系统服务...${NC}"
sudo tee /etc/systemd/system/lovecube.service > /dev/null << EOF
[Unit]
Description=Love Cube Backend Service
After=network.target

[Service]
Type=forking
User=$(whoami)
WorkingDirectory=$(echo ~)/app
ExecStart=$(echo ~)/app/start-app.sh
ExecStop=$(echo ~)/app/stop-app.sh
Restart=always
RestartSec=10
Environment=PATH=/usr/bin:/usr/local/bin

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable lovecube

# 11. 启动应用
echo -e "${BLUE}🚀 启动应用...${NC}"
./start-app.sh

# 等待应用启动
echo "等待应用启动..."
sleep 10

# 检查应用状态
if [ -f app.pid ] && ps -p $(cat app.pid) > /dev/null; then
    echo -e "${GREEN}✅ 应用启动成功！${NC}"
    echo -e "${BLUE}应用信息：${NC}"
    echo "- PID: $(cat app.pid)"
    echo "- 日志: tail -f ~/app/logs/app.log"
    echo "- 访问地址: $APP_BASE_URL"
    echo "- 健康检查: $APP_DOMAIN/health"
else
    echo -e "${RED}❌ 应用启动失败${NC}"
    echo "请检查日志: tail -f ~/app/logs/app.log"
    exit 1
fi

# 12. 配置防火墙
echo -e "${BLUE}🔒 配置防火墙...${NC}"
sudo ufw --force enable
sudo ufw allow ssh
sudo ufw allow 80
sudo ufw allow 443

echo -e "${GREEN}🎉 部署完成！${NC}"
echo
echo -e "${BLUE}=== 部署信息 ===${NC}"
echo "应用地址: $APP_BASE_URL"
echo "数据库: $DB_HOST"
echo "OSS Bucket: $OSS_BUCKET_NAME"
echo
echo -e "${BLUE}=== 常用命令 ===${NC}"
echo "启动应用: ~/app/start-app.sh"
echo "停止应用: ~/app/stop-app.sh"
echo "重启应用: ~/app/restart-app.sh"
echo "查看日志: tail -f ~/app/logs/app.log"
echo "查看状态: sudo systemctl status lovecube"
echo
echo -e "${YELLOW}⚠️  下一步需要：${NC}"
echo "1. 配置域名DNS解析"
echo "2. 申请和配置SSL证书"
echo "3. 修改小程序配置中的服务器地址"
echo "4. 在微信公众平台配置服务器域名" 