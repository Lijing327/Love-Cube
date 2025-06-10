const config = {
  baseUrl: "http://192.168.1.158:8090/admin/api",
  wsBaseUrl: "ws://192.168.1.158:8090/admin",
  version: '1.0.0',
  
  // 上传相关配置
  upload: {
    imageUrl: 'http://192.168.1.158:8090/admin/api/upload/image',
    maxSize: 5 * 1024 * 1024, // 5MB
    acceptTypes: ['jpg', 'jpeg', 'png', 'gif']
  },
  
  // 默认配置
  defaults: {
    avatar: '/static/images/default-avatar.png',
    pageSize: 10,
    wsReconnectInterval: 3000, // WebSocket重连间隔（毫秒）
    wsHeartbeatInterval: 30000, // WebSocket心跳间隔（毫秒）
    wsMaxReconnectAttempts: 5, // 最大重连次数
    wsPath: '/ws/chat' // WebSocket 路径
  },

  // 图片处理配置
  images: {
    baseUrl: 'http://192.168.1.158:8090/admin',
    defaultAvatar: '/static/images/default-avatar.png', // 默认头像
    avatarPlaceholder: '/static/images/avatar-placeholder.png', // 头像加载失败时的占位图
    uploadPath: '/uploads/avatar/' // 头像上传路径
  },
  
  // 缓存相关配置
  storage: {
    tokenKey: 'token',
    userInfoKey: 'userInfo',
    expireTime: 7 * 24 * 60 * 60 * 1000 // 7天
  }
};

export default config; 