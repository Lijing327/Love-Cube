const Dialog = require('@vant/weapp/dialog/dialog');
const config = require("../../utils/config");
const areaList = require("../../utils/area-data");
const { getToken } = require("../../utils/auth");

Page({
  data: {
    userInfo: null,
    avatarUrl: '',
    showEditModal: false,
    showDatePicker: false,
    showLocationPicker: false,
    minDate: new Date(1960, 0, 1).getTime(),
    maxDate: new Date().getTime(),
    areaList: areaList,
    tempUserInfo: {}, // 用于临时存储编辑的用户信息
    isLoading: false,
    loadError: false,
    debugInfo: {} // 用于调试的信息
  },

  onLoad() {
    console.log('个人页面 onLoad');
    this.checkLoginStatus();
  },

  onShow() {
    console.log('个人页面 onShow');
    this.checkLoginStatus();
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

  loadUserInfo() {
    const token = getToken();
    if (!token) {
      console.log('loadUserInfo: 未检测到token');
      this.setData({ 
        loadError: true,
        debugInfo: { error: 'No token found' }
      });
      return;
    }

    console.log('开始加载用户信息');
    console.log('API地址:', `${config.baseUrl}/users/me`);
    console.log('Token:', token);

    // 设置loading状态
    this.setData({ 
      isLoading: true,
      loadError: false,
      debugInfo: {
        loadingStartTime: new Date().toISOString(),
        apiUrl: `${config.baseUrl}/users/me`
      }
    });
    
    wx.showLoading({
      title: '加载中...',
      mask: true
    });

    wx.request({
      url: `${config.baseUrl}/users/me`,
      header: {
        'Authorization': `Bearer ${token}`
      },
      success: (res) => {
        console.log('获取用户信息响应:', res);
        
        this.setData({
          debugInfo: {
            ...this.data.debugInfo,
            responseStatus: res.statusCode,
            responseData: res.data
          }
        });

        if (res.statusCode === 200 && res.data) {
          console.log('完整的用户信息对象:', JSON.stringify(res.data, null, 2));
          console.log('用户信息中的所有字段:', Object.keys(res.data));
          
          // 临时解决方案：从本地存储获取照片数据（后端修复前的临时方案）
          const localPhotos = wx.getStorageSync('userPhotos') || [];
          console.log('本地存储的照片数据:', localPhotos);
          
          // 处理照片URL
          const processedPhotos = res.data.photos ? 
            res.data.photos.map(photo => this.handleImageUrl(photo)) : 
            localPhotos.map(photo => this.handleImageUrl(photo));
          console.log('原始照片数据:', res.data.photos);
          console.log('处理后的照片数据:', processedPhotos);
          
          this.setData({
            userInfo: {
              ...res.data,
              photos: processedPhotos
            },
            avatarUrl: this.handleImageUrl(res.data.profilePhoto || res.data.avatar),
            tempUserInfo: { ...res.data },
            loadError: false
          });
          console.log('用户信息加载成功:', res.data);
          console.log('处理后的头像URL:', this.handleImageUrl(res.data.profilePhoto || res.data.avatar));
        } else if (res.statusCode === 401) {
          console.log('token已过期');
          wx.removeStorageSync('token');
          this.setData({ loadError: true });
          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 1500,
            success: () => {
              setTimeout(() => {
                wx.redirectTo({
                  url: '/pages/login/login'
                });
              }, 1500);
            }
          });
        } else {
          console.log('加载失败:', res);
          this.setData({ 
            loadError: true,
            debugInfo: {
              ...this.data.debugInfo,
              error: res.data?.message || '未知错误'
            }
          });
          wx.showToast({
            title: res.data?.message || '获取信息失败',
            icon: 'error',
            duration: 2000
          });
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        this.setData({ 
          loadError: true,
          debugInfo: {
            ...this.data.debugInfo,
            error: err.errMsg || '网络错误'
          }
        });
        wx.showToast({
          title: '网络错误，请检查网络连接',
          icon: 'error',
          duration: 2000
        });
      },
      complete: () => {
        console.log('请求完成');
        if (this.data.isLoading) {
          wx.hideLoading();
          this.setData({ 
            isLoading: false,
            debugInfo: {
              ...this.data.debugInfo,
              loadingEndTime: new Date().toISOString()
            }
          });
        }
      }
    });
  },

  handleImageUrl(url) {
    console.log('处理头像URL:', url);
    if (!url) {
      console.log('使用默认头像');
      return config.images.defaultAvatar;
    }
    if (url.startsWith('http://') || url.startsWith('https://')) {
      console.log('使用完整URL:', url);
      return url;
    }
    const processedUrl = `${config.images.baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
    console.log('拼接后的URL:', processedUrl);
    return processedUrl;
  },

  editProfile() {
    wx.navigateTo({
      url: '/pages/profile/profile'
    });
  },

  onCloseEditModal() {
    this.setData({
      showEditModal: false
    });
  },

  onNicknameChange(e) {
    this.setData({
      'tempUserInfo.nickname': e.detail
    });
  },

  onBioChange(e) {
    this.setData({
      'tempUserInfo.bio': e.detail
    });
  },

  onGenderChange(e) {
    this.setData({
      'tempUserInfo.gender': e.detail
    });
  },

  onOccupationChange(e) {
    this.setData({
      'tempUserInfo.occupation': e.detail
    });
  },

  showDatePicker() {
    this.setData({
      showDatePicker: true
    });
  },

  onCloseDatePicker() {
    this.setData({
      showDatePicker: false
    });
  },

  onSelectDate(e) {
    const date = new Date(e.detail);
    const birthday = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    this.setData({
      'tempUserInfo.birthday': birthday,
      showDatePicker: false
    });
  },

  showLocationPicker() {
    this.setData({
      showLocationPicker: true
    });
  },

  onCloseLocationPicker() {
    this.setData({
      showLocationPicker: false
    });
  },

  onSelectLocation(e) {
    const { values } = e.detail;
    const location = values.map(item => item.name).join(' ');
    this.setData({
      'tempUserInfo.location': location,
      'tempUserInfo.locationCode': values[values.length - 1].code,
      showLocationPicker: false
    });
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail || e;
    
    wx.uploadFile({
      url: `${config.baseUrl}/upload/avatar`,
      filePath: avatarUrl,
      name: 'file',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      success: (res) => {
        const data = JSON.parse(res.data);
        if (data.url) {
          this.setData({
            'userInfo.avatar': data.url,
            avatarUrl: this.handleImageUrl(data.url)
          });
          wx.showToast({
            title: '头像更新成功',
            icon: 'success'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '上传失败',
          icon: 'error'
        });
      }
    });
  },

  saveProfile() {
    const token = wx.getStorageSync('token');
    wx.request({
      url: `${config.baseUrl}/users/profile`,
      method: 'PUT',
      header: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      },
      data: this.data.tempUserInfo,
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({
            userInfo: this.data.tempUserInfo,
            showEditModal: false
          });
          wx.showToast({
            title: '保存成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: '保存失败',
            icon: 'error'
          });
        }
      },
      fail: () => {
        wx.showToast({
          title: '保存失败',
          icon: 'error'
        });
      }
    });
  },

  navigateToVisitors() {
    wx.navigateTo({
      url: '/pages/visitors/visitors'
    });
  },

  navigateToLikes() {
    wx.navigateTo({
      url: '/pages/likes/likes'
    });
  },

  navigateToMatches() {
    wx.navigateTo({
      url: '/pages/matches/matches'
    });
  },

  navigateToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    });
  },

  viewVip() {
    wx.navigateTo({
      url: '/pages/vip/vip'
    });
  },

  showAbout() {
    wx.showModal({
      title: '关于我们',
      content: '恋爱魔方 v' + config.version + '\n让每个人都能找到心仪的另一半',
      showCancel: false
    });
  },

  logout() {
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          wx.clearStorageSync();
          wx.reLaunch({
            url: '/pages/login/login'
          });
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

  // 显示关于我们
  showAboutDialog() {
    Dialog.alert({
      title: '关于我们',
      message: '心愿魔方致力于为用户提供优质的社交服务。\n客服电话：400-123-4567\n版本号：1.0.0',
    });
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = getToken();
    console.log('当前token:', token);
    
    if (token) {
      this.loadUserInfo();
    } else {
      console.log('未检测到token');
      this.setData({
        userInfo: null,
        avatarUrl: ''
      });
    }
  },

  // 跳转到登录页
  goToLogin() {
    wx.redirectTo({
      url: '/pages/login/login'
    });
  },

  // 预览照片
  previewPhoto(e) {
    const { index } = e.currentTarget.dataset;
    const { photos } = this.data.userInfo;
    
    wx.previewImage({
      current: photos[index],
      urls: photos
    });
  }
});
