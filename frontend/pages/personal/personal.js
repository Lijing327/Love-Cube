import Dialog from '@vant/weapp/dialog/dialog';
import config from "../utils/config";

Page({
  data: {
    userInfo: {
      avatar: "",
      nickname: "",
      gender: "",
      age: 0,
      location: "",
      isVip: false,
      level: 1,
      constellation: "",
      signature: "",
      completionRate: 0,
      totalLikes: 0,
      totalVisitors: 0,
      totalMatches: 0,
      vipExpireDate: null,
      walletBalance: 0,
      unreadMessages: 0
    },
    statistics: {
      todayVisitors: 0,
      newLikes: 0,
      newMatches: 0
    }
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync("token");
    if (!token) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return false;
    }
    return true;
  },

  // 获取用户信息
  getUserInfo() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: config.baseUrl + "/users/me",
        method: "GET",
        header: {
          Authorization: "Bearer " + wx.getStorageSync("token")
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = res.data;
            this.setData({
              userInfo: {
                avatar: data.profilePhoto || '/images/default-avatar.png',
                nickname: data.nickname || "用户名",
                gender: data.gender || "未知",
                age: data.age || 25,
                location: data.location || "未知",
                isVip: data.isVip || false,
                level: data.level || 1,
                constellation: data.constellation || "未知",
                signature: data.signature || "",
                completionRate: this.calculateCompletionRate(data),
                totalLikes: data.totalLikes || 0,
                totalVisitors: data.totalVisitors || 0,
                totalMatches: data.totalMatches || 0,
                vipExpireDate: data.vipExpireDate,
                walletBalance: data.walletBalance || 0,
                unreadMessages: data.unreadMessages || 0
              }
            });
            resolve();
          } else {
            wx.showToast({
              title: "获取信息失败",
              icon: "error"
            });
            reject();
          }
        },
        fail: reject
      });
    });
  },

  // 获取统计数据
  getStatistics() {
    return new Promise((resolve, reject) => {
      wx.request({
        url: config.baseUrl + "/users/statistics",
        method: "GET",
        header: {
          Authorization: "Bearer " + wx.getStorageSync("token")
        },
        success: (res) => {
          if (res.statusCode === 200) {
            this.setData({
              statistics: res.data
            });
            resolve();
          } else {
            reject();
          }
        },
        fail: reject
      });
    });
  },

  // 计算资料完整度
  calculateCompletionRate(data) {
    const requiredFields = [
      'nickname',
      'gender',
      'age',
      'location',
      'constellation',
      'signature',
      'profilePhoto'
    ];
    
    const completedFields = requiredFields.filter(field => {
      return data[field] && data[field] !== '未知';
    });
    
    return Math.floor((completedFields.length / requiredFields.length) * 100);
  },

  // 页面显示时获取数据
  onShow() {
    if (!this.checkLoginStatus()) return;
    
    wx.showLoading({
      title: '加载中...'
    });

    Promise.all([
      this.getUserInfo(),
      this.getStatistics()
    ]).then(() => {
      wx.hideLoading();
    }).catch(() => {
      wx.hideLoading();
      wx.showToast({
        title: '数据加载失败',
        icon: 'error'
      });
    });
  },

  // 下拉刷新
  onPullDownRefresh() {
    Promise.all([
      this.getUserInfo(),
      this.getStatistics()
    ]).then(() => {
      wx.stopPullDownRefresh();
    }).catch(() => {
      wx.stopPullDownRefresh();
      wx.showToast({
        title: '刷新失败',
        icon: 'error'
      });
    });
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: "退出登录",
      content: "确定要退出登录吗？",
      success: (res) => {
        if (res.confirm) {
          wx.clearStorage();
          wx.reLaunch({
            url: "/pages/login/login"
          });
        }
      }
    });
  },

  // 显示关于我们
  showAboutDialog() {
    Dialog.alert({
      title: '关于我们',
      message: '心愿魔方致力于为用户提供优质的社交服务。\n客服电话：400-123-4567\n版本号：1.0.0',
    });
  }
});
