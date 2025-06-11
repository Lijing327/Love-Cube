// 用户认证相关的工具函数

/**
 * 获取用户ID
 * 优先从全局数据获取，如果没有则从本地存储获取
 */
function getUserId() {
  const app = getApp();
  return app?.globalData?.userId || wx.getStorageSync("userId");
}

/**
 * 获取认证token
 */
function getToken() {
  return wx.getStorageSync("token");
}

/**
 * 设置用户ID
 * 同时保存到全局数据和本地存储
 * @param {string} id 用户ID
 */
function setUserId(id) {
  if (!id) {
    console.error('尝试设置空的用户ID');
    return;
  }
  const userId = id.toString(); // 确保是字符串
  const app = getApp();
  if (app?.globalData) {
    app.globalData.userId = userId;
  }
  wx.setStorageSync("userId", userId);
}

/**
 * 设置认证token
 * @param {string} token JWT token
 */
function setToken(token) {
  if (!token) {
    console.error('尝试设置空的token');
    return;
  }
  wx.setStorageSync("token", token);
}

/**
 * 清除所有认证信息
 */
function clearAuth() {
  const app = getApp();
  if (app?.globalData) {
    app.globalData.userId = null;
  }
  wx.removeStorageSync("userId");
  wx.removeStorageSync("token");
}

/**
 * 检查是否已登录
 */
function isLoggedIn() {
  const userId = getUserId();
  const token = getToken();
  return !!(userId && token);
}

module.exports = {
  getUserId,
  getToken,
  setUserId,
  setToken,
  clearAuth,
  isLoggedIn
}; 