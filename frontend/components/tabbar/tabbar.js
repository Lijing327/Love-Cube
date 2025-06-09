Component({
  properties: {
    currentPage: {
      type: String,
      value: ""
    }
  },

  methods: {
    switchTab(e) {
      const url = e.currentTarget.dataset.url;
      wx.switchTab({ url });
    }
  }
});
