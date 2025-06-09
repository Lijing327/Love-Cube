// pages/search/search.js
import config from '../../utils/config';

Page({

  /**
   * 页面的初始数据
   */
  data: {
    keyword: '',
    searchResults: [],
    page: 0,
    size: 10,
    hasMore: true,
    loading: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

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
      page: 0,
      searchResults: []
    }, () => {
      this.search();
      wx.stopPullDownRefresh();
    });
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.setData({
        page: this.data.page + 1
      }, () => {
        this.search();
      });
    }
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  // 输入关键词
  onInput(e) {
    this.setData({
      keyword: e.detail.value
    });
    if (e.detail.value) {
      this.search();
    } else {
      this.setData({
        searchResults: [],
        page: 0,
        hasMore: true
      });
    }
  },

  // 搜索
  search() {
    if (this.data.loading) return;
    
    this.setData({ loading: true });
    wx.request({
      url: `${config.baseUrl}/search`,
      data: {
        keyword: this.data.keyword,
        page: this.data.page,
        size: this.data.size
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const newResults = this.data.page === 0 
            ? res.data 
            : [...this.data.searchResults, ...res.data];
          
          this.setData({
            searchResults: newResults,
            hasMore: res.data.length === this.data.size
          });
        }
      },
      complete: () => {
        this.setData({ loading: false });
      }
    });
  },

  // 查看用户资料
  viewProfile(e) {
    const userId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: `/pages/profile/profile?id=${userId}`
    });
  }
})