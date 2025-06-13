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
      signature: '',
      photos: [] // 个人生活照片数组
    },
    region: ['北京市', '北京市', '东城区'],
    heightArray: Array.from({length: 81}, (_, i) => 140 + i), // 140cm - 220cm
    heightIndex: 0,
    signatureCount: 0,
    showUnsavedDialog: false,
    isDataModified: false, // 标记数据是否被修改
    originalData: null, // 保存原始数据
    maxPhotos: 9, // 最大照片数量
    isUploadingPhoto: false
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
            signature: data.signature || '',
            photos: data.photos || []
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
    if (this.data.isUploadingPhoto) {
      wx.showToast({
        title: '正在上传中...',
        icon: 'none'
      });
      return;
    }

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        
        // 预览选择的图片
        this.setData({
          'userInfo.avatar': tempFilePath,
          isUploadingPhoto: true
        });

        // 压缩图片
        wx.compressImage({
          src: tempFilePath,
          quality: 80,
          success: (compressRes) => {
            this.uploadAvatar(compressRes.tempFilePath);
          },
          fail: () => {
            // 如果压缩失败，使用原图上传
            this.uploadAvatar(tempFilePath);
          }
        });
      }
    });
  },

  // 上传头像
  uploadAvatar(filePath) {
    wx.showLoading({
      title: '上传中...',
      mask: true
    });

    wx.uploadFile({
      url: config.baseUrl + '/upload/avatar', // 使用与personal.js一致的接口
      filePath: filePath,
      name: 'file',
      header: {
        Authorization: "Bearer " + wx.getStorageSync("token")
      },
      success: (uploadRes) => {
        console.log('上传响应:', uploadRes);
        try {
          // 检查HTTP状态码
          if (uploadRes.statusCode !== 200) {
            throw new Error(`HTTP ${uploadRes.statusCode}: ${uploadRes.data}`);
          }
          
          const data = JSON.parse(uploadRes.data);
          console.log('解析后的数据:', data);
          
          if (data.url) {
            this.setData({
              'userInfo.avatar': data.url,
              isDataModified: true
            });
            wx.showToast({
              title: '上传成功',
              icon: 'success'
            });
          } else {
            throw new Error(data.message || '上传失败');
          }
        } catch (error) {
          console.error('上传头像失败:', error);
          // 恢复原来的头像
          this.setData({
            'userInfo.avatar': this.data.originalData ? JSON.parse(this.data.originalData).avatar : ''
          });
          wx.showToast({
            title: error.message || '上传失败',
            icon: 'error'
          });
        }
      },
      fail: (error) => {
        console.error('上传头像请求失败:', error);
        // 恢复原来的头像
        this.setData({
          'userInfo.avatar': this.data.originalData ? JSON.parse(this.data.originalData).avatar : ''
        });
        wx.showToast({
          title: '网络错误',
          icon: 'error'
        });
      },
      complete: () => {
        wx.hideLoading();
        this.setData({ isUploadingPhoto: false });
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

  // 选择照片
  choosePhotos() {
    const remainCount = this.data.maxPhotos - this.data.userInfo.photos.length;
    if (remainCount <= 0) {
      wx.showToast({
        title: '最多上传9张照片',
        icon: 'none'
      });
      return;
    }

    if (this.data.isUploadingPhoto) {
      wx.showToast({
        title: '正在上传中...',
        icon: 'none'
      });
      return;
    }

    wx.chooseMedia({
      count: remainCount,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      sizeType: ['compressed'],
      success: (res) => {
        this.uploadPhotos(res.tempFiles);
      }
    });
  },

  // 上传照片
  uploadPhotos(tempFiles) {
    if (tempFiles.length === 0) return;

    this.setData({ isUploadingPhoto: true });
    wx.showLoading({
      title: '上传中...',
      mask: true
    });

    const uploadTasks = tempFiles.map(file => {
      return new Promise((resolve, reject) => {
        wx.compressImage({
          src: file.tempFilePath,
          quality: 80,
          success: (compressRes) => {
            wx.uploadFile({
              url: config.baseUrl + '/upload/avatar', // 使用与头像上传相同的接口
              filePath: compressRes.tempFilePath,
              name: 'file',
              header: {
                Authorization: "Bearer " + wx.getStorageSync("token")
              },
              success: (uploadRes) => {
                console.log('照片上传响应:', uploadRes);
                try {
                  // 检查HTTP状态码
                  if (uploadRes.statusCode !== 200) {
                    throw new Error(`HTTP ${uploadRes.statusCode}: ${uploadRes.data}`);
                  }
                  
                  const data = JSON.parse(uploadRes.data);
                  console.log('照片解析后的数据:', data);
                  
                  if (data.url) {
                    resolve(data.url);
                  } else {
                    reject(new Error(data.message || '上传失败'));
                  }
                } catch (error) {
                  console.error('照片上传解析失败:', error);
                  reject(error);
                }
              },
              fail: reject
            });
          },
          fail: () => {
            // 压缩失败，使用原图上传
            wx.uploadFile({
              url: config.baseUrl + '/upload/avatar', // 使用与头像上传相同的接口
              filePath: file.tempFilePath,
              name: 'file',
              header: {
                Authorization: "Bearer " + wx.getStorageSync("token")
              },
              success: (uploadRes) => {
                console.log('照片上传响应:', uploadRes);
                try {
                  // 检查HTTP状态码
                  if (uploadRes.statusCode !== 200) {
                    throw new Error(`HTTP ${uploadRes.statusCode}: ${uploadRes.data}`);
                  }
                  
                  const data = JSON.parse(uploadRes.data);
                  console.log('照片解析后的数据:', data);
                  
                  if (data.url) {
                    resolve(data.url);
                  } else {
                    reject(new Error(data.message || '上传失败'));
                  }
                } catch (error) {
                  console.error('照片上传解析失败:', error);
                  reject(error);
                }
              },
              fail: reject
            });
          }
        });
      });
    });

    Promise.all(uploadTasks)
      .then(photoUrls => {
        const newPhotos = [...this.data.userInfo.photos, ...photoUrls];
        this.setData({
          'userInfo.photos': newPhotos,
          isDataModified: true
        });
        
        // 临时解决方案：保存到本地存储（后端修复前的临时方案）
        wx.setStorageSync('userPhotos', newPhotos);
        console.log('照片已保存到本地存储:', newPhotos);
        
        // 自动保存到后端
        this.savePhotosToServer(newPhotos);
        
        wx.showToast({
          title: '上传成功',
          icon: 'success'
        });
      })
      .catch(error => {
        console.error('上传照片失败:', error);
        wx.showToast({
          title: '上传失败',
          icon: 'error'
        });
      })
      .finally(() => {
        wx.hideLoading();
        this.setData({ isUploadingPhoto: false });
      });
  },

  // 保存照片到服务器
  savePhotosToServer(photos) {
    wx.request({
      url: config.baseUrl + '/users/profile',
      method: 'PUT',
      data: {
        ...this.data.userInfo,
        photos: photos,
        location: this.data.region.join(' '),
        height: this.data.heightArray[this.data.heightIndex]
      },
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token'),
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200) {
          console.log('照片保存成功');
          this.setData({
            originalData: JSON.stringify(this.data.userInfo)
          });
        } else {
          console.error('照片保存失败:', res);
        }
      },
      fail: (error) => {
        console.error('照片保存请求失败:', error);
      }
    });
  },

  // 预览照片
  previewPhoto(e) {
    const { index } = e.currentTarget.dataset;
    const { photos } = this.data.userInfo;
    
    wx.previewImage({
      current: photos[index],
      urls: photos
    });
  },

  // 删除照片
  deletePhoto(e) {
    const { index } = e.currentTarget.dataset;
    Dialog.confirm({
      title: '提示',
      message: '确定要删除这张照片吗？'
    }).then(() => {
      const photos = [...this.data.userInfo.photos];
      photos.splice(index, 1);
      this.setData({
        'userInfo.photos': photos,
        isDataModified: true
      });
      
      // 临时解决方案：更新本地存储（后端修复前的临时方案）
      wx.setStorageSync('userPhotos', photos);
      console.log('本地存储照片已更新:', photos);
      
      // 自动保存到后端
      this.savePhotosToServer(photos);
      
      wx.showToast({
        title: '删除成功',
        icon: 'success'
      });
    }).catch(() => {
      // 取消删除
    });
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
