import config from "../../utils/config";
import { setUserId, setToken, getUserId } from "../../utils/auth";

Page({
  data: {
    userInfo: null
  },

  onLoad() {
    console.log("🔄 登录页面加载");
    
    // 开发者工具诊断
    this.diagnoseDeveloperTool();
    
    // 清除旧的登录信息
    wx.removeStorageSync('token');
    wx.removeStorageSync('userId');
    
    // 输出诊断信息
    this.logSystemInfo();
  },

  diagnoseDeveloperTool() {
    // 检查是否在开发者工具中运行
    try {
      const accountInfo = wx.getAccountInfoSync();
      console.log("📊 小程序账户信息:", accountInfo);
      
      // 如果系统信息获取失败，提示用户重置工具
      const systemInfo = wx.getSystemInfoSync();
      if (!systemInfo.platform || !systemInfo.version) {
        console.warn("⚠️ 检测到开发者工具可能存在问题，建议：");
        console.warn("1. 关闭并重新打开微信开发者工具");
        console.warn("2. 清除工具缓存：工具 -> 清除缓存 -> 清除所有");
        console.warn("3. 重新编译项目");
        
        // 在界面显示提示
        wx.showModal({
          title: '开发者工具问题',
          content: '检测到开发者工具可能存在问题，建议：\n1. 重启微信开发者工具\n2. 清除工具缓存\n3. 重新编译项目',
          showCancel: false,
          confirmText: '知道了'
        });
      }
    } catch (e) {
      console.error("❌ 开发者工具诊断失败:", e);
    }
  },

  logSystemInfo() {
    try {
      const systemInfo = wx.getSystemInfoSync();
      console.log("📱 系统信息:", {
        platform: systemInfo.platform,
        version: systemInfo.version,
        SDKVersion: systemInfo.SDKVersion,
        model: systemInfo.model
      });
      
      // 检查微信版本是否支持 wx.login
      if (wx.canIUse('login')) {
        console.log("✅ 当前微信版本支持 wx.login");
      } else {
        console.warn("⚠️ 当前微信版本不支持 wx.login");
      }
    } catch (e) {
      console.error("❌ 获取系统信息失败:", e);
    }
  },

  login() {
    console.log("🔄 开始登录流程");
    wx.showLoading({
      title: '登录中...',
      mask: true
    });

    // 检查网络状态
    wx.getNetworkType({
      success: (networkRes) => {
        console.log("🌐 网络状态:", networkRes.networkType);
        if (networkRes.networkType === 'none') {
          wx.hideLoading();
          wx.showToast({
            title: '请检查网络连接',
            icon: 'error'
          });
          return;
        }
        
        this.performLogin();
      },
      fail: () => {
        console.warn("⚠️ 无法获取网络状态，继续登录流程");
        this.performLogin();
      }
    });
  },

  performLogin() {
    wx.login({
      success: (res) => {
        console.log("✅ wx.login 成功，返回的 code:", res.code);

        if (!res.code) {
          console.error("❌ 获取 code 失败！");
          wx.hideLoading();
          wx.showToast({
            title: '登录失败，请重试',
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
              const userId = res.data.userId.toString(); // 确保是字符串
              setUserId(userId);
              wx.setStorageSync('userId', userId); // 双重保险
              console.log("✅ userId 存储成功:", userId);

              if (res.data.token) {
                const token = res.data.token;
                setToken(token);
                wx.setStorageSync('token', token); // 双重保险
                console.log("✅ Token 存储成功");

                // 更新全局登录状态
                const app = getApp();
                if (app) {
                  app.globalData.isLoggedIn = true;
                  app.globalData.userInfo = res.data.userInfo || { userId: userId };
                  console.log("✅ 全局登录状态已更新");
                  
                  // 调用刷新登录状态方法
                  if (app.refreshLoginStatus) {
                    app.refreshLoginStatus();
                  }
                }

                wx.showToast({
                  title: '登录成功',
                  icon: 'success',
                  duration: 1500
                });

                // 延迟跳转，让用户看到成功提示
                setTimeout(() => {
                  // 检查是否有返回页面
                  const returnUrl = wx.getStorageSync('returnUrl');
                  if (returnUrl) {
                    wx.removeStorageSync('returnUrl');
                    wx.navigateBack({
                      success: () => console.log("✅ 返回上一页成功！"),
                      fail: () => {
                        wx.reLaunch({
                          url: returnUrl,
                          success: () => console.log("✅ 跳转到指定页面成功！"),
                          fail: () => wx.reLaunch({ url: "/pages/index/index" })
                        });
                      }
                    });
                  } else {
                    wx.reLaunch({
                      url: "/pages/welcome/welcome",
                      success: () => console.log("✅ 跳转到首页成功！"),
                      fail: (err) => console.error("❌ 跳转失败：", err)
                    });
                  }
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
        
        // 显示详细错误信息和解决建议
        wx.showModal({
          title: '登录失败',
          content: '微信登录授权失败，请确保：\n1. 网络连接正常\n2. 重新启动小程序\n3. 检查微信版本是否最新',
          showCancel: true,
          cancelText: '模拟登录',
          confirmText: '重试',
          success: (res) => {
            if (res.confirm) {
              // 用户选择重试
              setTimeout(() => {
                this.login();
              }, 1000);
            } else if (res.cancel) {
              // 开发模式下的模拟登录
              this.simulateLogin();
            }
          }
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

        const userId = getUserId() || wx.getStorageSync('userId'); // 双重检查
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

  // 开发模式下的模拟登录（仅用于测试）
  simulateLogin() {
    console.log("🔧 使用模拟登录进行测试");
    wx.showLoading({
      title: '模拟登录中...',
      mask: true
    });

    // 模拟一个测试code
    const testCode = 'test_code_' + Date.now();
    const loginUrl = `${config.baseUrl}/wechat/login`;
    
    console.log("📤 模拟登录请求，地址:", loginUrl);
    console.log("📤 使用测试 code:", testCode);

    wx.request({
      url: loginUrl,
      method: 'GET',
      data: { code: testCode },
      success: (res) => {
        console.log('📥 模拟登录响应:', res.data);
        wx.hideLoading();
        
        if (res.statusCode === 200) {
          wx.showToast({
            title: '服务器连接成功',
            icon: 'success'
          });
        } else {
          wx.showToast({
            title: '服务器连接失败',
            icon: 'error'
          });
        }
      },
      fail: (err) => {
        console.error("❌ 模拟登录请求失败:", err);
        wx.hideLoading();
        wx.showModal({
          title: '网络测试',
          content: `服务器连接失败:\n${err.errMsg}\n\n请检查：\n1. 服务器是否正常运行\n2. 域名配置是否正确\n3. 网络连接是否正常`,
          showCancel: false
        });
      }
    });
  }

});
