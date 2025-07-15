import config from "../../utils/config";

Page({
  data: {
    notifications: [], // 改为通知列表
    unreadCount: 0
  },

  onLoad() {
    this.loadNotifications();
    this.loadUnreadCount();
  },

  onShow() {
    this.loadNotifications();
    this.loadUnreadCount();
  },

  // 加载留言通知
  loadNotifications() {
    wx.request({
      url: `${config.baseUrl}/message-wall/unread-count`,
      method: "GET",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        console.log("✅ 留言通知加载成功: ", res.data);
        if (res.data && res.data.success) {
          // 这里简化处理，实际应该获取详细的通知列表
          this.setData({ unreadCount: res.data.data || 0 });
        }
      },
      fail: (err) => {
        console.error("❌ 获取留言通知失败: ", err);
      }
    });
  },

  // 加载未读留言数量
  loadUnreadCount() {
    wx.request({
      url: `${config.baseUrl}/message-wall/unread-count`,
      method: "GET",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        if (res.data && res.data.success) {
          this.setData({ unreadCount: res.data.data || 0 });
        }
      },
      fail: (err) => {
        console.error("❌ 获取未读留言数量失败: ", err);
      }
    });
  },

  // 标记所有留言为已读
  markAllAsRead() {
    wx.request({
      url: `${config.baseUrl}/message-wall/mark-all-read`,
      method: "POST",
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
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
  }
});
