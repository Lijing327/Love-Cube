import config from "./pages/utils/config";

App({
  globalData: {
    skipLogin: false // 设置为 true 表示跳过登录，false 表示需要登录
  },
  onLaunch() {
    console.log("🚀 小程序启动...");

    if (this.globalData.skipLogin) {
      wx.navigateTo({
        url: "/pages/welcome/welcome",
        success: () => console.log("✅ 直接跳转到首页成功！"),
        fail: (err) => console.error("❌ 跳转到首页失败：", err)
      });
    } else {
      // 执行正常的登录逻辑
      const token = wx.getStorageSync("token") || "";
      const userId = wx.getStorageSync("userId") || "";

      console.log("🔍 获取到的 token:", token ? token : "❌ 没有 token");
      console.log("🔍 获取到的 userId:", userId ? userId : "❌ 没有 userId");

      if (!token || !userId) {
        console.warn("⚠️ 没有 token 或 userId，跳转到登录页");
        wx.redirectTo({ url: "/pages/login/login" });
      } else {
        console.log("✅ token 和 userId 存在，尝试验证用户状态...");

        wx.request({
          url: `${config.baseUrl}/users/checkUserStatus`,
          method: "GET",
          header: { Authorization: token ? `Bearer ${token}` : "" }, // ✅ 避免 `Bearer null`
          success(res) {
            console.log("🔄 服务器返回:", res.data);

            if (res.statusCode === 401) {
              console.error("❌ token 失效，跳转到登录页");
              wx.removeStorageSync("token");
              wx.redirectTo({ url: "/pages/login/login" });
            } else if (res.data.registered) {
              console.log("✅ 用户已注册，存储 userInfo 并跳转首页");

              wx.setStorageSync("userInfo", res.data.userInfo);
              wx.redirectTo({ url: "/pages/welcome/welcome" });
            } else {
              console.warn("⚠️ 用户未注册，跳转到注册页");
              wx.redirectTo({ url: "/pages/register/register" });
            }
          },
          fail(err) {
            console.error("❌ 请求失败:", err);
            wx.redirectTo({ url: "/pages/login/login" });
          }
        });
      }
    }
  }
});
