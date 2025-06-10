import config from "../../utils/config";

// 格式化时间的工具函数
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  
  const date = new Date(timestamp);
  const now = new Date();
  const diff = now - date;
  
  // 如果是无效日期，返回空
  if (isNaN(date.getTime())) return '';
  
  // 补零函数
  const pad = (num) => num < 10 ? `0${num}` : num;
  
  // 获取时间部分
  const hours = pad(date.getHours());
  const minutes = pad(date.getMinutes());
  const timeStr = `${hours}:${minutes}`;
  
  // 如果是今天的消息
  if (date.toDateString() === now.toDateString()) {
    return timeStr;
  }
  
  // 如果是昨天的消息
  const yesterday = new Date(now);
  yesterday.setDate(yesterday.getDate() - 1);
  if (date.toDateString() === yesterday.toDateString()) {
    return `昨天 ${timeStr}`;
  }
  
  // 如果是今年的消息
  if (date.getFullYear() === now.getFullYear()) {
    return `${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${timeStr}`;
  }
  
  // 其他情况显示完整日期
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${timeStr}`;
};

// 判断两个时间戳是否是同一天
const isSameDay = (timestamp1, timestamp2) => {
  if (!timestamp1 || !timestamp2) return false;
  const date1 = new Date(timestamp1);
  const date2 = new Date(timestamp2);
  return date1.toDateString() === date2.toDateString();
};

// 处理图片URL的工具函数
const handleImageUrl = (url) => {
  if (!url) return config.images.defaultAvatar;
  
  try {
    // 如果是完整的URL（以http或https开头）
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    
    // 如果是本地图片路径（以/images开头）
    if (url.startsWith('/images/')) {
      return url;
    }

    // 如果是上传的头像路径（以/uploads/avatar/开头）
    if (url.startsWith('/uploads/avatar/')) {
      return `${config.images.baseUrl}${url}`;
    }
    
    // 如果是相对路径，拼接服务器基础路径
    return `${config.images.baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  } catch (err) {
    console.error('处理图片URL出错:', err);
    return config.images.defaultAvatar;
  }
};

