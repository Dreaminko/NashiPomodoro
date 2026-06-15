# NashiPomodoro

[简体中文](README_ZH.md) | [English](README.md) | [日本語](README_JA.md)

NashiPomodoro 是一款面向 Nothing Phone 的 Android 番茄钟应用。它将专注计时、任务管理和数据统计与 Glyph Interface 结合，通过机身背部灯条直观显示专注与休息进度。

## 重要提示：关闭系统 Glyph 进度条

> [!IMPORTANT]
> 在 Nothing Phone 上使用本应用前，必须先在系统设置中关闭 Nothing OS 自带的 **Glyph 进度条（Glyph Progress）**。
>
> 系统自带进度条会占用 Glyph Interface，并与 NashiPomodoro 的灯效控制发生冲突，可能导致本应用的进度灯条无法显示、显示异常或计时功能无法正常配合灯效工作。

不同 Nothing OS 版本中的菜单名称可能略有差异。请进入系统的 **Glyph Interface** 设置，找到系统自带的进度条或进度跟踪功能并将其关闭，然后再启动 NashiPomodoro。

## 功能

- 可配置的专注、短休息和长休息时长
- 自动切换番茄钟阶段，并支持设置长休息间隔
- Glyph 灯条显示专注、短休息和长休息的剩余进度


## 运行要求

- Android 14（API 34）或更高版本
- 建议使用带有受支持 Glyph 灯条的 Nothing Phone
- 通知、振动和 Glyph Interface 相关权限

应用可在其他符合 Android 版本要求的设备上运行基础番茄钟功能，但 Glyph 灯效仅会在代码已适配的 Nothing Phone 设备上启用。

## 开发环境

- Android Studio
- JDK 21
- Android SDK 36.1
- Gradle 9.5

## 权限说明

- **通知权限**：显示正在运行的计时前台服务通知。
- **前台服务与唤醒锁**：在应用进入后台后继续可靠计时。
- **振动权限**：在专注或休息阶段结束时提供触觉提醒。
- **Glyph 权限**：控制受支持 Nothing Phone 的 Glyph Interface 灯效。

## 翻译

项目使用标准 Android 字符串资源管理多语言内容。新增或维护翻译时，请参阅 [TRANSLATING.md](TRANSLATING.md)。
