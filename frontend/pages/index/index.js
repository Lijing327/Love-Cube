import config from "../../utils/config";
import areaList from "../../utils/area";

// pages/index/index.js
Page({
  data: {
    // 添加默认轮播图
    banners: [
      {
        id: 1,
        imageUrl: '/images/心愿魔方.jpg',
        title: '欢迎使用心愿魔方'
      },
      {
        id: 2,
        imageUrl: '/images/广告图.jpg', 
        title: '填写资料开始匹配'
      }
    ],
    navItems: [
      { id: 1, type: 'square', icon: '/images/nav/square.png', text: '兴趣广场' },
      { id: 2, type: 'match', icon: '/images/nav/match.png', text: '速配' },
      { id: 3, type: 'date', icon: '/images/nav/date.png', text: '约会' },
      { id: 4, type: 'more', icon: '/images/nav/more.png', text: '更多' }
    ],
    // 添加虚拟推荐用户数据
    recommends: [
      {
        id: 'demo_1',
        userId: 'demo_1',
        avatar: '/images/默认头像.jpg',
        nickname: '小王',
        age: 25,
        gender: '女',
        distance: '1.2km',
        tag: '旅游爱好者'
      },
      {
        id: 'demo_2', 
        userId: 'demo_2',
        avatar: '/images/默认头像.jpg',
        nickname: '小李',
        age: 28,
        gender: '男',
        distance: '2.5km',
        tag: '音乐达人'
      },
      {
        id: 'demo_3',
        userId: 'demo_3', 
        avatar: '/images/默认头像.jpg',
        nickname: '小张',
        age: 26,
        gender: '女',
        distance: '3.1km',
        tag: '美食爱好者'
      }
    ],
    // 添加虚拟新人数据
    newcomers: [
      {
        id: 'newcomer_1',
        userId: 'newcomer_1',
        avatar: '/images/默认头像.jpg',
        nickname: '小陈',
        age: 24,
        gender: '男',
        city: '广州',
        distance: '1.8km'
      },
      {
        id: 'newcomer_2',
        userId: 'newcomer_2', 
        avatar: '/images/默认头像.jpg',
        nickname: '小刘',
        age: 27,
        gender: '女',
        city: '深圳',
        distance: '2.3km'
      }
    ],
    isLoggedIn: false,
    userInfo: {},
    showFilterPopup: false,
    showRegionPicker: false,
    areaList,
    ageRange: [18, 35],
    gender: '',
    selectedRegion: '',
    selectedRegionCode: '',
    filterTags: [
      { id: 1, text: '有房', selected: false },
      { id: 2, text: '有车', selected: false },
      { id: 3, text: '年收入20w+', selected: false },
      { id: 4, text: '名校毕业', selected: false },
      { id: 5, text: '海外经历', selected: false },
      { id: 6, text: '单身', selected: false }
    ]
  },

  onLoad() {
    this.checkLoginStatus();
    this.loadBanners();
    this.loadRecommends();
    this.loadNewcomers();
  },

  onShow() {
    this.checkLoginStatus();
  },

  // 检查登录状态
  checkLoginStatus() {
    const app = getApp();
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    this.setData({
      isLoggedIn: !!(token && app.globalData.isLoggedIn),
      userInfo: userInfo || {}
    });
  },

  // 跳转到登录页面
  goToLogin() {
    wx.navigateTo({
      url: '/pages/login/login'
    });
  },

  // 跳转到个人页面
  goToPersonal() {
    wx.switchTab({
      url: '/pages/personal/personal'
    });
  },

  onPullDownRefresh() {
    this.loadRecommends();
    this.loadNewcomers();
    wx.stopPullDownRefresh();
  },

  // 加载轮播图
  loadBanners() {
    wx.request({
      url: config.baseUrl + '/banners',
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.length > 0) {
          this.setData({
            banners: res.data
          });
        }
        // 如果后端没有数据，保持默认数据不变
      },
      fail: (err) => {
        console.log('加载轮播图失败，使用默认数据:', err);
        // 保持默认数据，不做任何操作
      }
    });
  },

  // 加载推荐用户
  loadRecommends() {
    const app = getApp();
    const token = wx.getStorageSync("token");
    
    // 构建请求header
    const header = {};
    if (token && app.globalData.isLoggedIn) {
      header.Authorization = "Bearer " + token;
    }
    
    wx.request({
      url: config.baseUrl + '/recommends',
      header: header,
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.length > 0) {
          console.log('Recommends raw data:', res.data);
          // 查看第一个用户的完整数据结构
          if (res.data.length > 0) {
            console.log('第一个推荐用户的完整数据:', res.data[0]);
            console.log('第一个推荐用户的所有字段:', Object.keys(res.data[0]));
          }
          const formattedData = res.data.map(user => ({
            id: user.userid,
            userId: user.userid,
            avatar: this.getDisplayAvatar(user),
            nickname: user.username || '未设置昵称',
            age: user.age || '未知',
            gender: user.gender || '未知',
            distance: user.distance ? `${user.distance}km` : '',
            tag: user.tag || user.occupation || '暂无标签'
          }));
          console.log('Recommends formatted data:', formattedData);
          this.setData({
            recommends: formattedData
          });
        }
        // 如果没有数据，保持默认虚拟数据
      },
      fail: (err) => {
        console.log('加载推荐用户失败，使用默认数据:', err);
        // 保持默认虚拟数据，不做任何操作
      }
    });
  },

  // 加载新人推荐
  loadNewcomers() {
    const app = getApp();
    const token = wx.getStorageSync("token");
    
    // 构建请求header
    const header = {};
    if (token && app.globalData.isLoggedIn) {
      header.Authorization = "Bearer " + token;
    }
    
    wx.request({
      url: config.baseUrl + '/newcomers',
      header: header,
      success: (res) => {
        if (res.statusCode === 200 && res.data && res.data.length > 0) {
          console.log('Newcomers raw data:', res.data);
          // 查看第一个用户的完整数据结构
          if (res.data.length > 0) {
            console.log('第一个新人用户的完整数据:', res.data[0]);
            console.log('第一个新人用户的所有字段:', Object.keys(res.data[0]));
          }
          const formattedData = res.data.map(user => ({
            id: user.userid,
            userId: user.userid,
            avatar: this.getDisplayAvatar(user),
            nickname: user.username || '未设置昵称',
            age: user.age || '未知',
            gender: user.gender || '未知',
            city: user.city || user.location || '未知',
            distance: user.distance ? `${user.distance}km` : ''
          }));
          console.log('Newcomers formatted data:', formattedData);
          this.setData({
            newcomers: formattedData
          });
        }
        // 如果没有数据，保持默认虚拟数据
      },
      fail: (err) => {
        console.log('加载新人推荐失败，使用默认数据:', err);
        // 保持默认虚拟数据，不做任何操作
      }
    });
  },

  // 获取显示头像（优先使用生活照片）
  getDisplayAvatar(user) {
    console.log('处理用户头像:', user.userid, user);
    let avatarUrl = '';
    
    // 优先使用生活照片 - 检查多个可能的字段
    if (user.photos && user.photos.length > 0) {
      avatarUrl = user.photos[0];
      console.log('使用photos字段的生活照片:', avatarUrl);
    } else if (user.lifePhotos && user.lifePhotos.length > 0) {
      avatarUrl = user.lifePhotos[0];
      console.log('使用lifePhotos字段的生活照片:', avatarUrl);
    } else if (user.images && user.images.length > 0) {
      avatarUrl = user.images[0];
      console.log('使用images字段的生活照片:', avatarUrl);
    } else if (user.gallery && user.gallery.length > 0) {
      avatarUrl = user.gallery[0];
      console.log('使用gallery字段的生活照片:', avatarUrl);
    } else if (user.profilePhoto) {
      avatarUrl = user.profilePhoto;
      console.log('使用profilePhoto:', avatarUrl);
    } else if (user.avatar) {
      avatarUrl = user.avatar;
      console.log('使用avatar:', avatarUrl);
    } else {
      console.log('使用默认头像');
      return '/images/default-avatar.svg';
    }
    
    // 处理URL
    const finalUrl = this.handleImageUrl(avatarUrl);
    console.log('最终头像URL:', finalUrl);
    return finalUrl;
  },

  // 处理图片URL
  handleImageUrl(url) {
    if (!url) return '/images/default-avatar.svg';
    if (url.startsWith('http://') || url.startsWith('https://')) {
      return url;
    }
    return `${config.images.baseUrl}${url.startsWith('/') ? '' : '/'}${url}`;
  },

  // 点击搜索框
  onTapSearch() {
    wx.navigateTo({
      url: '/pages/search/search'
    });
  },

  // 显示筛选
  showFilter() {
    this.setData({
      showFilterPopup: true
    });
  },

  // 关闭筛选
  onCloseFilter() {
    this.setData({
      showFilterPopup: false
    });
  },

  // 重置筛选
  resetFilter() {
    this.setData({
      ageRange: [18, 35],
      gender: '',
      selectedRegion: '',
      selectedRegionCode: '',
      filterTags: this.data.filterTags.map(tag => ({
        ...tag,
        selected: false
      }))
    });
  },

  // 年龄范围改变
  onAgeChange(e) {
    this.setData({
      ageRange: e.detail
    });
  },

  // 性别选择改变
  onGenderChange(e) {
    this.setData({
      gender: e.detail
    });
  },

  // 显示地区选择器
  showRegionPicker() {
    this.setData({
      showRegionPicker: true
    });
  },

  // 关闭地区选择器
  closeRegionPicker() {
    this.setData({
      showRegionPicker: false
    });
  },

  // 确认地区选择
  onRegionConfirm(e) {
    const { values } = e.detail;
    this.setData({
      selectedRegion: values.map(item => item.name).join(''),
      selectedRegionCode: values[values.length - 1].code,
      showRegionPicker: false
    });
  },

  // 切换标签选择
  toggleTag(e) {
    const { id } = e.currentTarget.dataset;
    const tags = this.data.filterTags.map(tag => {
      if (tag.id === id) {
        return { ...tag, selected: !tag.selected };
      }
      return tag;
    });
    this.setData({ filterTags: tags });
  },

  // 应用筛选条件
  applyFilter() {
    const filters = {
      ageRange: this.data.ageRange,
      gender: this.data.gender,
      region: this.data.selectedRegionCode,
      tags: this.data.filterTags
        .filter(tag => tag.selected)
        .map(tag => tag.text)
    };

    wx.request({
      url: config.baseUrl + '/filter',
      method: 'POST',
      data: filters,
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const formattedData = res.data.map(user => ({
            id: user.userid,
            userId: user.userid,
            avatar: this.getDisplayAvatar(user),
            nickname: user.username || '未设置昵称',
            age: user.age || '未知',
            tag: user.tag || user.occupation || '暂无标签'
          }));
          
          this.setData({
            recommends: formattedData,
            showFilterPopup: false
          });
        }
      }
    });
  },

  // 刷新推荐
  refreshRecommends() {
    this.loadRecommends();
  },

  // 查看用户资料
  async viewProfile(e) {
    const userId = e.currentTarget.dataset.id;
    console.log('View profile event:', e);
    console.log('Current target:', e.currentTarget);
    console.log('Dataset:', e.currentTarget.dataset);
    console.log('Viewing profile for user:', userId);
    
    if (!userId) {
      console.error('No userId provided for navigation');
      wx.showToast({
        title: '用户ID不存在',
        icon: 'none'
      });
      return;
    }
    
    // 检查是否为虚拟用户
    if (userId.startsWith('demo_') || userId.startsWith('newcomer_')) {
      wx.showModal({
        title: '温馨提示',
        content: '完善个人资料后即可查看更多真实用户信息',
        confirmText: '去完善',
        cancelText: '稍后再说',
        success: (res) => {
          if (res.confirm) {
            wx.switchTab({
              url: '/pages/personal/personal'
            });
          }
        }
      });
      return;
    }
    
    const app = getApp();
    
    // 检查是否需要登录
    if (app.checkLoginRequired()) {
      const shouldLogin = await app.promptLogin("查看用户详细资料");
      if (!shouldLogin) {
        return; // 用户选择不登录
      }
      return; // 跳转到登录页面了
    }
    
    wx.navigateTo({
      url: `/pages/user-profile/user-profile?id=${userId}`,
      fail: function(err) {
        console.error('Navigation failed:', err);
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  },

  // 导航点击
  async onNavTap(e) {
    const type = e.currentTarget.dataset.type;
    const app = getApp();
    
    switch(type) {
      case 'square':
        wx.switchTab({ url: '/pages/dynamic/dynamic' }); // 跳转到动态页面
        break;
      case 'match':
        // 匹配功能需要登录
        if (app.checkLoginRequired()) {
          const shouldLogin = await app.promptLogin("使用匹配功能");
          if (!shouldLogin) return;
          return;
        }
        wx.switchTab({ url: '/pages/match/match' });
        break;
      case 'date':
        // 约会功能需要登录
        if (app.checkLoginRequired()) {
          const shouldLogin = await app.promptLogin("使用约会功能");
          if (!shouldLogin) return;
          return;
        }
        wx.showToast({ title: '约会功能即将上线', icon: 'none' });
        break;
      case 'more':
        wx.showActionSheet({
          itemList: ['搜索用户', '设置', '帮助'],
          success: (res) => {
            switch(res.tapIndex) {
              case 0:
                wx.navigateTo({ url: '/pages/search/search' });
                break;
              case 1:
                wx.navigateTo({ url: '/pages/settings/settings' });
                break;
              case 2:
                wx.showToast({ title: '帮助功能即将上线', icon: 'none' });
                break;
            }
          }
        });
        break;
    }
  },

  // 查看更多新人
  viewMoreNewcomers() {
    wx.navigateTo({
      url: '/pages/newcomers/newcomers'
    });
  },

  // 跳转到注册/个人资料页面
  goToRegister() {
    const app = getApp();
    if (!app.globalData.isLoggedIn) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
    } else {
      wx.switchTab({
        url: '/pages/personal/personal'
      });
    }
  },

  // 跳转到匹配页面
  goToMatch() {
    wx.switchTab({
      url: '/pages/match/match'
    });
  },


});