# 一剪（yijian）项目方案文档

## 1. 项目概述

**项目名称**：一剪（Yijian）  
**定位**：一款基于 Kuikly 跨端框架的移动端视频剪辑应用，仿照剪映（CapCut）实现核心功能  
**技术栈**：Kuikly（KMM跨端UI框架） + Kotlin Multiplatform + 原生视频渲染  
**目标平台**：Android、iOS  
**开发语言**：Kotlin（跨平台共享代码）  

---

## 2. 技术架构

### 2.1 整体架构分层

```
┌─────────────────────────────────────────────┐
│               UI 层（Pager/ComposeView）      │
│   ┌───────────┬───────────┬───────────────┐  │
│   │ 视频预览    │ 播放控制   │ 媒体选择       │  │
│   │ (Preview)  │ (Player)  │ (MediaPicker) │  │
│   └───────────┴───────────┴───────────────┘  │
├─────────────────────────────────────────────┤
│            组件库层（KuiklyUI）                │
│   ┌───────────┬───────────┬───────────────┐  │
│   │ 导航栏     │ 按钮/图标  │ 进度条/滑块    │  │
│   │ VideoTab   │ 时间轴    │ 弹窗/菜单      │  │
│   └───────────┴───────────┴───────────────┘  │
├─────────────────────────────────────────────┤
│          KuiklyCore 跨平台运行框架            │
│   ┌───────────────────────────────────────┐  │
│   │ 响应式UI | FlexBox布局 | 动画引擎       │  │
│   │ Pager管理 | 路由 | 状态管理             │  │
│   └───────────────────────────────────────┘  │
├─────────────────────────────────────────────┤
│          平台桥接层（KuiklyBase）             │
│   ┌───────────────┬───────────────────────┐  │
│   │ KuiklyBase-   │ KuiklyBase-          │  │
│   │ components    │ platform             │  │
│   ├───────────────┼───────────────────────┤  │
│   │ KuiklyBase-   │ KuiklyBase-          │  │
│   │ kotlin        │ third-party          │  │
│   └───────────────┴───────────────────────┘  │
├─────────────────────────────────────────────┤
│         原生平台层                            │
│   ┌────────────────┬──────────────────────┐  │
│   │  Android平台    │    iOS平台            │  │
│   │  MediaCodec    │  AVFoundation        │  │
│   │  ExoPlayer     │  AVPlayer            │  │
│   │  SurfaceView   │  AVPlayerLayer       │  │
│   └────────────────┴──────────────────────┘  │
└─────────────────────────────────────────────┘
```

### 2.2 架构核心原则

1. **分层职责明确**：UI 层只关注界面描述，通过平台桥接层调用原生能力
2. **响应式数据流**：使用 Kuikly `observable` 响应式字段驱动 UI 自动更新
3. **组件化封装**：视频播放器、时间轴、控制面板等封装为独立 ComposeView
4. **桥接模式**：通过 Kuikly 的 `expand-native-api` 扩展原生视频播放能力

### 2.3 依赖库关系

- **KuiklyCore**：核心跨端框架（UI DSL、响应式、布局、动画）
- **KuiklyUI**：通用 UI 组件库（导航栏、弹窗、进度条等）
- **KuiklyBase-components**：基础跨端组件封装
- **KuiklyBase-kotlin**：Kotlin 跨平台工具库（集合、协程、序列化）
- **KuiklyBase-platform**：平台抽象层（文件I/O、网络、存储）
- **KuiklyUI-third-party**：第三方库集成封装

---

## 3. 功能规划

### 3.1 总功能矩阵

| 模块 | 功能 | 优先级 | 阶段 |
|------|------|--------|------|
| 视频导入 | 本地视频选择、相册访问 | P0 | 一期 |
| 视频预览 | 全屏预览、横竖屏适配 | P0 | 一期 |
| 播放控制 | 播放/暂停、进度拖动、音量控制 | P0 | 一期 |
| 媒体库 | 视频列表展示、缩略图加载 | P0 | 一期 |
| 视频裁剪 | 时间范围裁剪、片段分割 | P1 | 二期 |
| 滤镜特效 | LUT滤镜、色彩调节 | P1 | 二期 |
| 文字字幕 | 添加文字、字幕轨道 | P1 | 二期 |
| 音频编辑 | 背景音乐、音量调整 | P1 | 三期 |
| 转场效果 | 片段间转场动画 | P2 | 三期 |
| 时间轴编辑 | 多轨道时间轴、精准控制 | P2 | 三期 |
| 导出分享 | 视频导出、分辨率选择 | P2 | 三期 |

