import config from "../../utils/config";

Page({
  data: {
    userId: null,
    userInfo: {
      avatar: config.defaults.avatar,
      nickname: '',
      gender: '',
      age: '',
      constellation: '',
      location: '',
      occupation: '',
      height: '',
      signature: '',
      isVip: false,
      level: 1
    }
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ userId: options.id });
      this.loadUserProfile(options.id);
    } else {
      wx.showToast({
        title: '用户ID不存在',
        icon: 'error'
      });
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    }
  },

  // 加载用户资料
  loadUserProfile(userId) {
    wx.showLoading({
      title: '加载中...'
    });

    wx.request({
      url: `${config.baseUrl}/users/user/${userId}`,
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data;
          this.setData({
            userInfo: {
              avatar: data.profilePhoto || config.defaults.avatar,
              nickname: data.username || '未设置昵称',
              gender: data.gender || '未设置',
              age: data.age ? `${data.age}岁` : '未知',
              constellation: data.constellation || '未知',
              location: data.location || '未设置',
              occupation: data.occupation || '未设置',
              height: data.height ? `${data.height}cm` : '',
              signature: data.bio || '',
              isVip: data.isVip || false,
              level: data.level || 1
            }
          });
        } else {
          wx.showToast({
            title: '获取用户信息失败',
            icon: 'error'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '网络错误',
          icon: 'error'
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  // 返回上一页
  onBack() {
    wx.navigateBack();
  },

  // 点击喜欢
  onLike() {
    wx.request({
      url: `${config.baseUrl}/likes/${this.data.userId}`,
      method: 'POST',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        if (res.statusCode === 200) {
          wx.showToast({
            title: '已喜欢',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: res.data?.message || '操作失败',
            icon: 'error'
          });
        }
      }
    });
  },

  // 开始聊天
  onChat() {
    const { userId } = this.data;
    const { nickname, avatar } = this.data.userInfo;
    
    wx.navigateTo({
      url: `/pages/chat/chat?targetId=${userId}&username=${encodeURIComponent(nickname)}&profile_photo=${encodeURIComponent(avatar)}`,
      fail: (err) => {
        console.error('导航到聊天页面失败:', err);
        wx.showToast({
          title: '打开聊天失败',
          icon: 'none'
        });
      }
    });
  },

  // 预览照片
  previewPhoto(e) {
    const { index } = e.currentTarget.dataset;
    const { photos } = this.data.userInfo;
    
    if (!photos || photos.length === 0) return;
    
    wx.previewImage({
      current: photos[index],
      urls: photos
    });
  }
}); 