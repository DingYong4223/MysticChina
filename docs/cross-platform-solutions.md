# 跨平台视频处理方案

> **文档版本**：v1.0  
> **更新日期**：2026-05-29  

---

## 概述

跨平台视频处理方案试图在多平台共享视频处理代码，以降低开发成本。但视频处理本质上是平台敏感的操作（依赖硬件编解码器、GPU API、系统框架），因此跨平台方案往往在性能或功能上存在折衷。

---

## 一、方案全景对比

| 方案 | 平台 | 状态 | 性能 | License | 推荐度 |
|------|------|------|------|---------|-------|
| **FFmpegKit** | Android + iOS | ⚠️ 已停止维护 | 中（软件编解码） | LGPL/GPL | ⭐⭐ |
| **OpenCV** | 全平台 | ✅ 活跃 | 中 | Apache 2.0 | ⭐⭐⭐ |
| **GStreamer** | 全平台 | ✅ 活跃 | 高（插件化） | LGPL | ⭐⭐ |
| **FFmpeg 原生** | 全平台 | ✅ 活跃 | 高（但构建复杂） | LGPL/GPL | ⭐⭐⭐ |
| **KMP expect/actual + 原生** | KMP | ✅ 推荐 | 最高（原生 HW） | 自定义 | ⭐⭐⭐⭐⭐ |
| **React Native Video** | RN 框架 | ✅ | 中 | MIT | ⭐⭐ |
| **Flutter Video Editor** | Flutter | ✅ | 中 | MIT | ⭐⭐ |

---

## 二、FFmpegKit（已停止维护）

### 状态警告
> ⚠️ **FFmpegKit 已于 2025 年正式停止维护**，GitHub 仓库已归档。最后发布版本为 v6.0（2023年8月，基于 FFmpeg 6.0）。建议不在新项目中使用，已有项目可考虑社区 Fork。

### 历史特性（供参考）

```
支持平台：Android（API 24+）、iOS（iOS 12.1+）
框架集成：Flutter、React Native、Cordova
License：
  - 默认包：LGPL 3.0（可闭源商业使用）
  - GPL 包（含 x264/x265）：GPL 3.0（需开源）
最后版本：v6.0（FFmpeg 6.0 基础）
```

### 核心问题

1. **性能差**：大多数操作使用软件编解码，不使用 MediaCodec/VideoToolbox 硬件加速
   - 1080p 30s 视频转码：Android MediaCodec 约 10s，FFmpegKit 约 60-90s
   
2. **License 风险**：GPL 包含 x264/x265 时要求开源
   
3. **包体积大**：完整 FFmpeg 库体积约 20-40MB（各架构）

4. **已停维**：安全漏洞无人修复

### 替代方案
- Android：Media3 Transformer / LiTr
- iOS：AVFoundation
- 格式转换（确需 FFmpeg）：手动编译 FFmpeg 或使用社区维护的 Fork

---

## 三、OpenCV（图像处理为主）

### 特点
- **优势**：丰富的计算机视觉算法（人脸检测、目标追踪等）
- **劣势**：主要针对图像处理，视频支持通过 VideoCapture 实现，不适合视频编辑流程

### 移动端集成

```kotlin
// Android：通过 openCV4Android SDK
// 添加依赖
implementation("org.opencv:opencv:4.9.0")

// 基本视频读取
val cap = VideoCapture(videoPath)
val frame = Mat()
while (cap.read(frame)) {
    // 处理每帧
    Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY)
}
```

### 适用场景
- ✅ AI 特效中的前景检测、人脸识别预处理
- ✅ 自定义图像滤镜算法
- ❌ 不适合作为视频编辑主管线

---

## 四、GStreamer

### 特点
- 插件化架构，功能极其强大
- 主要用于桌面/服务器流媒体处理
- **移动端支持有限**，配置复杂

### 移动端现状
- Android：官方提供 GStreamer Android 支持包，但集成复杂
- iOS：有 Framework 但维护不活跃
- 体积大（50MB+）

### 结论
> **不适合** yijian 这类移动视频编辑 App。

---

## 五、KMP expect/actual 模式（强烈推荐）

### 核心理念

不尝试共享视频处理代码，而是共享**数据模型**和**业务逻辑**，视频处理的具体实现交给各平台原生 API。

```
┌─────────────────────────────────────────────────────────────────┐
│                      KMP 分层策略                                │
├──────────────────────────────────────────────────────────────────┤
│  共享层（commonMain）                                             │
│  ├── 数据模型：VideoClip, EditTimeline, ExportConfig             │
│  ├── 业务逻辑：时间轴管理, 编辑指令序列, undo/redo               │
│  ├── expect 接口声明：VideoProcessor, ThumbnailGenerator         │
│  └── UI 层（KuiklyUI）：所有页面、组件                           │
├────────────────────────┬─────────────────────────────────────────┤
│  androidMain           │  iosMain                                │
│  actual VideoProcessor │  actual VideoProcessor                  │
│  → Media3 Transformer  │  → AVFoundation                         │
│  → LiTr                │  → VideoToolbox                         │
│  → OpenGL ES           │  → Core Image / Metal                   │
└────────────────────────┴─────────────────────────────────────────┘
```

