import config from "../../utils/config";
import areaList from "../../utils/area";

// pages/index/index.js
Page({
  data: {
    banners: [],
    navItems: [
      { id: 1, type: 'square', icon: '/images/nav/square.png', text: '广场' },
      { id: 2, type: 'match', icon: '/images/nav/match.png', text: '速配' },
      { id: 3, type: 'date', icon: '/images/nav/date.png', text: '约会' },
      { id: 4, type: 'more', icon: '/images/nav/more.png', text: '更多' }
    ],
    recommends: [],
    newcomers: [],
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
    this.loadBanners();
    this.loadRecommends();
    this.loadNewcomers();
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
        if (res.statusCode === 200) {
          this.setData({
            banners: res.data
          });
        }
      }
    });
  },

  // 加载推荐用户
  loadRecommends() {
    wx.request({
      url: config.baseUrl + '/recommends',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('Recommends raw data:', res.data);
          const formattedData = res.data.map(user => ({
            id: user.userid,
            userId: user.userid,
            avatar: user.photos && user.photos.length > 0 ? user.photos[0] : (user.profilePhoto || user.avatar || '/images/default-avatar.png'),
            nickname: user.username || '未设置昵称',
            age: user.age || '未知',
            tag: user.tag || user.occupation || '暂无标签'
          }));
          console.log('Recommends formatted data:', formattedData);
          this.setData({
            recommends: formattedData
          });
        }
      }
    });
  },

  // 加载新人推荐
  loadNewcomers() {
    wx.request({
      url: config.baseUrl + '/newcomers',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('Newcomers raw data:', res.data);
          const formattedData = res.data.map(user => ({
            id: user.userid,
            userId: user.userid,
            avatar: user.photos && user.photos.length > 0 ? user.photos[0] : (user.profilePhoto || user.avatar || '/images/default-avatar.png'),
            nickname: user.username || '未设置昵称',
            age: user.age || '未知',
            city: user.city || user.location || '未知'
          }));
          console.log('Newcomers formatted data:', formattedData);
          this.setData({
            newcomers: formattedData
          });
        }
      }
    });
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
            avatar: user.profilePhoto || user.avatar || '/images/default-avatar.png',
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
  viewProfile(e) {
    const userId = e.currentTarget.dataset.id;
    console.log('View profile event:', e);
    console.log('Current target:', e.currentTarget);
    console.log('Dataset:', e.currentTarget.dataset);
    console.log('Viewing profile for user:', userId);
    if (userId) {
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
    } else {
      console.error('No userId provided for navigation');
      wx.showToast({
        title: '用户ID不存在',
        icon: 'none'
      });
    }
  },

  // 导航点击
  onNavTap(e) {
    const type = e.currentTarget.dataset.type;
    switch(type) {
      case 'square':
        wx.navigateTo({ url: '/pages/square/square' });
        break;
      case 'match':
        wx.navigateTo({ url: '/pages/match/match' });
        break;
      case 'date':
        wx.navigateTo({ url: '/pages/date/date' });
        break;
      case 'more':
        wx.navigateTo({ url: '/pages/more/more' });
        break;
    }
  },

  // 查看更多新人
  viewMoreNewcomers() {
    wx.navigateTo({
      url: '/pages/newcomers/newcomers'
    });
  }
});