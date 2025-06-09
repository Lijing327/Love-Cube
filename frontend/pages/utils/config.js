const isDevTools = wx.getSystemInfoSync().platform === 'devtools';

// 🔁 控制是否强制使用本地后端
const forceUseLocal = true;

const config = {
  local: {
    baseUrl: "http://192.168.1.158:8090/admin/api",
    wsBaseUrl: "ws://192.168.1.158:8090/admin/chat"
  },
  prod: {
    baseUrl: "https://xifg.com.cn/admin/api",
    wsBaseUrl: "wss://xifg.com.cn/admin/chat"
  }
};

export default forceUseLocal
  ? config.local
  : (isDevTools ? config.prod : config.local);