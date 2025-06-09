// pages/utils/auth.js

export function getUserId() {
  const app = getApp();
  return app?.globalData?.userId || wx.getStorageSync("userId");
}

export function getToken() {
  return wx.getStorageSync("token");
}

export function setUserId(id) {
  const app = getApp();
  app.globalData.userId = id;
  wx.setStorageSync("userId", id);
}

export function setToken(token) {
  wx.setStorageSync("token", token);
}
