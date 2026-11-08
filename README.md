# 组件检查 · 二合一

无需 Root，离线检查当前 Android 用户是否安装以下两个组件：

| 显示名称 | 包名 |
|---|---|
| SystemHelper（联通终端管理组件） | `com.mobiletools.systemhelper` |
| RegService（移动终端管理组件） | `com.miui.dmregservice` |

从 [最新 Release](https://github.com/SeyanaA/systemhelper-checker/releases/latest) 下载 **Component-Checker-v1.1.0.apk**，安装后打开“组件检查”。v1.0 用户可直接覆盖升级，保留已有 SystemHelper 检查记录。

## 检测结果

- **未安装**：当前用户未登记安装。系统分区可能仍保留原始 APK，不算当前用户已安装。
- **已安装**：当前用户登记安装，停用或暂停也属于已安装。显示版本及启用状态，可打开系统详情页。
- **无法确认**：系统查询出错，不把查询异常当成已卸载。

两个组件分别显示结果，并各自保存最近确认未安装的时间。以后检测到该组件已安装，会提示恢复安装。首次检查就已安装时，无法据此推断安装时间。旧版单组件历史只归属于 SystemHelper，不会被误用于 RegService。

打开、返回应用或点击“重新检查两个组件”时更新结果。不持续在后台监控，不发送通知，不修改、卸载或恢复目标组件。检查记录仅保存在本机，清除应用数据或卸载本应用会丢失记录。

仅检查当前用户；手机分身、工作资料或其他用户请在相应空间检查。

## 权限与隐私

Manifest 仅通过 `<queries>` 声明上述两个包，无任何 `uses-permission`；无联网、广告、统计 SDK、后台服务或开机接收器。检测使用标准 PackageManager，不请求 `MATCH_UNINSTALLED_PACKAGES`，并核对 `ApplicationInfo.FLAG_INSTALLED`。

## 本机验证

v1.1.0 在小米 17（Android 17 / 澎湃 OS4）上验证两个目标均为未安装，与系统用户 0 的 `installed=false` 对照。设备测试同时覆盖已安装包、不存在包、查询异常、两个目标实际状态、旧版历史兼容及两个组件历史互不覆盖。测试不恢复或重新安装 SystemHelper、RegService。

v1.0 曾在小米平板 7 Pro（Android 16 / 澎湃 OS3）验证 SystemHelper 检测；二合一版本尚未在平板上验证。其他机型尚未实机验证。

## 构建

支持 Android 8.0 / API 26 起，目标 SDK 36。采用纯 Java / Android 原生界面，无第三方应用运行依赖。

准备 JDK 21、Android SDK Platform 36 r02 和 Build Tools 36.0.0，设置路径后运行：

```sh
export CHECKER_JDK=/path/to/jdk/Contents/Home
export CHECKER_BUILD_TOOLS=/path/to/android-sdk/build-tools/36.0.0
export CHECKER_ANDROID_JAR=/path/to/android-sdk/platforms/android-36/android.jar
python3 build.py --tests
```

生成正式 APK 和独立临时设备测试 APK，位于 `dist/`。不需要测试时运行 `python3 build.py`。

本地签名密钥位于 `signing/`，不包含在仓库、源码压缩包或 APK 内。后续更新应保留该密钥，以便覆盖安装。自行构建会生成自己的签名，不能直接覆盖本仓库发布的 APK。

## 参考

- [Android 包可见性](https://developer.android.com/training/package-visibility/declaring)
- [PackageManager](https://developer.android.com/reference/android/content/pm/PackageManager)
