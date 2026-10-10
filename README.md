# MysticChina（神秘中国）

首页保留 **探索 / 学习 / 关于**，每个 Tab 都继承 FulaCard 的 `ProtocolPage`，通过独立 `pageID` 加载卡片。底栏沿用 QMBang 的尺寸、选中图标与安全区布局。当前业务只保留文字书写游戏；视频导入、编辑、播放器、主题选择和协议演示页面已移除。

## 工程结构

- `shared/.../pages/HomePage.kt`：首页 Tab 容器，切换调用 `showProtocolPage`。
- `shared/src/commonMain/assets/common/protocol_pages/`：`explore.json`、`learn.json`、`about.json`，直接组合 FulaCard 的文本卡片、入口卡片。
- `shared/.../data/`：与 QMBang 相同的本地协议读取、后台 JSON → Protobuf 转换和 `ProtocolPageSource` 接入。
- `com.fula:fula-page` → `fula-card` → `fula-component`：协议渲染、卡片、基础组件由 FulaCard 提供，宿主不再维护重复实现。
- `shared/.../pages/HanziPage.kt`、`shared/.../hanzi/`、`shared/src/commonMain/assets/hanzi/`：原有游戏页面、桥接和完整游戏资源。
- `androidApp` / `iosApp`：注册汉字 WebView；协议入口 `mysticchina_hanzi` 由宿主路由适配为 `HanziPage`。

## FulaCard 本地联调

与 QMBang 一样，通过 `debug_aar_config.json` 控制依赖，业务代码始终声明 Maven 坐标。

| 配置 | `true` | `false` |
| --- | --- | --- |
| 顶层 `debug` | 联调 `../FulaCard` 的 `fula-component`、`fula-card`、`fula-page` 源码 | 使用 Maven / Maven Local 的 `com.fula:*:1.0.0` |
| `kuiklyui.debug` | 联调 `../KuiklyUI` 源码 | 使用 `kuiklyui.published_version`，默认 `2.28.0-2.1.21` |

默认两个开关均为 `true`，可直接在 FulaCard 修改组件、卡片和协议框架后重新构建本工程。目录位置变化时调整 `source_dir`。关闭顶层开关前，确保 FulaCard 已执行其 `publish-local.sh` 发布所需版本。

修改页面内容只需编辑三个协议 JSON；卡片入口的 `pageID` 保持 `mysticchina_hanzi`。原游戏 HTML、脚本、汉字数据与本地存储保持原样。

```sh
./gradlew :shared:testDebugUnitTest :androidApp:assembleDebug
./gradlew :shared:compileKotlinIosSimulatorArm64 :shared:podspec
python3 scripts/check-debug-aar.py
```

依赖组合检查脚本临时切换配置，并在结束时恢复原文件；运行时避免同时启动其他 Gradle 构建。

iOS 首次联调需在 `iosApp` 执行 `pod install`，再用 Xcode 打开 `iosApp.xcworkspace`。共享模块启用 `fulaPage` 子模块页面汇总，保留宿主 `shared.podspec`，禁止 KuiklyUI 源码依赖重新生成 Podspec。

## 验证边界

Android Debug APK、iOS Simulator ARM64 共享代码及四种源码 / Maven 依赖组合已检查。新增的两个 JVM 测试覆盖 Tab 协议、游戏路由和请求失败分支；iOS WebView 已做 Swift 类型检查。未启动应用，三个 Tab 的设备显示、返回导航和文字书写交互仍需验收；iOS 完整应用链接未验证。
