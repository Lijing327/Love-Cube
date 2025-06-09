import Dialog from '@vant/weapp/dialog/dialog';
import config from "../../utils/config";

Page({
  data: {
    userInfo: null,
    statistics: null,
    completionRate: 0,
    isLoading: false
  },

  onLoad() {
    this.getUserInfo();
  },

  onShow() {
    // 检查登录状态并刷新数据
    const token = wx.getStorageSync('token');
    if (token) {
      this.getUserInfo();
    } else {
      wx.showToast({
        title: '请先登录',
        icon: 'none',
        success: () => {
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            });
          }, 1500);
        }
      });
    }
  },

  onHide() {
    // 确保在页面隐藏时清除loading状态
    if (this.data.isLoading) {
      wx.hideLoading();
      this.setData({ isLoading: false });
    }
  },

  onUnload() {
    // 确保在页面卸载时清除loading状态
    if (this.data.isLoading) {
      wx.hideLoading();
      this.setData({ isLoading: false });
    }
  },

  getUserInfo() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none',
        success: () => {
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            });
          }, 1500);
        }
      });
      return;
    }

    // 设置loading状态
    this.setData({ isLoading: true });
    wx.showLoading({
      title: '加载中...',
      mask: true
    });

    wx.request({
      url: config.baseUrl + '/users/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + token
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const { statistics, completionRate, ...userInfo } = res.data;
          this.setData({
            userInfo,
            statistics,
            completionRate
          });
        } else if (res.statusCode === 401) {
          // 清除无效token
          wx.removeStorageSync('token');
          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            success: () => {
              setTimeout(() => {
                wx.redirectTo({
                  url: '/pages/login/login'
                });
              }, 1500);
            }
          });
        } else {
          wx.showToast({
            title: res.data?.message || '获取信息失败',
            icon: 'error'
          });
        }
      },
      fail: (err) => {
        console.error('获取用户信息失败:', err);
        wx.showToast({
          title: '网络错误',
          icon: 'error'
        });
      },
      complete: () => {
        // 清除loading状态
        if (this.data.isLoading) {
          wx.hideLoading();
          this.setData({ isLoading: false });
        }
      }
    });
  },

  // 根据生日计算星座
  getConstellation(birthDate) {
    if (!birthDate) return '未知';
    // 实现星座计算逻辑
    const date = new Date(birthDate);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    
    const constellations = [
      {name: '摩羯座', start: [1, 1], end: [1, 19]},
      {name: '水瓶座', start: [1, 20], end: [2, 18]},
      {name: '双鱼座', start: [2, 19], end: [3, 20]},
      {name: '白羊座', start: [3, 21], end: [4, 19]},
      {name: '金牛座', start: [4, 20], end: [5, 20]},
      {name: '双子座', start: [5, 21], end: [6, 21]},
      {name: '巨蟹座', start: [6, 22], end: [7, 22]},
      {name: '狮子座', start: [7, 23], end: [8, 22]},
      {name: '处女座', start: [8, 23], end: [9, 22]},
      {name: '天秤座', start: [9, 23], end: [10, 23]},
      {name: '天蝎座', start: [10, 24], end: [11, 22]},
      {name: '射手座', start: [11, 23], end: [12, 21]},
      {name: '摩羯座', start: [12, 22], end: [12, 31]}
    ];

    for (let constellation of constellations) {
      const [startMonth, startDay] = constellation.start;
      const [endMonth, endDay] = constellation.end;
      
      if ((month === startMonth && day >= startDay) || 
          (month === endMonth && day <= endDay)) {
        return constellation.name;
      }
    }
    
    return '未知';
  },

  // 编辑资料
  onEditProfile() {
    wx.navigateTo({
      url: '/pages/profile/profile'
    });
  },

  // 查看VIP
  viewVip() {
    wx.navigateTo({
      url: '/pages/vip/vip'
    });
  },

  // 退出登录
  onLogout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          const token = wx.getStorageSync('token');
          if (token) {
            // 设置loading状态
            this.setData({ isLoading: true });
            wx.showLoading({
              title: '退出中...',
              mask: true
            });

            wx.request({
              url: config.baseUrl + '/users/logout',
              method: 'POST',
              header: {
                'Authorization': 'Bearer ' + token
              },
              success: () => {
                // 清除本地存储
                wx.removeStorageSync('token');
                wx.removeStorageSync('userInfo');
                
                wx.showToast({
                  title: '已退出登录',
                  icon: 'success',
                  success: () => {
                    setTimeout(() => {
                      wx.redirectTo({
                        url: '/pages/login/login'
                      });
                    }, 1500);
                  }
                });
              },
              fail: () => {
                wx.showToast({
                  title: '退出失败',
                  icon: 'error'
                });
              },
              complete: () => {
                // 清除loading状态
                if (this.data.isLoading) {
                  wx.hideLoading();
                  this.setData({ isLoading: false });
                }
              }
            });
          } else {
            wx.redirectTo({
              url: '/pages/login/login'
            });
          }
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