Page({
  data: {
    userId: null,
    receiverId: null,
    username: "",
    avatar: "",
    userAvatar: "",
    inputText: "",
    chatMessages: [],
    socketTask: null,
    socketOpen: false,
    messageQueue: [], // 消息队列
    reconnectCount: 0,
    maxReconnectAttempts: 5,
    heartbeatInterval: null,
    imageLoadErrors: {} // 记录图片加载错误
  },

  onLoad(options) {
    console.log("进入聊天界面，接收参数:", options);
    const userId = wx.getStorageSync('userId');
    const receiverId = options.targetId;
    const username = decodeURIComponent(options.username || '');
    const profilePhoto = decodeURIComponent(options.profile_photo || '');
    
    if (!userId || !receiverId) {
      wx.showToast({
        title: '用户信息不完整',
        icon: 'none'
      });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }

    // 获取当前用户头像
    const userInfo = wx.getStorageSync('userInfo');
    const userAvatar = userInfo?.avatar || config.images.defaultAvatar;

    this.setData({
      userId: userId,
      receiverId: receiverId,
      username: username || "未知用户",
      avatar: handleImageUrl(profilePhoto),
      userAvatar: handleImageUrl(userAvatar)
    });
  
    // 加载聊天记录
    this.loadChatHistory();
    this.initWebSocket();
  },

  // 加载聊天记录
  loadChatHistory() {
    wx.request({
      url: `${config.baseUrl}/chat/history/${this.data.userId}/${this.data.receiverId}`,
      method: "GET",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        console.log("✅ 聊天记录加载成功: ", res.data);
        
        if (res.statusCode === 200 && Array.isArray(res.data)) {
          const formattedMessages = res.data.map(msg => ({
            id: msg.id,
            content: msg.content,
            type: msg.type || 'chat',
            timestamp: msg.timestamp,
            formattedTime: formatTime(msg.timestamp), // 添加格式化后的时间
            isSelf: msg.senderId === this.data.userId,
            avatar: msg.senderId === this.data.userId ? 
              this.data.userAvatar : 
              this.data.avatar
          }));
          this.setData({ chatMessages: formattedMessages });
        } else {
          console.log("没有聊天记录或返回格式不正确");
          this.setData({ chatMessages: [] });
        }
      },
      fail: (err) => {
        console.error("❌ 获取聊天记录失败: ", err);
        wx.showToast({
          title: '获取聊天记录失败',
          icon: 'none'
        });
      }
    });
  },

  // 初始化 WebSocket
  initWebSocket() {
    if (this.data.socketOpen) return;

    const userId = this.data.userId;
    if (!userId) {
      console.error("❌ 用户ID不存在，无法建立WebSocket连接");
      return;
    }

    if (this.data.reconnectCount >= this.data.maxReconnectAttempts) {
      console.error("❌ 达到最大重连次数");
      wx.showToast({
        title: '连接失败，请重试',
        icon: 'none'
      });
      return;
    }

    // 构建 WebSocket URL
    const wsUrl = `${config.wsBaseUrl}${config.defaults.wsPath}/${userId}`;
    console.log("🔗 正在连接WebSocket:", wsUrl);

    const socketTask = wx.connectSocket({
      url: wsUrl,
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: () => {
        console.log("✅ WebSocket 请求已发送");
      },
      fail: (err) => {
        console.error("❌ WebSocket 连接失败", err);
        this.handleReconnect();
      }
    });

    // WebSocket 连接成功
    socketTask.onOpen(() => {
      console.log("🔗 WebSocket 连接成功");
      this.setData({ 
        socketOpen: true, 
        socketTask,
        reconnectCount: 0 
      });

      // 启动心跳
      this.startHeartbeat();

      // 发送暂存的消息
      this.sendQueuedMessages();
    });

    // WebSocket 错误处理
    socketTask.onError(err => {
      console.error("❌ WebSocket 错误：", err);
      this.handleConnectionError();
    });

    // WebSocket 接收消息
    socketTask.onMessage((res) => {
      try {
        const msg = JSON.parse(res.data);
        console.log("📩 收到消息:", msg);

        // 处理心跳响应
        if (msg.type === 'pong') {
          console.log("💓 收到心跳响应");
          return;
        }

        // 处理普通消息
        if (msg.type === 'chat') {
          this.handleChatMessage(msg);
        }
      } catch (err) {
        console.error("消息解析错误:", err);
      }
    });

    // WebSocket 关闭
    socketTask.onClose(() => {
      console.log("⚠️ WebSocket 连接关闭");
      this.handleConnectionClosed();
    });

    this.setData({ socketTask });
  },

  // 发送队列中的消息
  sendQueuedMessages() {
    while (this.data.messageQueue.length > 0) {
      const pendingMessage = this.data.messageQueue.shift();
      this._sendWebSocketMessage(pendingMessage);
    }
  },

  // 处理聊天消息
  handleChatMessage(msg) {
    const messages = [...this.data.chatMessages];
    messages.push({
      id: msg.id || Date.now(),
      content: msg.content,
      type: msg.type,
      timestamp: msg.timestamp,
      formattedTime: formatTime(msg.timestamp), // 添加格式化后的时间
      isSelf: msg.senderId === this.data.userId,
      avatar: msg.senderId === this.data.userId ? 
        this.data.userAvatar : 
        this.data.avatar
    });
    this.setData({ 
      chatMessages: messages 
    }, () => {
      // 滚动到最新消息
      this.scrollToBottom();
    });
  },

  // 滚动到最新消息
  scrollToBottom() {
    const query = wx.createSelectorQuery();
    query.select('.chat-box').boundingClientRect();
    query.selectViewport().scrollOffset();
    query.exec((res) => {
      if (res[0] && res[1]) {
        wx.pageScrollTo({
          scrollTop: res[0].bottom,
          duration: 300
        });
      }
    });
  },

  // 处理重连
  handleReconnect() {
    const reconnectCount = this.data.reconnectCount + 1;
    this.setData({ reconnectCount });
    
    if (reconnectCount < config.defaults.wsMaxReconnectAttempts) {
      const delay = Math.min(1000 * Math.pow(2, reconnectCount), 10000);
      console.log(`⏳ 将在 ${delay}ms 后尝试第 ${reconnectCount} 次重连`);
      
      setTimeout(() => {
        if (!this.data.socketOpen) {
          this.initWebSocket();
        }
      }, delay);
    } else {
      wx.showModal({
        title: '连接失败',
        content: '无法连接到聊天服务器，是否重试？',
        success: (res) => {
          if (res.confirm) {
            this.setData({ reconnectCount: 0 });
            this.initWebSocket();
          }
        }
      });
    }
  },

  // 处理连接错误
  handleConnectionError() {
    this.setData({ socketOpen: false, socketTask: null });
    this.stopHeartbeat();
    wx.showToast({
      title: 'WebSocket连接错误',
      icon: 'none'
    });
    this.handleReconnect();
  },

  // 处理连接关闭
  handleConnectionClosed() {
    this.setData({ socketOpen: false, socketTask: null });
    this.stopHeartbeat();
    this.handleReconnect();
  },

  // 启动心跳
  startHeartbeat() {
    this.stopHeartbeat();
    const heartbeatInterval = setInterval(() => {
      if (this.data.socketOpen) {
        this._sendWebSocketMessage({
          type: 'ping',
          timestamp: Date.now()
        });
      }
    }, 30000); // 30秒发送一次心跳
    this.setData({ heartbeatInterval });
  },

  // 停止心跳
  stopHeartbeat() {
    if (this.data.heartbeatInterval) {
      clearInterval(this.data.heartbeatInterval);
      this.setData({ heartbeatInterval: null });
    }
  },

  // 输入框内容变更
  onInput(e) {
    this.setData({ inputText: e.detail.value });
  },

  // 发送消息
  sendMessage() {
    const messageContent = this.data.inputText.trim();
    if (!messageContent) return;

    const message = {
      type: 'chat',
      senderId: this.data.userId,
      receiverId: this.data.receiverId,
      content: messageContent,
      timestamp: Date.now() // 使用当前时间戳
    };

    if (!this.data.socketOpen || !this.data.socketTask) {
      console.warn("⚠️ WebSocket 连接未建立，暂存消息");
      this.data.messageQueue.push(message);
      this.initWebSocket(); // 尝试重新连接
      return;
    }

    this._sendWebSocketMessage(message);
  },

  // WebSocket 发送消息方法
  _sendWebSocketMessage(message) {
    this.data.socketTask.send({
      data: JSON.stringify(message),
      success: () => {
        console.log("✅ 发送消息成功", message);
        
        // 只有聊天消息才添加到消息列表
        if (message.type === 'chat') {
          const messages = [...this.data.chatMessages];
          messages.push({
            id: message.timestamp,
            content: message.content,
            type: message.type,
            timestamp: message.timestamp,
            formattedTime: formatTime(message.timestamp), // 添加格式化后的时间
            isSelf: true
          });
          this.setData({
            chatMessages: messages,
            inputText: ""
          });
        }
      },
      fail: (err) => {
        console.error("❌ WebSocket 发送失败:", err);
        if (message.type === 'chat') {
          wx.showToast({
            title: "发送失败",
            icon: "none"
          });
          // 将消息加入队列，等待重连后发送
          this.data.messageQueue.push(message);
        }
      }
    });
  },

  // 图片加载失败处理
  handleImageError(e) {
    const type = e.currentTarget.dataset.type || 'avatar';
    const index = e.currentTarget.dataset.index;
    
    // 更新图片加载错误状态
    const imageLoadErrors = { ...this.data.imageLoadErrors };
    const key = `${type}_${index || 'main'}`;
    
    if (!imageLoadErrors[key]) {
      imageLoadErrors[key] = true;
      
      // 根据不同类型设置不同的占位图
      if (type === 'avatar') {
        if (index !== undefined) {
          // 消息列表中的头像
          const messages = [...this.data.chatMessages];
          if (messages[index]) {
            messages[index].tempAvatar = config.images.avatarPlaceholder;
            this.setData({ 
              chatMessages: messages,
              imageLoadErrors
            });
          }
        } else {
          // 主头像
          this.setData({
            avatar: config.images.avatarPlaceholder,
            imageLoadErrors
          });
        }
      } else if (type === 'user-avatar') {
        this.setData({
          userAvatar: config.images.avatarPlaceholder,
          imageLoadErrors
        });
      }
    }
  },

  // 返回按钮处理
  handleBack() {
    // 如果WebSocket连接还在，先关闭
    if (this.data.socketTask) {
      this.data.socketTask.close({
        success: () => {
          console.log("✅ WebSocket连接已关闭");
        }
      });
    }
    
    // 停止心跳
    this.stopHeartbeat();
    
    // 返回上一页
    wx.navigateBack({
      delta: 1,
      fail: () => {
        // 如果返回失败（没有上一页），则跳转到首页
        wx.switchTab({
          url: '/pages/index/index'
        });
      }
    });
  },

  // 页面卸载
  onUnload() {
    // 页面卸载时清理资源
    if (this.data.socketTask) {
      this.data.socketTask.close({
        success: () => {
          console.log("✅ WebSocket连接已关闭");
        }
      });
    }
    this.stopHeartbeat();
  },

  // 添加到 Page 中供模板使用
  isSameDay: isSameDay,
});
