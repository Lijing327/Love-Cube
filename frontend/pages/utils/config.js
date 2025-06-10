const isDevTools = wx.getAppBaseInfo().platform === 'devtools';

// 🔁 控制是否强制使用本地后端
const forceUseLocal = true;

const config = {
  local: {
    baseUrl: 'http://192.168.1.158:8090/admin/api',
    wsUrl: 'ws://192.168.1.158:8090/admin/ws',
    imageBaseUrl: 'http://192.168.1.158:8090/admin'
  },
  prod: {
    baseUrl: 'https://xifg.com.cn/admin/api',
    wsUrl: 'wss://xifg.com.cn/admin/ws',
    imageBaseUrl: 'https://xifg.com.cn/admin'
  }
};

export default forceUseLocal
  ? config.local
  : (isDevTools ? config.local : config.prod);