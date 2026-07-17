# OpenTV

[![Android CI](https://github.com/lc5900/openTV/actions/workflows/android-ci.yml/badge.svg)](https://github.com/lc5900/openTV/actions/workflows/android-ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

<p align="center">
  <img src="app/src/main/res/drawable-nodpi/ic_launcher_brand.png" width="112" alt="OpenTV 图标">
</p>

OpenTV 是一款使用 Kotlin 与 Jetpack Compose 构建的轻量 Android IPTV 播放器。项目由早期 Android 版本重新设计而来，采用 Material 3 界面、Media3 播放内核，并提供可维护的频道订阅与多线路管理。

## 主要功能

- 播放 HLS、HTTP/HTTPS 直播流和 RTSP 单播流。
- 订阅远程 M3U/M3U8 或 OpenTV JSON 频道列表。
- 解析 `#EXTINF`、`tvg-name`、`group-title`、`#EXTGRP` 及相对地址。
- 合并同名频道的多个播放源，播放失败时自动切换备用线路。
- 在设置中添加、编辑或删除频道线路，并随时恢复内置频道。
- 支持频道搜索、分组搜索、深色主题和横竖屏播放。

## 添加 IPTV 订阅

打开应用右上角的“设置”，在“网络订阅”中粘贴 HTTP 或 HTTPS 地址并点击“同步”。同步会替换当前频道列表；手工修改后的列表保存在设备本地。

支持标准 M3U 内容：

```m3u
#EXTM3U
#EXTINF:-1 tvg-name="新闻频道" group-title="新闻",新闻频道
https://example.com/live/news.m3u8
```

也支持 OpenTV JSON：

```json
[
  {
    "id": 1,
    "name": "新闻频道",
    "group": "新闻",
    "urls": ["https://example.com/live/news.m3u8"]
  }
]
```

## 构建与运行

需要 JDK 17 或更高版本，以及 Android SDK 36。项目使用 Gradle Wrapper，无需单独安装 Gradle。

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat installDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

提交代码前建议运行：

```powershell
.\gradlew.bat testDebugUnitTest lintDebug
```

## 技术栈

- Kotlin、Coroutines 与 StateFlow
- Jetpack Compose、Material 3
- AndroidX Media3 / ExoPlayer
- DataStore Preferences
- Kotlinx Serialization JSON

## 协议与使用说明

RTSP 支持取决于流媒体编码和设备解码能力。当前不支持 UDP 多播、Stalker Portal 或 Xtream Codes API 登录。内置频道仅用于演示公开直播能力，播放地址可能因服务方调整、网络环境或地区限制而失效；请仅订阅和播放你有权访问的内容。

贡献代码前请阅读 [AGENTS.md](AGENTS.md)。

## 开源协议

OpenTV 使用 [Apache License 2.0](LICENSE) 发布。你可以使用、修改和分发本项目，包括商业用途，但需要保留许可证和版权声明。内置或订阅的直播内容不属于本项目授权范围，使用者需要自行确认内容访问权。
