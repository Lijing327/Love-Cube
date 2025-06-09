import config from "../utils/config";
import { setUserId, setToken } from "../utils/auth";
import { getUserId } from "../utils/auth";

Page({
  data: {
    userInfo: null
  },

  login() {
    wx.login({
      success: (res) => {
        console.log("✅ wx.login 成功，返回的 code:", res.code);

        if (!res.code) {
          console.error("❌ 获取 code 失败！");
          return;
        }
        console.log("📤 即将发起登录请求，地址为：", `${config.baseUrl}/wechat/login`, "code:", res.code);

        wx.request({
          url: `${config.baseUrl}/wechat/login`,
          method: 'GET',
          data: { code: res.code },
          header: { 'Content-Type': 'application/json' },
          success(res) {
            console.log('✅ 登录成功:', res.data);

            if (res.data.userId) {
              setUserId(res.data.userId); // ✅ 封装的方式设置 userId
              console.log("✅ userId 存储成功:", res.data.userId);

              if (res.data.token && res.data.token.includes(".")) {
                setToken(res.data.token); // ✅ 封装的方式设置 token
                console.log("✅ Token 存储成功:", res.data.token);
              } else {
                console.error("❌ 服务器返回的 token 无效:", res.data.token);
              }

              const storedUserId = getUserId();
              if (!storedUserId) {
                console.error("❌ userId 存储失败，检查后端返回数据");
                return;
              }

              wx.redirectTo({
                url: "/pages/welcome/welcome",
                success: () => console.log("✅ 跳转到首页成功！"),
                fail: (err) => console.error("❌ 跳转到首页失败：", err)
              });

            } else if (res.data.status === "USER_NOT_REGISTERED") {
              console.warn("⚠️ 用户未注册，跳转到注册页");
              wx.setStorageSync("openid", res.data.openid);
              wx.navigateTo({ url: "/pages/register/register" });
            } else {
              console.error("❌ 登录失败:", res.data.message);
            }
          },
          fail(err) {
            console.error('❌ 登录请求失败:', err);
          }
        });
      },
      fail: (err) => {
        console.error("❌ wx.login error:", err);
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
          wx.redirectTo({ url: "/pages/login/login" });
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
