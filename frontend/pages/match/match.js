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
    touchStartY: 0
  },

  onLoad() {
    // 先测试连接
    this.testConnection();
  },

  onShow() {
    // 每次显示页面时刷新匹配列表
    if (this.data.matchList.length === 0) {
      this.loadMatchList();
    }
  },

  // 测试服务连接
  testConnection() {
    wx.request({
      url: `${config.baseUrl}/matches/test`,
      method: 'GET',
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('匹配服务连接正常:', res.data);
          this.loadMatchList();
        } else {
          console.error('服务连接异常:', res);
          wx.showToast({
            title: '服务暂时不可用',
            icon: 'none',
            duration: 2000
          });
        }
      },
      fail: (err) => {
        console.error('服务连接失败:', err);
        wx.showToast({
          title: '无法连接到服务器',
          icon: 'none',
          duration: 2000
        });
      }
    });
  },

  // 加载匹配列表
  loadMatchList() {
    const userId = wx.getStorageSync('userId');
    if (!userId) {
      wx.showToast({
        title: '请先登录',
        icon: 'none'
      });
      return;
    }

    // 显示加载中
    const loadingTimer = setTimeout(() => {
      wx.showLoading({
        title: '加载中...',
        mask: true
      });
    }, 300);

    wx.request({
      url: `${config.baseUrl}/matches/list`,
      method: 'GET',
      data: {
        userId: userId,
        minAge: this.data.ageRange[0],
        maxAge: this.data.ageRange[1],
        gender: this.data.gender || null,
        location: this.data.selectedRegion || null
      },
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      complete: () => {
        // 清除定时器并隐藏loading
        clearTimeout(loadingTimer);
        wx.hideLoading();
      },
      success: (res) => {
        console.log('匹配列表响应:', res);  // 添加日志
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
          title: '网络错误',
          icon: 'none',
          duration: 2000
        });
      }
    });
  },

  // 触摸开始
  onTouchStart(e) {
    const touch = e.touches[0];
    this.setData({
      touchStartX: touch.clientX,
      touchStartY: touch.clientY
    });
  },

  // 触摸移动
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

  // 触摸结束
  onTouchEnd(e) {
    const moveX = this.data.x;
    if (Math.abs(moveX) > 100) {
      // 滑动距离足够大，触发喜欢/不喜欢
      if (moveX > 0) {
        this.onLike();
      } else {
        this.onDislike();
      }
    } else {
      // 滑动距离不够，回到原位
      this.setData({
        x: 0,
        rotate: 0
      });
    }
  },

  // 不喜欢
  onDislike() {
    const { currentIndex, matchList } = this.data;
    if (currentIndex >= matchList.length) return;

    const userId = matchList[currentIndex].id;
    wx.request({
      url: config.baseUrl + `/match/${userId}/dislike`,
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

  // 喜欢
  onLike() {
    const { currentIndex, matchList } = this.data;
    if (currentIndex >= matchList.length) return;

    const userId = matchList[currentIndex].id;
    wx.request({
      url: config.baseUrl + `/match/${userId}/like`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.isMatch) {
            // 匹配成功
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

  // 超级喜欢
  onSuperLike() {
    const { currentIndex, matchList } = this.data;
    if (currentIndex >= matchList.length) return;

    const userId = matchList[currentIndex].id;
    wx.request({
      url: config.baseUrl + `/match/${userId}/superlike`,
      method: 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          if (res.data.isMatch) {
            // 匹配成功
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

  // 切换到下一张卡片
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

  // 年龄范围改变
  onAgeChange(e) {
    this.setData({
      ageRange: e.detail
    });
  },

  // 距离范围改变
  onDistanceChange(e) {
    this.setData({
      distanceRange: e.detail
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
      distance: this.data.distanceRange,
      gender: this.data.gender,
      region: this.data.selectedRegionCode,
      tags: this.data.filterTags
        .filter(tag => tag.selected)
        .map(tag => tag.text)
    };

    wx.request({
      url: config.baseUrl + '/match/filter',
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

  // 关闭匹配成功弹窗
  closeMatchSuccess() {
    this.setData({
      showMatchSuccess: false
    });
  },

  // 进入聊天
  goToChat() {
    const userId = this.data.matchedUserId;
    this.setData({
      showMatchSuccess: false
    });
    wx.navigateTo({
      url: `/pages/chat/chat?id=${userId}&type=single`
    });
  },

  // 处理图片加载错误
  handleImageError(e) {
    const type = e.currentTarget.dataset.type;
    const index = e.currentTarget.dataset.index;
    
    console.warn('图片加载失败:', type, e);

    switch(type) {
      case 'user-avatar':
        // 更新特定用户的头像为默认图片
        if (typeof index !== 'undefined') {
          const key = `matchList[${index}].profilePhoto`;
          this.setData({
            [key]: '/images/default-avatar.png'
          });
        }
        break;
      
      case 'success-image':
        // 使用本地备用成功图片
        this.setData({
          'successImageSrc': '/images/match-success-fallback.png'
        });
        break;
      
      case 'empty-image':
        // 使用本地备用空状态图片
        this.setData({
          'emptyImageSrc': '/images/empty-fallback.png'
        });
        break;
    }
  },
});
