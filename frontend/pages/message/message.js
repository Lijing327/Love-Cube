import config from "../../utils/config";

Page({
  data: {
    currentTab: 'chat',
    unreadChat: 0,
    unreadInteract: 0,
    unreadVisitor: 0,
    chatList: [],
    interactList: [],
    visitorList: [],
    showMoreActions: false,
    showEmpty: false,
    moreActions: [
      { name: '发起群聊', color: '#333' },
      { name: '添加好友', color: '#333' }
    ],
    emptyText: {
      chat: '暂无聊天消息',
      interact: '暂无互动消息',
      visitor: '暂无访客记录'
    }
  },

  onLoad() {
    this.loadMessages();
  },

  onShow() {
    // 每次显示页面时更新未读消息数
    this.updateUnreadCount();
  },

  onPullDownRefresh() {
    this.loadMessages();
    wx.stopPullDownRefresh();
  },

  // 加载消息数据
  loadMessages() {
    switch (this.data.currentTab) {
      case 'chat':
        this.loadChatList();
        break;
      case 'interact':
        this.loadInteractList();
        break;
      case 'visitor':
        this.loadVisitorList();
        break;
    }
  },

  // 加载聊天列表
  loadChatList() {
    wx.request({
      url: config.baseUrl + '/messages/chat',
      method: 'GET',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const chatList = res.data.map(item => ({
            ...item,
            lastTime: this.formatTime(item.lastTime)
          }));
          this.setData({
            chatList,
            showEmpty: chatList.length === 0
          });
        }
      }
    });
  },

  // 加载互动列表
  loadInteractList() {
    wx.request({
      url: config.baseUrl + '/messages/interact',
      method: 'GET',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const interactList = res.data.map(item => ({
            ...item,
            time: this.formatTime(item.time)
          }));
          this.setData({
            interactList,
            showEmpty: interactList.length === 0
          });
        }
      }
    });
  },

  // 加载访客列表
  loadVisitorList() {
    wx.request({
      url: config.baseUrl + '/messages/visitor',
      method: 'GET',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const visitorList = res.data.map(item => ({
            ...item,
            visitTime: this.formatTime(item.visitTime)
          }));
          this.setData({
            visitorList,
            showEmpty: visitorList.length === 0
          });
        }
      }
    });
  },

  // 更新未读消息数
  updateUnreadCount() {
    wx.request({
      url: config.baseUrl + '/messages/unread',
      method: 'GET',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({
            unreadChat: res.data.chat || 0,
            unreadInteract: res.data.interact || 0,
            unreadVisitor: res.data.visitor || 0
          });

          // 更新TabBar的消息数
          const totalUnread = res.data.chat + res.data.interact + res.data.visitor;
          if (totalUnread > 0) {
            wx.setTabBarBadge({
              index: 2,
              text: totalUnread.toString()
            });
          } else {
            wx.removeTabBarBadge({
              index: 2
            });
          }
        }
      }
    });
  },

  // 切换标签页
  switchTab(e) {
    const tab = e.currentTarget.dataset.tab;
    this.setData({
      currentTab: tab,
      showEmpty: false
    }, () => {
      this.loadMessages();
    });
  },

  // 进入聊天
  enterChat(e) {
    const { id, type } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/chat/chat?id=${id}&type=${type}`
    });
  },

  // 处理互动
  handleInteract(e) {
    const { id, type } = e.currentTarget.dataset;
    // 根据互动类型处理
    switch (type) {
      case 'like':
      case 'comment':
        wx.navigateTo({
          url: `/pages/dynamic/detail?id=${id}`
        });
        break;
      case 'follow':
        wx.navigateTo({
          url: `/pages/profile/profile?id=${id}`
        });
        break;
      case 'gift':
        this.viewGiftDetail(id);
        break;
    }
  },

  // 查看礼物详情
  viewGiftDetail(id) {
    wx.navigateTo({
      url: `/pages/gift/detail?id=${id}`
    });
  },

  // 关注用户
  followBack(e) {
    const { id } = e.currentTarget.dataset;
    wx.request({
      url: config.baseUrl + `/users/${id}/follow`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          wx.showToast({
            title: '关注成功',
            icon: 'success'
          });
          this.loadInteractList();
        }
      }
    });
  },

  // 回赠礼物
  sendGift(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/gift/send?userId=${id}`
    });
  },

  // 查看用户资料
  viewProfile(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/profile/profile?id=${id}`
    });
  },

  // 打招呼
  sayHi(e) {
    const { id } = e.currentTarget.dataset;
    wx.request({
      url: config.baseUrl + `/users/${id}/greet`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          wx.showToast({
            title: '已打招呼',
            icon: 'success'
          });
          // 跳转到聊天页面
          wx.navigateTo({
            url: `/pages/chat/chat?id=${id}&type=single`
          });
        }
      }
    });
  },

  // 搜索
  onSearch() {
    wx.navigateTo({
      url: '/pages/message/search'
    });
  },

  // 显示更多操作
  showMoreActions() {
    this.setData({
      showMoreActions: true
    });
  },

  // 关闭更多操作
  onCloseMoreActions() {
    this.setData({
      showMoreActions: false
    });
  },

  // 选择操作
  onSelectAction(e) {
    const { name } = e.detail;
    switch (name) {
      case '发起群聊':
        wx.navigateTo({
          url: '/pages/chat/create-group'
        });
        break;
      case '添加好友':
        wx.navigateTo({
          url: '/pages/contact/add-friend'
        });
        break;
    }
    this.onCloseMoreActions();
  },

  // 格式化时间
  formatTime(timestamp) {
    const now = new Date().getTime();
    const diff = now - timestamp;
    
    if (diff < 60000) { // 1分钟内
      return '刚刚';
    } else if (diff < 3600000) { // 1小时内
      return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) { // 24小时内
      return Math.floor(diff / 3600000) + '小时前';
    } else if (diff < 604800000) { // 7天内
      return Math.floor(diff / 86400000) + '天前';
    } else {
      const date = new Date(timestamp);
      return `${date.getMonth() + 1}月${date.getDate()}日`;
    }
  }
}); 