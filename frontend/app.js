import config from "./utils/config";

App({
  globalData: {
    skipLogin: false,
    userInfo: null,
    isLoggedIn: false,
    allowBrowse: true  // 允许浏览模式
  },
  onLaunch() {
    console.log("🚀 小程序启动...");
    // 不再强制检查登录状态，允许用户先浏览
    this.checkLoginStatusSilently();
  },

  checkLoginStatusSilently() {
    const token = wx.getStorageSync("token");
    const userId = wx.getStorageSync("userId");
    const userInfo = wx.getStorageSync("userInfo");

    console.log("🔍 静默检查登录状态...");
    console.log("Token:", token ? "存在" : "不存在");
    console.log("UserId:", userId ? "存在" : "不存在");

    // 如果没有基本的登录信息，设置为未登录状态，但不强制跳转
    if (!token || !userId) {
      console.log("ℹ️ 用户未登录，允许浏览模式");
      this.globalData.isLoggedIn = false;
      this.globalData.userInfo = null;
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
          console.log("✅ 全局登录状态已设置为已登录");
        } else {
          console.log("❌ 登录状态无效，设置为未登录模式");
          this.globalData.isLoggedIn = false;
          this.globalData.userInfo = null;
          // 清除无效token
          wx.removeStorageSync("token");
          wx.removeStorageSync("userId");
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
          console.log("ℹ️ 网络错误且无本地用户信息，进入浏览模式");
          this.globalData.isLoggedIn = false;
          this.globalData.userInfo = null;
        }
      }
    });
  },

  // 引导用户登录（用于需要登录的功能）
  promptLogin(action = "使用此功能") {
    return new Promise((resolve, reject) => {
      wx.showModal({
        title: '需要登录',
        content: `${action}需要登录，是否立即登录？`,
        confirmText: '去登录',
        cancelText: '稍后再说',
        success: (res) => {
          if (res.confirm) {
            // 保存当前页面路径，登录后返回
            const pages = getCurrentPages();
            const currentPage = pages[pages.length - 1];
            const route = currentPage.route;
            wx.setStorageSync('returnUrl', '/' + route);
            
            wx.navigateTo({
              url: "/pages/login/login",
              success: () => resolve(true),
              fail: () => reject(false)
            });
          } else {
            resolve(false);
          }
        }
      });
    });
  },

  // 检查是否需要登录
  checkLoginRequired() {
    return !this.globalData.isLoggedIn;
  },

  // 刷新登录状态（登录成功后调用）
  refreshLoginStatus() {
    const token = wx.getStorageSync("token");
    const userId = wx.getStorageSync("userId");
    const userInfo = wx.getStorageSync("userInfo");
    
    if (token && userId) {
      this.globalData.isLoggedIn = true;
      this.globalData.userInfo = userInfo;
      console.log("✅ 登录状态已刷新");
    } else {
      this.globalData.isLoggedIn = false;
      this.globalData.userInfo = null;
      console.log("❌ 登录状态已清除");
    }
  },

  // 强制跳转到登录页（仅在必要时使用）
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
