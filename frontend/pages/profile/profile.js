import config from "../../utils/config";
import Dialog from '@vant/weapp/dialog/dialog';

Page({
  data: {
    userInfo: {
      avatar: '',
      nickname: '',
      gender: '',
      birthday: '',
      constellation: '',
      occupation: '',
      height: '',
      signature: ''
    },
    region: ['北京市', '北京市', '东城区'],
    heightArray: Array.from({length: 81}, (_, i) => 140 + i), // 140cm - 220cm
    heightIndex: 0,
    signatureCount: 0,
    showUnsavedDialog: false,
    isDataModified: false, // 标记数据是否被修改
    originalData: null // 保存原始数据
  },

  onLoad() {
    this.checkLoginStatus();
    this.getUserInfo();
  },

  // 检查登录状态
  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.showToast({
        title: '请先登录',
        icon: 'none',
        success: () => {
          setTimeout(() => {
            wx.redirectTo({
              url: '/pages/login/login'
            });
          }, 1500);
        }
      });
      return false;
    }
    return true;
  },

  // 获取用户信息
  getUserInfo() {
    if (!this.checkLoginStatus()) return;

    wx.showLoading({
      title: '加载中...'
    });

    wx.request({
      url: config.baseUrl + '/users/me',
      method: 'GET',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token')
      },
      success: (res) => {
        if (res.statusCode === 200) {
          const data = res.data;
          const userInfo = {
            avatar: data.profilePhoto || '/images/default-avatar.png',
            nickname: data.nickname || '',
            gender: data.gender || '',
            birthday: data.birthday || '',
            constellation: data.constellation || '未知',
            occupation: data.occupation || '',
            height: data.height || '',
            signature: data.signature || ''
          };
          
          this.setData({
            userInfo,
            originalData: JSON.stringify(userInfo),
            region: data.location ? data.location.split(' ') : ['北京市', '北京市', '东城区'],
            signatureCount: data.signature ? data.signature.length : 0
          });

          if (data.height) {
            const heightIndex = this.data.heightArray.indexOf(parseInt(data.height));
            this.setData({ heightIndex: heightIndex > -1 ? heightIndex : 0 });
          }
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            success: () => {
              setTimeout(() => {
                wx.redirectTo({
                  url: '/pages/login/login'
                });
              }, 1500);
            }
          });
        } else {
          wx.showToast({
            title: '获取信息失败',
            icon: 'error'
          });
        }
      },
      fail: () => {
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

  // 检查数据是否被修改
  checkDataModified() {
    const currentData = JSON.stringify(this.data.userInfo);
    return currentData !== this.data.originalData;
  },

  // 返回按钮处理
  onBack() {
    if (this.checkDataModified()) {
      this.setData({ showUnsavedDialog: true });
    } else {
      wx.navigateBack();
    }
  },

  // 确认离开
  confirmLeave() {
    this.setData({ showUnsavedDialog: false });
    wx.navigateBack();
  },

  // 取消离开
  cancelLeave() {
    this.setData({ showUnsavedDialog: false });
  },

  // 选择头像
  chooseImage() {
    wx.chooseImage({
      count: 1,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFilePaths[0];
        
        wx.showLoading({
          title: '上传中...'
        });

        // 上传图片
        wx.uploadFile({
          url: config.baseUrl + '/upload/avatar',
          filePath: tempFilePath,
          name: 'file',
          header: {
            Authorization: "Bearer " + wx.getStorageSync("token")
          },
          success: (uploadRes) => {
            const data = JSON.parse(uploadRes.data);
            if (data.url) {
              this.setData({
                'userInfo.avatar': data.url
              });
              wx.showToast({
                title: '上传成功',
                icon: 'success'
              });
            }
          },
          fail: () => {
            wx.showToast({
              title: '上传失败',
              icon: 'error'
            });
          },
          complete: () => {
            wx.hideLoading();
          }
        });
      }
    });
  },

  // 输入昵称
  onNicknameInput(e) {
    this.setData({
      'userInfo.nickname': e.detail.value
    });
  },

  // 选择性别
  selectGender(e) {
    const gender = e.currentTarget.dataset.gender;
    this.setData({
      'userInfo.gender': gender
    });
  },

  // 选择生日
  onBirthdayChange(e) {
    const birthday = e.detail.value;
    this.setData({
      'userInfo.birthday': birthday,
      'userInfo.constellation': this.getConstellation(birthday)
    });
  },

  // 选择地区
  onRegionChange(e) {
    this.setData({
      region: e.detail.value
    });
  },

  // 输入职业
  onOccupationInput(e) {
    this.setData({
      'userInfo.occupation': e.detail.value
    });
  },

  // 选择身高
  onHeightChange(e) {
    const height = this.data.heightArray[e.detail.value];
    this.setData({
      'userInfo.height': height,
      heightIndex: e.detail.value
    });
  },

  // 输入个性签名
  onSignatureInput(e) {
    const value = e.detail.value;
    this.setData({
      'userInfo.signature': value,
      signatureCount: value.length
    });
  },

  // 根据生日计算星座
  getConstellation(birthday) {
    if (!birthday) return '未知';
    
    const month = parseInt(birthday.split('-')[1]);
    const day = parseInt(birthday.split('-')[2]);
    
    const constellations = {
      '水瓶座': [[1,20], [2,18]],
      '双鱼座': [[2,19], [3,20]],
      '白羊座': [[3,21], [4,19]],
      '金牛座': [[4,20], [5,20]],
      '双子座': [[5,21], [6,21]],
      '巨蟹座': [[6,22], [7,22]],
      '狮子座': [[7,23], [8,22]],
      '处女座': [[8,23], [9,22]],
      '天秤座': [[9,23], [10,23]],
      '天蝎座': [[10,24], [11,22]],
      '射手座': [[11,23], [12,21]],
      '摩羯座': [[12,22], [1,19]]
    };

    for (let [name, dates] of Object.entries(constellations)) {
      const [[startMonth, startDay], [endMonth, endDay]] = dates;
      if (
        (month === startMonth && day >= startDay) ||
        (month === endMonth && day <= endDay)
      ) {
        return name;
      }
    }
    
    return '未知';
  },

  // 保存资料
  saveProfile() {
    if (!this.checkLoginStatus()) return;

    if (!this.data.userInfo.nickname) {
      wx.showToast({
        title: '请输入昵称',
        icon: 'none'
      });
      return;
    }

    wx.showLoading({
      title: '保存中...'
    });

    const profileData = {
      ...this.data.userInfo,
      location: this.data.region.join(' '),
      height: this.data.heightArray[this.data.heightIndex]
    };

    wx.request({
      url: config.baseUrl + '/users/profile',
      method: 'PUT',
      data: profileData,
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token'),
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200) {
          wx.showToast({
            title: '保存成功',
            icon: 'success'
          });
          this.setData({
            isDataModified: false,
            originalData: JSON.stringify(this.data.userInfo)
          });
          // 延迟返回上一页
          setTimeout(() => {
            wx.navigateBack();
          }, 1500);
        } else if (res.statusCode === 401) {
          wx.removeStorageSync('token');
          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            success: () => {
              setTimeout(() => {
                wx.redirectTo({
                  url: '/pages/login/login'
                });
              }, 1500);
            }
          });
        } else {
          wx.showToast({
            title: res.data?.message || '保存失败',
            icon: 'error'
          });
        }
      },
      fail: () => {
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

  // 页面返回拦截
  onUnload() {
    if (this.checkDataModified()) {
      // 可以选择性地添加一些提示或处理
    }
  }
});
