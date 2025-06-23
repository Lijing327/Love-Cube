import config from "../../utils/config";

Page({
  data: {
    nickname: "用户" + Math.floor(Math.random() * 10000),
    avatarUrl: "/assets/default-avatar.png",
    phone: "",
    openid: ""
  },

  onLoad() {
    let openid = wx.getStorageSync("openid");
    if (!openid) {
      wx.showToast({ title: "请重新登录", icon: "none" });
      wx.redirectTo({ url: "/pages/login/login" });
      return;
    }
    this.setData({ openid });
  },

  chooseAvatar() {
    let that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        console.log("选择的头像:", res.tempFiles[0].tempFilePath);
        that.setData({ avatarUrl: res.tempFiles[0].tempFilePath });
      }
    });
  },

  inputNickname(e) {
    this.setData({ nickname: e.detail.value });
  },

  inputPhone(e) {
    this.setData({ phone: e.detail.value });
  },

  submitRegister() {
    if (!this.data.phone) {
      wx.showToast({ title: "请输入手机号", icon: "none" });
      return;
    }
  
    const that = this;
  
    wx.request({
      url: `${config.baseUrl}/users/register`,
      method: "POST",
      header: {
        "Content-Type": "application/json"
      },
      data: {
        openid: this.data.openid,
        username: this.data.nickname,
        profile_photo: this.data.avatarUrl,
        phone_number: this.data.phone
      },
      success(res) {
        console.log("📥 注册接口响应：", res);
        if (res.statusCode === 400 && res.data === "用户已注册") {
          wx.showToast({ title: "您已注册，请直接登录", icon: "none" });
          wx.redirectTo({ url: "/pages/login/login" });
          return;
        }
        
        if (!res.data || !res.data.userId) {
          wx.showToast({
            title: "注册失败，请重试",
            icon: "error"
          });
          return;
        }
        console.log("✅ 注册成功:", res.data);
  
        // 显示注册成功提示
        wx.showToast({ title: "注册成功！", icon: "success", duration: 1500 });
  
        // 等待提示完成后执行登录
        setTimeout(() => {
          wx.login({
            success(loginRes) {
              if (!loginRes.code) {
                console.error("❌ 获取 code 失败！");
                return;
              }
  
              wx.request({
                url: `${config.baseUrl}/wechat/login`,
                method: "GET",
                data: { code: loginRes.code },
                success(loginResult) {
                  console.log("✅ 注册后登录成功:", loginResult.data);
  
                  if (loginResult.data.userId && loginResult.data.token) {
                    wx.setStorageSync('userId', loginResult.data.userId);
                    wx.setStorageSync('token', loginResult.data.token);
  
                    wx.redirectTo({
                      url: "/pages/welcome/welcome",
                      success: () => console.log("✅ 跳转到首页成功！")
                    });
                  } else {
                    console.error("❌ 注册后登录失败，返回数据异常:", loginResult.data);
                  }
                },
                fail(err) {
                  console.error("❌ 注册后登录请求失败:", err);
                }
              });
            }
          });
        }, 1500); // 与 Toast 一致等待时间
      },
      fail(err) {
        console.error("❌ 注册失败:", err);
      }
    });
  }
  
});
