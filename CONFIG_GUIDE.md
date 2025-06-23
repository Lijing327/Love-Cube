# IP地址配置指南

## 概述
本项目已将IP地址配置统一管理，方便后期更改服务器地址。

## 需要修改的文件

当需要更改服务器IP地址时，请按以下顺序修改：

### 1. 前端配置
**主配置文件**: `frontend/global-config.js`
```javascript
server: {
  ip: "新的IP地址",      // 修改这里
  port: "8090",          // 如需要可修改端口
  basePath: "/admin"     // 如需要可修改基础路径
}
```

### 2. 微信小程序配置
**文件**: `frontend/project.config.json`
```json
{
  "setting": {
    "uploadFileDomain": ["新的IP地址"],
    "domain": {
      "request": ["http://新的IP地址:8090"],
      "download": ["http://新的IP地址:8090"],
      "upload": ["http://新的IP地址:8090"]
    }
  }
}
```

### 3. 后端配置
**文件**: `backend/src/main/resources/application-dev.yml`
```yaml
app:
  base-url: http://新的IP地址:8090/admin
```

## 配置优势

1. **统一管理**: 前端配置主要在 `global-config.js` 中管理
2. **自动生成**: URL会根据IP配置自动生成
3. **易于维护**: 只需修改几个关键文件

## 注意事项

- SQL文件中的测试数据URL不需要修改
- 模拟测试数据中的硬编码URL不需要修改
- 修改后需要重新编译前端项目
- 修改后需要重启后端服务

## 文件结构
```
前端配置:
├── frontend/global-config.js        (主配置文件)
├── frontend/utils/config.js         (使用全局配置)
└── frontend/project.config.json     (微信小程序域名配置)

后端配置:
└── backend/src/main/resources/application-dev.yml
``` 