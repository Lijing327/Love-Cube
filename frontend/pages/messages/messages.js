import config from "../../utils/config";
import { getUserId } from "../utils/auth";

Page({
  data: {
    messages: []
  },

  onLoad() {
    let userId = getUserId();
    if (!userId) {
      console.warn("⚠️ userId 不存在，跳转登录页");
      wx.redirectTo({ url: "/pages/login/login" });
      return;
    }
    console.log("📩 请求聊天列表，用户ID:", userId);

    wx.request({
      url: `${config.baseUrl}/chat/partners/${userId}`,
      method: "GET",
      success: (res) => {
        console.log("✅ 消息列表加载成功: ", res.data);
        if (Array.isArray(res.data) && res.data.length > 0) {
          let messages = res.data.map(item => ({
            id: item.id, // ID
            avatar: item.avatar, // 头像
            username: item.username || "未知用户", // 用户名
            lastMessage: item.lastMessage||"暂无聊天记录",
            time: item.time || "刚刚" // 真实时间
          }));
          this.setData({ messages });
        } else {
          console.warn("⚠️ 消息列表为空");
          wx.showToast({ title: "暂无聊天记录", icon: "none" });
        }
      },
      fail: (err) => {
        console.error("❌ 获取消息列表失败: ", err);
      }
    });
  },

  onSwipeChange(e) {
    console.log("滑动事件:", e);
  },


  // 隐藏消息（不删除，只是隐藏）
  hideMessage(e) {
    let id = e.currentTarget.dataset.id;
    let messages = this.data.messages.map(msg => {
      if (msg.id === id) msg.hidden = true;
      return msg;
    });
    this.setData({ messages });
  },

  // 删除消息（从数据库删除）
  deleteMessage(e) {
    let id = e.currentTarget.dataset.id;
    wx.request({
      url: `${config.baseUrl}/chat/delete/${id}`,
      method: "DELETE",
      success(res) {
        console.log("✅ 聊天对话删除成功");
        let messages = this.data.messages.filter(msg => msg.id !== id);
        this.setData({ messages });
      },
      fail(err) {
        console.error("❌ 删除聊天失败: ", err);
      }
    });
  }
});