### 3.2 一期功能详情

**P0 核心功能** —— 视频预览与播放

- [x] 视频列表/媒体库页面
- [x] 视频选择与加载
- [x] 全屏视频预览页面
- [x] 播放/暂停控制
- [x] 进度显示与拖动
- [x] 播放完成循环/停止
- [x] 基础播放器UI（控制栏、进度条）

---

## 4. 项目结构

```
yijian/
├── shared/                          # KMM 跨平台共享模块
│   ├── src/
│   │   ├── commonMain/
│   │   │   ├── kotlin/com/yijian/
│   │   │   │   ├── App.kt              # 应用入口/路由
│   │   │   │   ├── theme/              # 主题系统
│   │   │   │   │   ├── Theme.kt        # 颜色、字体、间距
│   │   │   │   │   └── Colors.kt       # 色彩定义
│   │   │   │   ├── components/         # 通用 UI 组件
│   │   │   │   │   ├── TopBar.kt       # 顶部导航栏
│   │   │   │   │   ├── BottomBar.kt    # 底部控制栏
│   │   │   │   │   ├── ProgressBar.kt  # 进度条组件
│   │   │   │   │   ├── IconButton.kt   # 图标按钮
│   │   │   │   │   └── VideoThumbnail.kt # 视频缩略图
│   │   │   │   ├── pages/             # 页面（Pager）
│   │   │   │   │   ├── MainPage.kt     # 主页（媒体库）
│   │   │   │   │   ├── PreviewPage.kt  # 视频预览页
│   │   │   │   │   └── SplashPage.kt   # 启动页
│   │   │   │   ├── player/            # 视频播放器
│   │   │   │   │   ├── VideoPlayerView.kt  # 播放器UI组件
│   │   │   │   │   ├── PlayerController.kt # 播放控制器
│   │   │   │   │   └── PlayerState.kt      # 播放状态模型
│   │   │   │   ├── model/             # 数据模型
│   │   │   │   │   ├── VideoInfo.kt    # 视频信息
│   │   │   │   │   └── MediaItem.kt   # 媒体条目
│   │   │   │   ├── bridge/            # 原生桥接
│   │   │   │   │   ├── VideoPlayerBridge.kt # 播放器桥接接口
│   │   │   │   │   └── FileBridge.kt   # 文件访问桥接
│   │   │   │   └── util/              # 工具类
│   │   │   │       ├── FormatUtil.kt   # 时间格式化等
│   │   │   │       └── Constants.kt   # 常量定义
│   │   │   └── resources/             # 共享资源
│   │   ├── androidMain/               # Android 平台实现
│   │   │   └── kotlin/com/yijian/
│   │   │       ├── player/
│   │   │       │   └── AndroidVideoPlayer.kt
│   │   │       └── bridge/
│   │   │           └── AndroidFileBridge.kt
│   │   ├── iosMain/                   # iOS 平台实现
│   │   │   └── kotlin/com/yijian/
│   │   │       ├── player/
│   │   │       │   └── IOSVideoPlayer.kt
│   │   │       └── bridge/
│   │   │           └── IOSFileBridge.kt
│   └── build.gradle.kts
├── androidApp/                       # Android 宿主工程
│   ├── src/main/
│   └── build.gradle.kts
├── iosApp/                           # iOS 宿主工程
│   ├── iosApp/
│   ├── Podfile
│   └── ...
├── build.gradle.kts                  # 根构建文件
├── settings.gradle.kts
├── gradle.properties
├── gradle/
├── gradlew
├── gradlew.bat
└── docs/                             # 项目文档
    ├── ARCHITECTURE.md               # 本架构文档
    └── PLAN.md                       # 计划文档
```

---

## 5. 视频播放器设计

### 5.1 架构

