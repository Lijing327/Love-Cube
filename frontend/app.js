import config from "./utils/config";

App({
  globalData: {
    skipLogin: false,
    userInfo: null,
    isLoggedIn: false
  },
  onLaunch() {
    console.log("🚀 小程序启动...");
    this.checkLoginStatus();
  },

  checkLoginStatus() {
    const token = wx.getStorageSync("token");
    const userId = wx.getStorageSync("userId");
    const userInfo = wx.getStorageSync("userInfo");

    console.log("🔍 检查登录状态...");
    console.log("Token:", token ? "存在" : "不存在");
    console.log("UserId:", userId ? "存在" : "不存在");

    // 如果没有基本的登录信息，直接跳转到登录页
    if (!token || !userId) {
      console.log("❌ 缺少登录信息，需要重新登录");
      this.redirectToLogin();
      return;
    }

    // 验证服务器端登录状态
    wx.request({
      url: `${config.baseUrl}/users/checkUserStatus`,
      method: "GET",
      header: { 
        Authorization: `Bearer ${token}`
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.success) {
          console.log("✅ 登录状态有效");
          this.globalData.isLoggedIn = true;
          this.globalData.userInfo = res.data.userInfo || userInfo;
          wx.setStorageSync("userInfo", this.globalData.userInfo);
        } else {
          console.log("❌ 登录状态无效，需要重新登录");
          this.redirectToLogin();
        }
      },
      fail: (err) => {
        console.error("❌ 验证登录状态失败:", err);
        // 网络错误时，如果本地有用户信息，暂时认为是登录的
        if (userInfo) {
          console.log("⚠️ 使用本地登录信息");
          this.globalData.isLoggedIn = true;
          this.globalData.userInfo = userInfo;
        } else {
          this.redirectToLogin();
        }
      }
    });
  },

  redirectToLogin() {
    // 清除登录信息
    wx.removeStorageSync("token");
    wx.removeStorageSync("userId");
    wx.removeStorageSync("userInfo");
    this.globalData.isLoggedIn = false;
    this.globalData.userInfo = null;

    // 跳转到登录页
    wx.reLaunch({
      url: "/pages/login/login"
    });
  }
});
