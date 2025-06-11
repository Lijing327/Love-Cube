import config from "../../utils/config";
import areaList from "../../utils/area";

Page({
  data: {
    currentIndex: 0,
    x: 0,
    rotate: 0,
    matchList: [],
    showFilterPopup: false,
    showRegionPicker: false,
    showMatchSuccess: false,
    showEmpty: false,
    areaList,
    ageRange: [18, 35],
    distanceRange: 50,
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
    ],
    matchedUserId: null,
    touchStartX: 0,
    touchStartY: 0,
    navBarHeight: 0,
    menuButtonHeight: 0,
    menuButtonTop: 0,
    statusBarHeight: 0
  },

  onLoad() {
    const systemInfo = wx.getSystemInfoSync();
    const menuButtonInfo = wx.getMenuButtonBoundingClientRect();
    
    this.setData({
      statusBarHeight: systemInfo.statusBarHeight,
      navBarHeight: (menuButtonInfo.top - systemInfo.statusBarHeight) * 2 + menuButtonInfo.height,
      menuButtonHeight: menuButtonInfo.height,
      menuButtonTop: menuButtonInfo.top
    });

    this.loadMatchList();
  },

  onShow() {
    if (this.data.matchList.length === 0) {
      this.loadMatchList();
    }
  },

  loadMatchList() {
    const userId = parseInt(wx.getStorageSync('userId'));
    if (!userId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({
      title: '加载中...',
      mask: true
    });

    const rawParams = {
      userId: userId,
      minAge: this.data.ageRange[0],
      maxAge: this.data.ageRange[1],
      gender: this.data.gender ? parseInt(this.data.gender) : undefined,
      location: this.data.selectedRegion || undefined
    };
    
    const params = {};
    Object.keys(rawParams).forEach(key => {
      if (rawParams[key] !== undefined) {
        params[key] = rawParams[key];
      }
    });

    wx.request({
      url: `${config.baseUrl}/matches/list`,
      method: 'GET',
      data: params,
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        console.log('匹配列表响应:', res);
        if (res.statusCode === 200 && res.data.success) {
          const matchData = res.data.data || [];
          this.setData({
            matchList: matchData,
            showEmpty: matchData.length === 0,
            currentIndex: 0,
            x: 0,
            rotate: 0
          });

          if (matchData.length === 0) {
            wx.showToast({
              title: '暂无匹配',
              icon: 'none'
            });
          }
        } else {
          console.error('获取匹配列表失败:', res);
          if (res.statusCode === 401) {
            wx.removeStorageSync('token');
            wx.showToast({
              title: '登录已过期，请重新登录',
              icon: 'none',
              duration: 2000
            });
            wx.navigateTo({
              url: '/pages/login/login'
            });
            return;
          }
          wx.showToast({
            title: res.data?.message || '获取匹配列表失败',
            icon: 'none',
            duration: 2000
          });
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        wx.showToast({
          title: '网络错误，请检查网络连接',
          icon: 'none',
          duration: 2000
        });
      },
      complete: () => {
        wx.hideLoading();
      }
    });
  },

  onTouchStart(e) {
    const touch = e.touches[0];
    this.setData({
      touchStartX: touch.clientX,
      touchStartY: touch.clientY
    });
  },

  onTouchMove(e) {
    const touch = e.touches[0];
    const moveX = touch.clientX - this.data.touchStartX;
    const moveY = touch.clientY - this.data.touchStartY;
    const rotate = moveX * 0.1;

    this.setData({
      x: moveX,
      rotate: rotate
    });
  },

  onTouchEnd(e) {
    const moveX = this.data.x;
    if (Math.abs(moveX) > 100) {
      if (moveX > 0) {
        this.onLike();
      } else {
        this.onDislike();
      }
    } else {
      this.setData({
        x: 0,
        rotate: 0
      });
    }
  },

  onDislike() {
    const { currentIndex, matchList } = this.data;
    if (currentIndex >= matchList.length) return;

    const userId = matchList[currentIndex].id;
    wx.request({
      url: config.baseUrl + `/matches/${userId}/dislike`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          this.nextCard('left');
        }
      }
    });
  },

  onLike() {
    const { currentIndex, matchList } = this.data;
    if (currentIndex >= matchList.length) return;

    const userId = matchList[currentIndex].id;
    wx.request({
      url: config.baseUrl + `/matches/${userId}/like`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.isMatch) {
            this.setData({
              showMatchSuccess: true,
              matchedUserId: userId
            });
          }
          this.nextCard('right');
        }
      }
    });
  },

  onSuperLike() {
    const { currentIndex, matchList } = this.data;
    if (currentIndex >= matchList.length) return;

    const userId = matchList[currentIndex].id;
    wx.request({
      url: config.baseUrl + `/matches/${userId}/superlike`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.isMatch) {
            this.setData({
              showMatchSuccess: true,
              matchedUserId: userId
            });
          }
          this.nextCard('up');
        }
      }
    });
  },

  nextCard(direction) {
    const animation = wx.createAnimation({
      duration: 300,
      timingFunction: 'ease-out'
    });

    let translateX = 0;
    let translateY = 0;
    let rotate = 0;

    switch (direction) {
      case 'left':
        translateX = -1000;
        rotate = -30;
        break;
      case 'right':
        translateX = 1000;
        rotate = 30;
        break;
      case 'up':
        translateY = -1000;
        break;
    }

    animation.translateX(translateX).translateY(translateY).rotate(rotate).step();

    this.setData({
      animation: animation.export()
    });

    setTimeout(() => {
      const { currentIndex, matchList } = this.data;
      if (currentIndex < matchList.length - 1) {
        this.setData({
          currentIndex: currentIndex + 1,
          x: 0,
          rotate: 0
        });
      } else {
        this.setData({
          showEmpty: true
        });
      }
    }, 300);
  },

  showFilter() {
    this.setData({
      showFilterPopup: true
    });
  },

  onCloseFilter() {
    this.setData({
      showFilterPopup: false
    });
  },

  resetFilter() {
    this.setData({
      ageRange: [18, 35],
      distanceRange: 50,
      gender: '',
      selectedRegion: '',
      selectedRegionCode: '',
      filterTags: this.data.filterTags.map(tag => ({
        ...tag,
        selected: false
      }))
    });
  },

  onAgeChange(e) {
    this.setData({
      ageRange: e.detail
    });
  },

  onDistanceChange(e) {
    this.setData({
      distanceRange: e.detail
    });
  },

  onGenderChange(e) {
    this.setData({
      gender: e.detail
    });
  },

  showRegionPicker() {
    this.setData({
      showRegionPicker: true
    });
  },

  closeRegionPicker() {
    this.setData({
      showRegionPicker: false
    });
  },

  onRegionConfirm(e) {
    const { values } = e.detail;
    this.setData({
      selectedRegion: values.map(item => item.name).join(''),
      selectedRegionCode: values[values.length - 1].code,
      showRegionPicker: false
    });
  },

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

  applyFilter() {
    const filters = {
      ageRange: this.data.ageRange,
      distance: this.data.distanceRange,
      gender: this.data.gender,
      region: this.data.selectedRegionCode,
      tags: this.data.filterTags
        .filter(tag => tag.selected)
        .map(tag => tag.text)
    };

    wx.request({
      url: config.baseUrl + '/matches/filter',
      method: 'POST',
      data: filters,
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({
            matchList: res.data,
            showEmpty: res.data.length === 0,
            currentIndex: 0,
            showFilterPopup: false
          });
        }
      }
    });
  },

  closeMatchSuccess() {
    this.setData({
      showMatchSuccess: false
    });
  },

  goToChat() {
    const userId = this.data.matchedUserId;
    this.setData({
      showMatchSuccess: false
    });
    wx.navigateTo({
      url: `/pages/chat/chat?id=${userId}&type=single`
    });
  },

  handleImageError(e) {
    const type = e.currentTarget.dataset.type;
    const index = e.currentTarget.dataset.index;
    
    console.warn('图片加载失败:', type, e);

    switch(type) {
      case 'user-avatar':
        if (typeof index !== 'undefined') {
          const key = `matchList[${index}].profilePhoto`;
          this.setData({
            [key]: '/images/default-avatar.png'
          });
        }
        break;
      
      case 'success-image':
        this.setData({
          'successImageSrc': '/images/match-success-fallback.png'
        });
        break;
      
      case 'empty-image':
        this.setData({
          'emptyImageSrc': '/images/empty-fallback.png'
        });
        break;
    }
  },
});
