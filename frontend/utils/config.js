const config = {
  baseUrl: "https://http://192.168.1.158:8090/admin/api",
  wsBaseUrl: "wss://http://192.168.1.158:8090/admin/chat",
  version: '1.0.0',
  
  // 上传相关配置
  upload: {
    imageUrl: 'https://http://192.168.1.158:8090/admin/api/upload/image',
    maxSize: 5 * 1024 * 1024, // 5MB
    acceptTypes: ['jpg', 'jpeg', 'png', 'gif']
  },
  
  // 默认配置
  defaults: {
    avatar: '/images/默认头像.jpg',
    pageSize: 10
  },
  
  // 缓存相关配置
  storage: {
    tokenKey: 'token',
    userInfoKey: 'userInfo',
    expireTime: 7 * 24 * 60 * 60 * 1000 // 7天
  }
};

export default config; 