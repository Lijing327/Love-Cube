import config from "../utils/config";
import { setUserId, setToken } from "../utils/auth";
import { getUserId } from "../utils/auth";

Page({
  data: {
    userInfo: null
  },

  onLoad() {
    console.log("🔄 登录页面加载");
    // 清除旧的登录信息
    wx.removeStorageSync('token');
    wx.removeStorageSync('userId');
  },

  login() {
    console.log("🔄 开始登录流程");
    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    wx.login({
      success: (res) => {
        console.log("✅ wx.login 成功，返回的 code:", res.code);

        if (!res.code) {
          console.error("❌ 获取 code 失败！");
          wx.hideLoading();
          wx.showToast({
            title: '登录失败',
            icon: 'error'
          });
          return;
        }

        // 构建完整的登录URL
        const loginUrl = `${config.baseUrl}/wechat/login`;
        console.log("📤 发起登录请求，地址:", loginUrl);

        wx.request({
          url: loginUrl,
          method: 'GET',
          data: { code: res.code },
          success: (res) => {
            console.log('📥 登录响应:', res.data);

            if (res.statusCode === 200 && res.data.userId) {
              // 存储用户信息
              setUserId(res.data.userId);
              console.log("✅ userId 存储成功:", res.data.userId);

              if (res.data.token) {
                setToken(res.data.token);
                console.log("✅ Token 存储成功");

                wx.showToast({
                  title: '登录成功',
                  icon: 'success',
                  duration: 1500
                });

                // 延迟跳转，让用户看到成功提示
                setTimeout(() => {
                  wx.reLaunch({
                    url: "/pages/welcome/welcome",
                    success: () => console.log("✅ 跳转到首页成功！"),
                    fail: (err) => console.error("❌ 跳转失败：", err)
                  });
                }, 1500);
              } else {
                console.error("❌ 服务器未返回 token");
                wx.showToast({
                  title: '登录异常',
                  icon: 'error'
                });
              }
            } else if (res.data.status === "USER_NOT_REGISTERED") {
              console.log("ℹ️ 用户未注册，跳转到注册页");
              wx.setStorageSync("openid", res.data.openid);
              wx.navigateTo({
                url: "/pages/register/register",
                fail: (err) => console.error("❌ 跳转到注册页失败：", err)
              });
            } else {
              console.error("❌ 登录失败:", res.data.message);
              wx.showToast({
                title: res.data.message || '登录失败',
                icon: 'error'
              });
            }
          },
          fail: (err) => {
            console.error("❌ 登录请求失败:", err);
            wx.showToast({
              title: '网络错误',
              icon: 'error'
            });
          },
          complete: () => {
            wx.hideLoading();
          }
        });
      },
      fail: (err) => {
        console.error("❌ wx.login 失败:", err);
        wx.hideLoading();
        wx.showToast({
          title: '登录失败',
          icon: 'error'
        });
      }
    });
  },

  getUserProfile() {
    wx.getUserProfile({
      desc: "获取您的头像和昵称",
      success: (res) => {
        this.setData({ userInfo: res.userInfo });
        wx.setStorageSync("userInfo", res.userInfo);
        console.log("✅ User info 存储成功:", wx.getStorageSync("userInfo"));

        const userId = getUserId();
        if (!userId) {
          console.warn("⚠️ userId 为空，尝试重新登录获取 userId");
          wx.reLaunch({ url: "/pages/login/login" });
          return;
        }

        wx.switchTab({
          url: "/pages/index/index",
          success: () => console.log("✅ 跳转到首页成功！"),
          fail: (err) => console.error("❌ 跳转到首页失败：", err)
        });
      }
    });
  },

});
