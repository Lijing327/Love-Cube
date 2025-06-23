import config from "../../utils/config";

Page({
  data: {
    dynamicList: [],
    pageNum: 1,
    pageSize: 10,
    isLoading: false,
    isRefreshing: false,
    noMore: false,
    showActionSheet: false,
    currentDynamicId: null,
    showCommentInput: false,
    commentContent: '',
    keyboardHeight: 0,
    actions: [
      { name: '删除', color: '#ff4d6a' },
      { name: '举报' }
    ],
  },

  onLoad() {
    this.loadDynamicList();
  },

  // 加载动态列表
  loadDynamicList(refresh = false) {
    if (this.data.isLoading || (this.data.noMore && !refresh)) return;

    const pageNum = refresh ? 1 : this.data.pageNum;

    this.setData({ isLoading: true });

    wx.request({
      url: config.baseUrl + '/dynamics',
      method: 'GET',
      data: {
        pageNum,
        pageSize: this.data.pageSize
      },
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const newList = res.data.list.map(item => ({
            ...item,
            createTime: this.formatTime(item.createTime)
          }));

          this.setData({
            dynamicList: refresh ? newList : [...this.data.dynamicList, ...newList],
            pageNum: refresh ? 2 : this.data.pageNum + 1,
            noMore: newList.length < this.data.pageSize
          });
        }
      },
      complete: () => {
        this.setData({ 
          isLoading: false,
          isRefreshing: false
        });
      }
    });
  },

  // 下拉刷新
  onRefresh() {
    this.setData({ isRefreshing: true });
    this.loadDynamicList(true);
  },

  // 上拉加载更多
  onLoadMore() {
    this.loadDynamicList();
  },

  // 发布动态
  onPostDynamic() {
    wx.showToast({
      title: '发布功能开发中',
      icon: 'none',
      duration: 2000
    });
    // TODO: 后续实现发布动态页面
    // wx.navigateTo({
    //   url: '/pages/post/post'
    // });
  },

  // 点赞
  onLike(e) {
    const { id } = e.currentTarget.dataset;
    const dynamicList = this.data.dynamicList;
    const index = dynamicList.findIndex(item => item.id === id);
    
    if (index === -1) return;

    const isLiked = dynamicList[index].isLiked;
    const likeCount = dynamicList[index].likeCount || 0;

    wx.request({
      url: config.baseUrl + `/dynamics/${id}/like`,
      method: isLiked ? 'DELETE' : 'POST',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (res) => {
        if (res.statusCode === 200) {
          this.setData({
            [`dynamicList[${index}].isLiked`]: !isLiked,
            [`dynamicList[${index}].likeCount`]: isLiked ? likeCount - 1 : likeCount + 1
          });
        }
      }
    });
  },

  // 评论
  onComment(e) {
    const { id } = e.currentTarget.dataset;
    this.setData({
      currentDynamicId: id,
      showCommentInput: true
    });
  },

  // 分享
  onShare(e) {
    const { id } = e.currentTarget.dataset;
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    });
  },

  // 显示操作菜单
  showActionSheet(e) {
    const { id } = e.currentTarget.dataset;
    this.setData({
      showActionSheet: true,
      currentDynamicId: id
    });
  },

  // 关闭操作菜单
  onCloseActionSheet() {
    this.setData({
      showActionSheet: false,
      currentDynamicId: null
    });
  },

  // 选择操作
  onSelectAction(e) {
    const { name } = e.detail;
    const id = this.data.currentDynamicId;

    if (name === '删除') {
      this.deleteDynamic(id);
    } else if (name === '举报') {
      this.reportDynamic(id);
    }

    this.onCloseActionSheet();
  },

  // 删除动态
  deleteDynamic(id) {
    wx.showModal({
      title: '提示',
      content: '确定要删除这条动态吗？',
      success: (res) => {
        if (res.confirm) {
          wx.request({
            url: config.baseUrl + `/dynamics/${id}`,
            method: 'DELETE',
            header: {
              Authorization: "Bearer " + wx.getStorageSync("token")
            },
            success: (res) => {
              if (res.statusCode === 200) {
                const dynamicList = this.data.dynamicList.filter(item => item.id !== id);
                this.setData({ dynamicList });
                wx.showToast({
                  title: '删除成功',
                  icon: 'success'
                });
              }
            }
          });
        }
      }
    });
  },

  // 举报动态
  reportDynamic(id) {
    wx.showToast({
      title: '举报成功',
      icon: 'success'
    });
  },

  // 预览图片
  previewImage(e) {
    const { urls, current } = e.currentTarget.dataset;
    wx.previewImage({
      urls,
      current
    });
  },

  // 查看全部评论
  viewAllComments(e) {
    const { id } = e.currentTarget.dataset;
    wx.navigateTo({
      url: `/pages/comments/comments?id=${id}`
    });
  },

  // 评论输入
  onCommentInput(e) {
    this.setData({
      commentContent: e.detail.value
    });
  },

  // 提交评论
  submitComment() {
    const content = this.data.commentContent.trim();
    if (!content) return;

    const id = this.data.currentDynamicId;
    const dynamicList = this.data.dynamicList;
    const index = dynamicList.findIndex(item => item.id === id);
    if (index > -1) {
      const dynamic = dynamicList[index];
      const newComment = {
        id: Date.now(),
        content,
        userInfo: {
          nickname: wx.getStorageSync('userInfo')?.nickname || '我'
        }
      };
      this.setData({
        [`dynamicList[${index}].topComments`]: [newComment, ...(dynamic.topComments || [])].slice(0, 2),
        [`dynamicList[${index}].commentCount`]: (dynamic.commentCount || 0) + 1,
        showCommentInput: false,
        commentContent: ''
      });
      wx.showToast({
        title: '评论成功',
        icon: 'success'
      });
    }
  },

  // 输入框获得焦点
  onInputFocus(e) {
    this.setData({
      keyboardHeight: e.detail.height || 0
    });
  },

  // 输入框失去焦点
  onInputBlur() {
    this.setData({
      keyboardHeight: 0
    });
  },

  // 关闭评论输入框
  onCloseCommentInput() {
    this.setData({
      showCommentInput: false,
      commentContent: '',
      keyboardHeight: 0
    });
  },

  // 格式化时间
  formatTime(timestamp) {
    const now = new Date().getTime();
    const diff = now - timestamp;
    
    if (diff < 60000) { // 1分钟内
      return '刚刚';
    } else if (diff < 3600000) { // 1小时内
      return Math.floor(diff / 60000) + '分钟前';
    } else if (diff < 86400000) { // 24小时内
      return Math.floor(diff / 3600000) + '小时前';
    } else if (diff < 604800000) { // 7天内
      return Math.floor(diff / 86400000) + '天前';
    } else {
      const date = new Date(timestamp);
      return `${date.getMonth() + 1}月${date.getDate()}日`;
    }
  },

  onShareAppMessage(res) {
    if (res.from === 'button') {
      const { id } = res.target.dataset;
      const dynamic = this.data.dynamicList.find(item => item.id === id);
      
      return {
        title: dynamic.content,
        path: `/pages/dynamic/detail?id=${id}`,
        imageUrl: dynamic.images ? dynamic.images[0] : ''
      };
    }
    return {
      title: '心愿魔方',
      path: '/pages/dynamic/dynamic'
    };
  },
});
