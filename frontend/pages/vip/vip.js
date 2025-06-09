Page({
  data: {
    userInfo: {
      avatar: "",
      name: "未登录用户"
    },
    vipStatus: "未开通 VIP",
    benefits: [
      { icon: "fire", text: "优先推荐匹配" },
      { icon: "chat-o", text: "无限制聊天" },
      { icon: "eye-o", text: "查看访客" },
      { icon: "gold-coin-o", text: "尊贵身份标识" },
    ],
    packages: [
      { id: "month", name: "月卡", price: 30 },
      { id: "season", name: "季卡", price: 80 },
      { id: "year", name: "年卡", price: 280 }
    ],
    selectedPackage: "month"
  },

  onPackageSelect(e) {
    this.setData({ selectedPackage: e.detail.value });
  },

  onPay() {
    const packageInfo = this.data.packages.find(p => p.id === this.data.selectedPackage);
    wx.showModal({
      title: "确认支付",
      content: `确认支付 ${packageInfo.price} 元购买 ${packageInfo.name} VIP 吗？`,
      success: (res) => {
        if (res.confirm) {
          this.processPayment(packageInfo);
        }
      }
    });
  },

  processPayment(packageInfo) {
    wx.showLoading({ title: "支付中..." });

    // 模拟支付请求（实际支付需调用后端API）
    setTimeout(() => {
      wx.hideLoading();
      wx.showToast({ title: "支付成功", icon: "success" });

      this.setData({
        vipStatus: `VIP 有效期：${packageInfo.name}`,
      });
    }, 2000);
  }
});