### 接口设计示例

```kotlin
// commonMain/VideoProcessor.kt
expect class VideoProcessor(context: Any?) {
    
    // 视频裁剪
    suspend fun trimVideo(
        inputPath: String,
        startMs: Long,
        endMs: Long,
        outputPath: String
    ): Result<Unit>
    
    // LUT 滤镜导出
    suspend fun applyFilter(
        inputPath: String,
        lutAssetName: String,
        intensity: Float,
        outputPath: String
    ): Result<Unit>
    
    // 合并多段视频
    suspend fun mergeVideos(
        segments: List<VideoSegment>,
        outputPath: String
    ): Result<Unit>
    
    // 导出
    suspend fun exportVideo(
        inputPath: String,
        options: ExportOptions,
        onProgress: (Float) -> Unit,
        outputPath: String
    ): Result<ExportResult>
    
    // 生成缩略图
    suspend fun generateThumbnail(
        videoPath: String,
        timeMs: Long
    ): ByteArray?
}

// 共享数据模型
data class VideoSegment(
    val path: String,
    val startMs: Long,
    val endMs: Long,
    val speed: Float = 1.0f,
    val volume: Float = 1.0f,
    val filterName: String? = null
)

data class ExportOptions(
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val fps: Int,
    val format: VideoFormat = VideoFormat.MP4
)
```

### Android actual 实现骨架

```kotlin
// androidMain/VideoProcessor.kt
actual class VideoProcessor actual constructor(context: Any?) {
    
    private val ctx = context as Context
    
    actual suspend fun trimVideo(
        inputPath: String, startMs: Long, endMs: Long, outputPath: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val transformer = Transformer.Builder(ctx).build()
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.fromFile(File(inputPath)))
                .setClippingConfiguration(
                    ClippingConfiguration.Builder()
                        .setStartPositionMs(startMs)
                        .setEndPositionMs(endMs)
                        .build()
                )
                .build()
            
            suspendCoroutine { continuation ->
                transformer.start(EditedMediaItem.Builder(mediaItem).build(), outputPath)
                transformer.addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: Composition, result: ExportResult) {
                        continuation.resume(Result.success(Unit))
                    }
                    override fun onError(composition: Composition, result: ExportResult, e: ExportException) {
                        continuation.resume(Result.failure(e))
                    }
                })
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### iOS actual 实现骨架（通过 Objective-C 桥接）

```kotlin
// iosMain/VideoProcessor.kt
actual class VideoProcessor actual constructor(context: Any?) {
    
    actual suspend fun trimVideo(
        inputPath: String, startMs: Long, endMs: Long, outputPath: String
    ): Result<Unit> = withContext(Dispatchers.Main) {
        // 调用 Kotlin/Native 与 Swift 互操作
        // 通过 iOS 平台桥接层调用 AVFoundation
        val bridge = YijianVideoBridge()  // Swift 类暴露给 Kotlin
        return bridge.trimVideo(inputPath, startMs, endMs, outputPath)
    }
}
```

---

## 六、竞品技术架构参考

### CapCut / 剪映（字节跳动）

```
架构模式：各平台独立原生实现，UI 层部分共享

Android 端：
  编解码：MediaCodec（硬件）
  渲染：自研 GPU 渲染引擎（OpenGL ES 3.0/Vulkan）
  特效：自研着色器 + 预编译特效包（.bundle 格式）
  AI：TensorFlow Lite（人脸、背景分离）
  字体：FreeType 自渲染

iOS 端：
  编解码：VideoToolbox（硬件）
  渲染：Metal 渲染管线
  特效：Metal 着色器 + 效果参数包
  AI：CoreML + Vision 框架

共享部分：
  效果配置格式（JSON）、贴纸/字体资源包
  云端特效分发协议
```

### InShot / 剪辑宝

```
主要依赖：FFmpegKit（部分版本）+ 原生 API 混合
导出：FFmpeg 软件编码（性能弱于原生）
滤镜：OpenGL ES（Android）/ Core Image（iOS）
```

### VN（巧影）

```
编解码：原生 MediaCodec / VideoToolbox
渲染：自研 OpenGL ES / Metal 引擎
多轨道：自研时间轴引擎
```

---

## 七、结论与建议

### 对于 yijian 项目

| 方案 | 结论 |
|------|------|
| FFmpegKit | ❌ 不推荐（已停维，性能差） |
| 纯跨平台库 | ❌ 不存在高质量选项 |
| KMP expect/actual + 原生 | ✅ **强烈推荐**（最优性能，最好维护性） |
| OpenCV | ✅ 补充使用（AI 特效相关） |

### 最终策略

```
1. 核心视频处理：各平台原生 API（Android: Media3/MediaCodec，iOS: AVFoundation）
2. 共享层：KMP 接口定义 + 数据模型 + 业务逻辑
3. UI 层：KuiklyUI 完全共享
4. 特效渲染：Android OpenGL ES + iOS Metal/Core Image
5. AI 特效（三期+）：Android TFLite + iOS CoreML
```