```
┌───────────────────────────────────────────┐
│          VideoPlayerView (ComposeView)      │
│  ┌───────────────────────────────────────┐ │
│  │         视频画面渲染区域                 │ │
│  │  (Android: SurfaceView / iOS: AVLayer) │ │
│  └───────────────────────────────────────┘ │
│  ┌───────────────────────────────────────┐ │
│  │         播放控制覆盖层                   │ │
│  │  ┌─────────┬──────────┬──────────┐   │ │
│  │  │ 播放/暂停 │ 进度条    │ 时间显示  │   │ │
│  │  └─────────┴──────────┴──────────┘   │ │
│  └───────────────────────────────────────┘ │
└───────────────────────────────────────────┘
         │                     │
         ▼                     ▼
  ┌──────────────┐   ┌──────────────────┐
  │ Android      │   │ iOS              │
  │ ExoPlayer    │   │ AVPlayer         │
  │ MediaPlayer  │   │ AVPlayerLayer    │
  └──────────────┘   └──────────────────┘
```

### 5.2 播放状态机

```
        ┌──────────┐
        │  IDLE    │
        └────┬─────┘
             │ loadVideo()
             ▼
        ┌──────────┐
        │ LOADING  │ ◄────┐
        └────┬─────┘      │
             │ onPrepared │
             ▼            │
        ┌──────────┐      │
        │  READY   │      │
        └────┬─────┘      │
             │ play()     │
             ▼            │
        ┌──────────┐      │
        │ PLAYING  ├──────┤
        └────┬─────┘      │
             │ pause()    │ loop
             ▼            │
        ┌──────────┐      │
        │ PAUSED   ├──────┘
        └────┬─────┘
             │ stop()
             ▼
        ┌──────────┐
        │  STOPPED │
        └──────────┘
```

### 5.3 平台桥接接口

```kotlin
// 统一播放器接口
interface IVideoPlayer {
    fun loadVideo(path: String)
    fun play()
    fun pause()
    fun seekTo(position: Long)
    fun setVolume(volume: Float)
    fun release()
    
    // 回调
    var onPrepared: (() -> Unit)?
    var onProgress: ((current: Long, duration: Long) -> Unit)?
    var onCompletion: (() -> Unit)?
    var onError: ((String) -> Unit)?
}
```

---

## 6. UI/UX 设计

### 6.1 剪映风格参考

- **主色调**：深色主题（#1A1A1A 背景），强调色使用亮蓝/品红渐变
- **布局**：沉浸式全屏预览 + 半透明控制栏叠加
- **交互**：点击切换控制栏显隐，手势滑动调节进度
- **图标**：简洁线性图标风格

### 6.2 一期页面流程

```
启动页 (SplashPage)
    │
    ▼
主页 (MainPage) 
├── 顶部：应用标题 + 设置按钮
├── 中部：视频列表（Grid/List）
│   └── 点击视频项
└── 底部：导入按钮
         │
         ▼
预览页 (PreviewPage)
├── 全屏视频画面
├── 叠加半透明控制栏
│   ├── 左上：返回按钮
│   ├── 底部中央：播放/暂停按钮
│   ├── 进度条（可拖动）
│   └── 时间显示
└── 点击画面：切换控制栏显隐
```

---

## 7. 关键技术决策

| 决策 | 方案 | 理由 |
|------|------|------|
| 跨端框架 | Kuikly | 原生性能、Kotlin DSL、动态化支持 |
| 视频播放 | 原生桥接（ExoPlayer/AVPlayer） | Kuikly无内置Video组件，需扩展原生API |
| 布局规范 | FlexBox | Kuikly默认布局系统 |
| 状态管理 | Kuikly observable | 框架原生响应式支持 |
| 主题系统 | ComposeView封装 | 统一管理颜色、字体、间距 |
| 图标方案 | Base64内嵌或资源引用 | 跨平台兼容 |

---

## 8. 风险与缓解

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Kuikly无内置Video组件 | 需要自建播放器桥接 | 通过 expand-native-api 扩展 |
| 外部库无法访问 | 部分组件需自建 | 基于KuiklyCore基础组件开发 |
| 视频性能 | 长视频卡顿 | 使用平台原生播放器、异步加载 |
| 跨平台一致性问题 | Android/iOS表现差异 | 统一接口抽象层 |

---

*文档版本：v1.0*  
*最后更新：2026-05-28*
