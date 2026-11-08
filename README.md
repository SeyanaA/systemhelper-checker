# 组件检查 v1.0

离线检查当前 Android 用户是否安装 `com.mobiletools.systemhelper`，无需 Root。

## 使用

从 [Releases](https://github.com/SeyanaA/systemhelper-checker/releases/latest) 下载 `SystemHelper-Checker-v1.0.apk` 并安装，打开桌面上的“组件检查”。打开、返回应用或点击“重新检查”会读取最新状态。

- **未安装**：当前用户未登记安装。系统分区可能仍保留原始 APK，不算当前用户已安装。
- **已安装**：当前用户登记安装，停用或暂停也属于已安装。显示版本及启用状态，可打开系统详情页。
- **无法确认**：系统查询出错，不把查询异常当成已卸载。

应用只在本机保存最近确认未安装的时间。如果之前确认过未安装，之后发现已安装，会提示恢复安装。首次检查就已安装时，不能据此推断何时安装。

仅检查当前用户；手机分身、工作资料或其他用户请在相应空间使用。不持续在后台监控，不发送通知，不修改、卸载或恢复目标组件。卸载本应用或清除其数据会清除本机检查记录。

## 权限与隐私

Manifest 仅通过 `<queries>` 声明一个目标包，无任何 `uses-permission`；无联网、广告、统计 SDK、后台服务或开机接收器。检测使用标准 PackageManager，不请求 `MATCH_UNINSTALLED_PACKAGES`，并核对 `ApplicationInfo.FLAG_INSTALLED`。

## 构建

Android 8.0/API 26 起可安装，目标 SDK 36。使用 JDK 21、Android SDK Platform 36 r02 和 Build Tools 36.0.0，无第三方应用运行依赖。SDK 从 Google 官方下载并按官方仓库清单校验。

设置 `CHECKER_JDK`、`CHECKER_BUILD_TOOLS`、`CHECKER_ANDROID_JAR` 后运行：

```sh
python3 build.py --tests
```

本地签名密钥位于 `signing/`，不包含在源码压缩包或 APK 内。后续更新应保留该密钥以便覆盖安装；不要将签名密钥公开。

`tests/` 是独立的临时设备验证包，不是交付应用的组成部分。它验证生产检测代码对已安装包、不存在包、查询被拒绝和实际 SystemHelper 状态的判断，不安装或恢复 SystemHelper。

## 参考

- Android 包可见性：https://developer.android.com/training/package-visibility/declaring
- PackageManager：https://developer.android.com/reference/android/content/pm/PackageManager

## 设备验证

小米 17（25113PN0EC）、Android 17 / OS4.0.5.0.XPCCNXM：安装启动通过，界面显示“未安装”，与系统用户 0 的 installed=false 一致。临时设备测试验证了已安装包、不存在包、查询异常及实际目标包四种情况。测试未恢复或重新安装 SystemHelper，也未改变其安装状态。其他机型尚未实机验证。
