import config from "../../utils/config";

Page({
  data: {
    userId: null,
    targetUserId: null,
    username: "",
    avatar: "",
    userAvatar: "",
    inputText: "",
    messages: [],
    canLeaveMessage: false // 是否可以留言（需要相互关注）
  },

  onLoad(options) {
    console.log("进入留言墙页面，接收参数:", options);
    const userId = wx.getStorageSync('userId');
    const targetUserId = options.targetId;
    const username = decodeURIComponent(options.username || '');
    const profilePhoto = decodeURIComponent(options.profile_photo || '');
    
    if (!userId || !targetUserId) {
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
      userId: parseInt(userId),
      targetUserId: parseInt(targetUserId),
      username: username || "未知用户",
      avatar: this.handleImageUrl(profilePhoto),
      userAvatar: this.handleImageUrl(userAvatar)
    });

    // 加载留言墙消息
    this.loadMessages();
    // 检查是否可以留言
    this.checkCanLeaveMessage();
  },

  // 加载留言墙消息
  loadMessages() {
    wx.request({
      url: `${config.baseUrl}/message-wall/messages/${this.data.targetUserId}`,
      method: "GET",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        console.log("✅ 留言墙消息加载成功: ", res.data);
        
        if (res.statusCode === 200 && res.data.success) {
          const messages = res.data.data || [];
          this.setData({ messages });
        } else {
          console.log("获取留言墙消息失败");
          this.setData({ messages: [] });
        }
      },
      fail: (err) => {
        console.error("❌ 获取留言墙消息失败: ", err);
        wx.showToast({
          title: '获取留言失败',
          icon: 'none'
        });
      }
    });
  },

  // 检查是否可以留言（需要相互关注）
  checkCanLeaveMessage() {
    wx.request({
      url: `${config.baseUrl}/user-interactions/can-leave-message/${this.data.targetUserId}`,
      method: "GET",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.success) {
          this.setData({ canLeaveMessage: res.data.data });
        }
      },
      fail: (err) => {
        console.error("❌ 检查留言权限失败: ", err);
      }
    });
  },

  // 输入框内容变化
  onInputChange(e) {
    this.setData({
      inputText: e.detail.value
    });
  },

  // 发送留言
  sendMessage() {
    const { inputText, targetUserId } = this.data;
    
    if (!inputText.trim()) {
      wx.showToast({
        title: '请输入留言内容',
        icon: 'none'
      });
      return;
    }

    if (!this.data.canLeaveMessage) {
      wx.showToast({
        title: '需要相互关注后才能留言',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({ title: '发送中...', mask: true });

    wx.request({
      url: `${config.baseUrl}/message-wall/send`,
      method: "POST",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token'),
        'Content-Type': 'application/json'
      },
      data: {
        targetUserId: targetUserId,
        content: inputText.trim()
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.success) {
          wx.showToast({
            title: '留言成功',
            icon: 'success'
          });
          
          // 清空输入框
          this.setData({ inputText: '' });
          
          // 重新加载留言
          this.loadMessages();
        } else {
          wx.showToast({
            title: res.data.message || '发送失败',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error("❌ 发送留言失败: ", err);
        wx.showToast({
          title: '发送失败',
          icon: 'none'
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  // 处理图片URL
  handleImageUrl(url) {
    if (!url) return config.images.defaultAvatar;
    
    try {
      if (url.startsWith('http://') || url.startsWith('https://')) {
        return url;
      }
      
      if (url.startsWith('/images/')) {
        return url;
      }

      if (url.startsWith('/uploads/avatar/')) {
        return `${config.images.baseUrl}${url}`;
      }
      
      return `${config.images.baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
    } catch (err) {
      console.error('处理图片URL出错:', err);
      return config.images.defaultAvatar;
    }
  },

  // 处理图片加载错误
  handleImageError(e) {
    const type = e.currentTarget.dataset.type;
    const index = e.currentTarget.dataset.index;
    
    console.warn('图片加载失败:', type, e);

    switch(type) {
      case 'avatar':
        this.setData({
          avatar: config.images.defaultAvatar
        });
        break;
      
      case 'user-avatar':
        this.setData({
          userAvatar: config.images.defaultAvatar
        });
        break;
      
      case 'message-avatar':
        if (typeof index !== 'undefined') {
          const key = `messages[${index}].avatar`;
          this.setData({
            [key]: config.images.defaultAvatar
          });
        }
        break;
    }
  },

  // 返回上一页
  handleBack() {
    wx.navigateBack();
  }
}); 