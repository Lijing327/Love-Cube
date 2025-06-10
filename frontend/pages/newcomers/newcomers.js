// pages/newcomers/newcomers.js
import config from "../../utils/config";

Page({

  /**
   * 页面的初始数据
   */
  data: {
    users: [],
    page: 1,
    pageSize: 10,
    hasMore: true,
    loading: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadNewcomers();
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.setData({
      page: 1,
      hasMore: true
    }, () => {
      this.loadNewcomers();
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  // 返回上一页
  onBack() {
    wx.navigateBack();
  },

  // 加载新人列表
  loadNewcomers(isLoadMore = false) {
    if (this.data.loading || (!isLoadMore && !this.data.hasMore)) return;

    this.setData({ loading: true });

    wx.request({
      url: `${config.baseUrl}/users/newcomers`,
      method: 'GET',
      data: {
        page: this.data.page,
        size: this.data.pageSize
      },
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const newUsers = res.data.map(user => ({
            id: user.userId,
            avatar: user.profilePhoto || '/images/default-avatar.png',
            nickname: user.nickname || '未设置昵称',
            age: user.age || '未知',
            location: user.location || '未设置',
            tags: user.tags || []
          }));

          this.setData({
            users: isLoadMore ? [...this.data.users, ...newUsers] : newUsers,
            hasMore: newUsers.length === this.data.pageSize,
            page: isLoadMore ? this.data.page + 1 : 1
          });
        } else {
          wx.showToast({
            title: '获取新人列表失败',
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
        this.setData({ loading: false });
      }
    });
  },

  // 加载更多
  loadMore() {
    this.loadNewcomers(true);
  },

  // 查看用户资料
  viewProfile(e) {
    const userId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/user-profile/user-profile?id=${userId}`
    });
  }
})