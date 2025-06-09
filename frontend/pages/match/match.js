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
    this.loadMatchList();
  },

  onShow() {
    // 每次显示页面时刷新匹配列表
    if (this.data.matchList.length === 0) {
      this.loadMatchList();
    }
  },

  // 加载匹配列表
  loadMatchList() {
    wx.request({
      url: config.baseUrl + '/match/list',
      method: 'GET',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({
            matchList: res.data,
            showEmpty: res.data.length === 0,
            currentIndex: 0,
            x: 0,
            rotate: 0
          });
        }
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
  }
});
