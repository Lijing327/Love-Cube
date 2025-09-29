import config from "../../utils/config";

Page({
  data: {
    notifications: [
      {
        id: 1,
        type: 'message',
        content: '你好！很高兴认识你，希望能和你成为朋友。',
        userInfo: {
          nickname: '小明',
          avatar: '/images/default-avatar.svg'
        },
        createTime: '2小时前',
        isRead: false
      },
      {
        id: 2,
        type: 'like',
        content: '给你点了个赞！',
        userInfo: {
          nickname: '小红',
          avatar: '/images/default-avatar.svg'
        },
        createTime: '5小时前',
        isRead: false
      },
      {
        id: 3,
        type: 'message',
        content: '你的动态很有趣，期待看到更多分享！',
        userInfo: {
          nickname: '美食达人',
          avatar: '/images/default-avatar.svg'
        },
        createTime: '1天前',
        isRead: true
      }
    ],
    unreadCount: 2
  },

  onLoad() {
    this.loadNotifications();
  },

  onShow() {
    this.loadNotifications();
  },

  // 加载留言通知
  loadNotifications() {
    const token = wx.getStorageSync('token');
    
    // 如果有token和配置，尝试加载真实数据
    if (token && config.baseUrl) {
      wx.request({
        url: `${config.baseUrl}/message-wall/unread-count`,
        method: "GET",
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          console.log("✅ 留言通知加载成功: ", res.data);
          if (res.data && res.data.success) {
            // 更新未读数量
            this.setData({ unreadCount: res.data.data || 0 });
          }
        },
        fail: (err) => {
          console.error("❌ 获取留言通知失败: ", err);
          // 失败时保持示例数据
        }
      });
    } else {
      // 没有token时保持示例数据
      console.log("使用示例数据");
    }
  },

  // 标记所有留言为已读
  markAllAsRead() {
    const token = wx.getStorageSync('token');
    
    if (token && config.baseUrl) {
      wx.request({
        url: `${config.baseUrl}/message-wall/mark-all-read`,
        method: "POST",
        header: {
          'Authorization': 'Bearer ' + token
        },
        success: (res) => {
          if (res.data && res.data.success) {
            wx.showToast({
              title: '已标记为已读',
              icon: 'success'
            });
            this.setData({ unreadCount: 0 });
          }
        },
        fail: (err) => {
          console.error("❌ 标记已读失败: ", err);
          wx.showToast({
            title: '操作失败',
            icon: 'none'
          });
        }
      });
    } else {
      // 示例数据模式：直接标记为已读
      wx.showToast({
        title: '已标记为已读',
        icon: 'success'
      });
      this.setData({ unreadCount: 0 });
    }
  },

  // 跳转到我的留言墙
  goToMyMessageWall() {
    const currentUserId = wx.getStorageSync('userId');
    if (!currentUserId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }
    
    wx.navigateTo({
      url: `/pages/message-wall/message-wall?targetId=${currentUserId}&username=${encodeURIComponent('我的留言墙')}&profile_photo=${encodeURIComponent('')}`,
      fail: (err) => {
        console.error('跳转失败:', err);
        wx.showToast({
          title: '跳转失败',
          icon: 'none'
        });
      }
    });
  },

  // 回复消息
  replyMessage(e) {
    const { id } = e.currentTarget.dataset;
    const notification = this.data.notifications.find(item => item.id === id);
    
    if (notification) {
      wx.showToast({
        title: '回复功能开发中',
        icon: 'none'
      });
      // TODO: 实现回复功能
    }
  },

  // 标记单条消息为已读
  markAsRead(e) {
    const { id } = e.currentTarget.dataset;
    const notifications = this.data.notifications.map(item => {
      if (item.id === id) {
        return { ...item, isRead: true };
      }
      return item;
    });
    
    const unreadCount = notifications.filter(item => !item.isRead).length;
    
    this.setData({
      notifications,
      unreadCount
    });
    
    wx.showToast({
      title: '已标记为已读',
      icon: 'success'
    });
  }
});
