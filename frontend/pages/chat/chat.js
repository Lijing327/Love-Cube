import config from "../utils/config";

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
  },
  onLoad(options) {
    console.log("进入聊天界面，接收参数:", options);
    this.setData({
      userId: wx.getStorageSync('userId'),
      receiverId: options.userId,
      username: options.username || "未知用户",
      avatar: options.profile_photo ? options.profile_photo : "/images/默认头像.jpg"
    });
  
    wx.request({
      url: `${config.baseUrl}/chat/${this.data.userId}/${this.data.receiverId}`,
      method: "GET",
      success: (res) => { // 使用箭头函数确保 this 作用域正确
        console.log("✅ 聊天记录加载成功: ", res.data);
    
        let formattedMessages = res.data.map(msg => ({
          id: msg.id,
          content: msg.content,
          isSelf: msg.senderId === this.data.userId
        }));
    
        this.setData({ chatMessages: formattedMessages });
      },
      fail: (err) => {
        console.error("❌ 获取聊天记录失败: ", err);
      }
    });

  
    this.initWebSocket();
  },

  // **初始化 WebSocket**
  initWebSocket() {
    let that = this;
    if (this.data.socketOpen) return;

    console.log("🔗 WebSocket 正在连接...");
    const socketTask = wx.connectSocket({
      url: `${config.wsBaseUrl}/${this.data.userId}`,
      success() {
        console.log("✅ WebSocket 请求已发送");
      },
      fail(err) {
        console.error("❌ WebSocket 连接失败", err);
      }
      
    });
    socketTask.onOpen(() => console.log("✅ WebSocket 连接成功"));
    socketTask.onError(err => console.error("❌ WebSocket 错误：", err));

    // **WebSocket 连接成功**
    socketTask.onOpen(() => {
      console.log("🔗 WebSocket 连接成功");
      that.setData({ socketOpen: true, socketTask });

      // **自动发送所有暂存的消息**
      while (that.data.messageQueue.length > 0) {
        let pendingMessage = that.data.messageQueue.shift();
        that._sendWebSocketMessage(pendingMessage);
      }
    });

    // **WebSocket 接收消息**
    socketTask.onMessage((res) => {
      let msg = JSON.parse(res.data);
      console.log("📩 收到消息:", msg);

      let messages = this.data.chatMessages;
      messages.push({
        id: msg.id,
        content: msg.content,
        isSelf: msg.senderId === this.data.userId
      });
      this.setData({ chatMessages: messages });
    });

    // **WebSocket 关闭**
    socketTask.onClose(() => {
      console.log("⚠️ WebSocket 连接关闭");
      that.setData({ socketOpen: false, socketTask: null });
    });

    this.setData({ socketTask });
  },

  // **输入框内容变更**
  onInput(e) {
    this.setData({ inputText: e.detail.value });
  },

  // **发送消息**
  sendMessage() {
    let that = this;
    let messageContent = this.data.inputText.trim();

    if (!messageContent) return;

    const message = {
      senderId: this.data.userId,
      receiverId: this.data.receiverId,
      content: messageContent
    };

    if (!this.data.socketOpen || !this.data.socketTask) {
      console.warn("⚠️ WebSocket 连接未建立，暂存消息");
      this.data.messageQueue.push(message);
      return;
    }

    this._sendWebSocketMessage(message);
  },

  // **WebSocket 发送消息方法**
  _sendWebSocketMessage(message) {
    let that = this;

    this.data.socketTask.send({
      data: JSON.stringify(message),
      success: () => {
        console.log("✅ 发送消息成功", message);
        let messages = that.data.chatMessages;
        messages.push({ id: Date.now(), content: message.content, isSelf: true });
        that.setData({ chatMessages: messages, inputText: "" });
      },
      fail(err) {
        console.error("❌ WebSocket 发送失败:", err);
        wx.showToast({ title: "发送失败", icon: "none" });
      }
    });
  }
});
