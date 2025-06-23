# project.config.json 配置说明

## 重要提醒 ⚠️

当更改服务器IP地址时，除了修改 `global-config.js`，还需要**手动更新** `project.config.json` 中的域名配置。

## 需要修改的配置项

在 `project.config.json` 文件中找到以下配置并更新：

```json
{
  "setting": {
    "uploadFileDomain": [
      "你的新IP地址"  // 当前: 192.168.1.158
    ],
    "domain": {
      "request": [
        "http://你的新IP地址:8090"  // 当前: http://192.168.1.158:8090
      ],
      "download": [
        "http://你的新IP地址:8090"  // 当前: http://192.168.1.158:8090
      ],
      "upload": [
        "http://你的新IP地址:8090"  // 当前: http://192.168.1.158:8090
      ]
    }
  }
}
```

## 为什么需要手动更新？

因为 `project.config.json` 是微信小程序的配置文件，必须是纯JSON格式，不能使用JavaScript变量或引用其他文件。

## 更新步骤

1. 首先修改 `global-config.js` 中的IP地址
2. 然后手动更新 `project.config.json` 中的对应IP地址
3. 重新编译项目 