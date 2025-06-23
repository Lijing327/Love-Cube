// 全局配置文件 - 统一管理IP地址和端口
const GLOBAL_CONFIG = {
  // 服务器配置
  server: {
    ip: "xifg.com.cn",      // 服务器IP地址 - 需要更改时只需修改这里
    port: "8090",             // 服务器端口
    basePath: "/admin"        // 基础路径
  },
  
  // 生成的URL配置
  get urls() {
    const { ip, port, basePath } = this.server;
    return {
      baseUrl: `http://${ip}:${port}${basePath}/api`,
      wsBaseUrl: `ws://${ip}:${port}${basePath}`,
      imageBaseUrl: `http://${ip}:${port}${basePath}`,
      uploadImageUrl: `http://${ip}:${port}${basePath}/api/upload/image`,
      uploadsPath: `http://${ip}:${port}${basePath}/uploads`,
      staticImagesPath: `http://${ip}:${port}${basePath}/images`
    };
  }
};

module.exports = GLOBAL_CONFIG; 