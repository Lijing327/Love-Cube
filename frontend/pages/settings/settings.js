import Dialog from "@vant/weapp/dialog/dialog";
import config from "../../utils/config";


Page({
  // 清理缓存
  clearCache() {
    Dialog.confirm({
      title: "清理缓存",
      message: "确定要清理缓存吗？",
      selector: '#dialog'
    }).then(() => {
      wx.clearStorageSync();
      wx.showToast({ title: "缓存已清理", icon: "success" });
    });
  },

  logout() {
    wx.showModal({
      title: "退出登录",
      content: "确定要退出当前账号吗？",
      success(res) {
        if (res.confirm) {
          wx.clearStorageSync();
          wx.redirectTo({ url: "/pages/login/login" });
        }
      }
    });
  }
});
