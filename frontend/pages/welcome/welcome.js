Page({
  /**
   * 页面的初始数据
   */
  data: {
    second: 6,
    timer: null // 用于存储计时器 ID
  },

  doJump() {
    // **清除计时器，避免二次跳转**
    clearInterval(this.data.timer);

    // **点击跳转**
    wx.switchTab({
      url: "/pages/index/index",
    });
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    console.log("欢迎页面加载成功");

    // 启动倒计时
    let timer = setInterval(() => {
      let currentSecond = this.data.second;

      if (currentSecond <= 1) { // 倒计时结束
        clearInterval(timer); // **清除计时器**
        wx.reLaunch({
          url: "/pages/index/index"
        });
      } else {
        this.setData({
          second: currentSecond - 1
        });
      }
    }, 1000); // 每秒减少 1

    // **存储计时器 ID，以便后续清除**
    this.setData({ timer });
  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {
    clearInterval(this.data.timer); // **确保在页面卸载时清除计时器**
  }
});
