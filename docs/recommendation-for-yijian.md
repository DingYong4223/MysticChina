# yijian 项目技术选型建议

> **文档版本**：v1.0  
> **更新日期**：2026-05-29  
> **项目背景**：KMP + KuiklyUI 跨平台视频剪辑 App，目标平台 Android/iOS/macOS

---

## 执行摘要

基于调研结论，yijian 的视频编辑技术栈推荐如下：

```
┌─────────────────────────────────────────────────────────────────┐
│                     yijian 技术选型总览                           │
├──────────────────────┬──────────────────────────────────────────┤
│  层次                 │  技术选型                                 │
├──────────────────────┼──────────────────────────────────────────┤
│  UI 层               │  KuiklyUI（已有）                          │
│  业务逻辑层           │  KMP commonMain（数据模型+时间轴+undo/redo）│
│  Android 编辑引擎     │  Jetpack Media3 Transformer（主）         │
│  Android GPU 渲染     │  OpenGL ES 3.0 + 自定义 GLSL 着色器       │
│  iOS 编辑引擎         │  AVFoundation（主）                        │
│  iOS GPU 渲染         │  Core Image + Metal                       │
│  LUT 滤镜（双平台）    │  512×512 PNG 纹理格式                     │
│  视频格式             │  H.264/H.265，MP4 容器                    │
│  AI 特效（三期）       │  TensorFlow Lite（Android）/ CoreML（iOS）│
└──────────────────────┴──────────────────────────────────────────┘
```

---

## 一、各功能域选型详解

### 1.1 视频裁剪（Trimming）

**优先策略：无重编码裁剪**

| 平台 | 方案 | 性能 | 精度 |
|------|------|------|------|
| Android | `MediaExtractor + MediaMuxer`（容器层裁剪）| < 1s | I 帧精度 |
| Android | `Media3 Transformer ClippingConfiguration` | ~3-8s | 帧精度 |
| iOS | `AVAssetExportSession + AVMutableComposition` | < 1s（passthrough）| I 帧精度 |

**建议**：
- 用户预览阶段：使用容器层快速裁剪（无损，极快）
- 最终导出：使用帧精度裁剪（如需精确帧）

```kotlin
// KMP 接口设计
interface IVideoEditor {
    // 快速裁剪（I 帧精度，无重编码）
    suspend fun quickTrim(input: String, startMs: Long, endMs: Long, output: String): Result<Unit>
    
    // 精确裁剪（帧精度，需重编码）
    suspend fun preciseTrim(input: String, startMs: Long, endMs: Long, output: String): Result<Unit>
}
```

---

### 1.2 LUT 滤镜

**方案选型：平台原生 GPU 着色器 + 512×512 PNG LUT 资源**

| 平台 | 方案 | 理由 |
|------|------|------|
| Android | OpenGL ES GLSL 着色器 | 性能最优，可与视频预览管线无缝集成 |
| iOS | Core Image `CIColorCube` / `CIColorCubeWithColorSpace` | Apple 官方，基于 Metal，200+ 内置滤镜 |

**LUT 资源规格**：
```
格式：512×512 PNG（8×8 排列，每格 64×64）
精度：64 级（标准精度，文件约 200-400KB）
管理：打包为 App 内置资源，支持云端动态下发
命名：<滤镜名>.png，例：film_vintage.png, cool_blue.png
```

**二期滤镜清单**（建议内置）：
```
基础调色类：正常、明亮、冷调、暖调、黑白
胶片类：复古、菲林、老照片
时尚类：小清新、高对比、低饱和
美食类：暖食、橙调
风景类：通透、蓝天
```

---

### 1.3 色彩调节

| 参数 | Android | iOS |
|------|---------|-----|
| 亮度（-100~100） | GLSL `uBrightness` uniform | `CIColorControls.inputBrightness` |
| 对比度（-100~100） | GLSL `uContrast` uniform | `CIColorControls.inputContrast` |
| 饱和度（-100~100） | GLSL HSV 转换 | `CIColorControls.inputSaturation` |
| 色温（冷/暖） | GLSL RGB 偏移 | `CITemperatureAndTint` |
| 色调（绿/洋红） | GLSL 色相旋转 | `CITemperatureAndTint` |
| 高光（-100~100） | GLSL 分段曲线 | `CIHighlightShadowAdjust` |
| 阴影（-100~100） | GLSL 分段曲线 | `CIHighlightShadowAdjust` |
| 锐化（0~100） | GLSL 拉普拉斯核 | `CISharpenLuminance` |
| 暗角（0~100） | GLSL 径向渐变 | `CIVignette` |

---

### 1.4 文字叠加

**技术方案**：

```
预览阶段：
  Android → Canvas 渲染 Bitmap → 上传为 GL 纹理 → 合成到视频帧
  iOS → CATextLayer / UILabel → 截图为 UIImage → CISourceOverCompositing

导出阶段：
  Android → OpenGL ES 合成到最终视频帧 → MediaCodec 编码
  iOS → AVVideoCompositionCoreAnimationTool + CATextLayer → AVAssetExportSession
```

**功能清单**：
- 字体选择（系统字体 + 内置字体包）
- 字号（12-120pt）
- 颜色（含渐变）
- 描边（颜色+宽度）
- 阴影（颜色+偏移+模糊）
- 透明度（0-100%）
- 动画（淡入/淡出/打字机/弹入）
- 时间范围（显示起止时间）

---

### 1.5 转场效果（三期）

| 转场类型 | 着色器复杂度 | 技术 |
|---------|-----------|------|
| 淡入淡出 | 低 | `mix(from, to, progress)` |
| 划入（左/右/上/下） | 低 | 坐标判断 |
| 缩放 | 中 | 矩阵变换 |
| 旋转翻页 | 高 | 3D 透视变换 |
| 光效扫过 | 高 | 渐变+高光遮罩 |
| 模糊过渡 | 中 | Gaussian Blur 过渡 |

---

### 1.6 音频处理

| 功能 | Android | iOS |
|------|---------|-----|
| 背景音乐 | MediaExtractor 提取 → 混音 | AVMutableAudioMix |
| 音量调节 | AudioTrack / MediaCodec | AVMutableAudioMixInputParameters |
| 音乐裁剪 | MediaExtractor + MediaMuxer | AVMutableComposition |
| 淡入淡出 | 关键帧音量 | setVolumeRamp |
| 变声（三期） | SoundTouch / Superpowered | AVAudioEngine |

---

### 1.7 视频导出

**配置建议**：

```kotlin
// KMP 导出配置
data class ExportConfig(
    val resolution: Resolution = Resolution.HD_1080P,
    val bitrate: Bitrate = Bitrate.HIGH,
    val frameRate: Int = 30,
    val codec: VideoCodec = VideoCodec.H264
) {
    enum class Resolution(val width: Int, val height: Int) {
        SD_720P(720, 1280),
        HD_1080P(1080, 1920),
        QHD_2K(1440, 2560),
        UHD_4K(2160, 3840)
    }
    
    enum class Bitrate(val bps: Int) {
        LOW(4_000_000),      // 4 Mbps
        MEDIUM(8_000_000),   // 8 Mbps（推荐）
        HIGH(16_000_000),    // 16 Mbps（高质量）
        LOSSLESS(-1)          // 最大码率
    }
    
    enum class VideoCodec { H264, H265 }
}
```

**导出时间参考**（中端设备）：

| 视频长度 | 分辨率 | 无特效 | 含 LUT 滤镜 | 含文字+转场 |
|---------|-------|--------|------------|-----------|
| 15秒 | 1080p | ~3s | ~5s | ~8s |
| 1分钟 | 1080p | ~10s | ~18s | ~30s |
| 3分钟 | 1080p | ~30s | ~55s | ~90s |
| 1分钟 | 4K | ~25s | ~45s | ~75s |

---

## 二、KMP 架构设计

### 目录结构

```
yijian/shared/src/
├── commonMain/kotlin/com/yijian/
│   ├── editor/                          # 编辑引擎（二期新增）
│   │   ├── VideoEditor.kt              # expect 接口
│   │   ├── EditTimeline.kt             # 时间轴数据模型
│   │   ├── VideoSegment.kt             # 片段数据模型
│   │   ├── TextOverlay.kt              # 文字叠加数据模型
│   │   ├── FilterEffect.kt             # 滤镜数据模型
│   │   ├── ExportConfig.kt             # 导出配置
│   │   └── EditHistory.kt             # undo/redo 历史
│   ├── pages/
│   │   ├── EditorPage.kt              # 编辑主页面（二期）
│   │   ├── FilterPage.kt              # 滤镜选择页面
│   │   └── TextEditorPage.kt          # 文字编辑页面
│   └── components/
│       ├── EditTimeline.kt             # 时间轴 UI 组件
│       ├── FilterGallery.kt            # 滤镜画廊组件
│       └── TextEditPanel.kt            # 文字编辑面板
│
├── androidMain/kotlin/com/yijian/
│   └── editor/
│       ├── AndroidVideoEditor.kt       # Media3 + OpenGL ES 实现
│       ├── GlFilterRenderer.kt         # OpenGL ES 渲染器
│       └── LutTextureLoader.kt         # LUT 纹理加载
│
└── iosMain/kotlin/com/yijian/
    └── editor/
        └── IOSVideoEditor.kt           # AVFoundation + CoreImage 实现
```

### 核心接口

```kotlin
// commonMain/editor/VideoEditor.kt
expect class VideoEditor(context: Any?) {
    
    // === 裁剪 ===
    suspend fun trimVideo(
        input: VideoSegment, 
        output: String
    ): EditorResult<Unit>
    
    // === 滤镜 ===
    suspend fun exportWithFilter(
        input: String,
        filter: FilterEffect?,
        colorAdjust: ColorAdjustment?,
        output: String,
        onProgress: (Float) -> Unit = {}
    ): EditorResult<Unit>
    
    // === 文字 ===
    suspend fun exportWithTextOverlays(
        input: String,
        textOverlays: List<TextOverlay>,
        output: String,
        onProgress: (Float) -> Unit = {}
    ): EditorResult<Unit>
    
    // === 完整导出 ===
    suspend fun exportTimeline(
        timeline: EditTimeline,
        config: ExportConfig,
        output: String,
        onProgress: (Float) -> Unit = {}
    ): EditorResult<ExportResult>
    
    // === 缩略图 ===
    suspend fun extractThumbnail(
        videoPath: String, 
        timeMs: Long, 
        width: Int = 200, 
        height: Int = 200
    ): ByteArray?
    
    fun release()
}

// 结果类型
sealed class EditorResult<T> {
    data class Success<T>(val data: T) : EditorResult<T>()
    data class Error<T>(val message: String, val cause: Throwable? = null) : EditorResult<T>()
}
```

---

## 三、迭代路线图

### 二期（编辑基础能力）

```
里程碑 1：视频裁剪
├── Android：Media3 Transformer ClippingConfiguration
├── iOS：AVMutableComposition + AVAssetExportSession
└── KMP 接口：VideoEditor.trimVideo()

里程碑 2：LUT 滤镜
├── Android：OpenGL ES GLSL 着色器
├── iOS：Core Image CIColorCube
├── 资源：内置 10 款 LUT，支持云端扩展
└── KMP 接口：VideoEditor.exportWithFilter()

里程碑 3：色彩调节
├── Android：OpenGL ES 着色器（亮度/对比度/饱和度等 8 项）
├── iOS：Core Image 滤镜链
└── KMP 接口：FilterEffect + ColorAdjustment 数据模型

里程碑 4：文字叠加
├── Android：Canvas Bitmap → GL 纹理合成
├── iOS：CATextLayer → AVVideoCompositionCoreAnimationTool
└── KMP 接口：VideoEditor.exportWithTextOverlays()
```

### 三期（高级功能）

```
多轨道时间轴编辑
转场效果（6 种基础转场）
音频处理（背景音乐、音量关键帧）
图层系统（贴纸、元素）
```

### 四期（AI 与导出优化）

```
AI 背景替换（TFLite + CoreML）
AI 美颜（人脸 Mesh + 滤镜）
视频超分（ESRGAN/Real-ESRGAN 移动版）
批量导出优化
```

---

## 四、风险与注意事项

### 4.1 License 风险

| 库 | License | 风险 |
|---|---------|------|
| Media3 Transformer | Apache 2.0 | ✅ 安全 |
| LiTr | BSD 2-Clause | ✅ 安全 |
| FFmpegKit（含 x264） | GPL 3.0 | ❌ 需开源，**避免使用** |
| FFmpegKit（LGPL 包） | LGPL 3.0 | ⚠️ 需动态链接，注意 |

### 4.2 设备兼容性

```
Android 最低要求：Android 6.0（API 23）
  - OpenGL ES 3.0：Android 4.3+ 几乎全覆盖
  - H.265 硬件编码：需要 Android 5.0+ 且芯片支持（需运行时检测）
  - 建议：H.264 作为默认，H.265 作为可选

iOS 最低要求：iOS 15（与 KuiklyUI 要求一致）
  - Metal：iOS 8+ 全覆盖
  - H.265：iPhone 7+（A10 芯片）
  - VideoToolbox 硬件：全 iOS 设备支持
```

### 4.3 性能与功耗

```
- 导出期间开启性能模式（Android 锁定 CPU/GPU 频率）
- 使用后台任务（WorkManager / BackgroundTask）
- 监控热量状态，必要时降质量保稳定
- 4K 导出：仅在充电时推荐
```

### 4.4 存储管理

```
- 临时文件：使用 cacheDir，用完即删
- 导出前检查存储空间（预估 = 码率 × 时长 × 1.2）
- 提供"草稿"机制：中间态持久化到 filesDir
```

---

## 五、开发资源

### 参考开源项目

| 项目 | 用途 | 链接 |
|------|------|------|
| LiTr Demo | Android 视频转码参考 | https://github.com/linkedin/LiTr |
| GPUImage Android | GLSL 滤镜着色器参考 | https://github.com/CyberAgent/android-gpuimage |
| GPUImage iOS | Metal/GLSL 滤镜参考 | https://github.com/BradLarson/GPUImage |
| Android-VideoEditor | 基础时间轴参考 | GitHub 搜索 |
| VideoCompositionExample | AVFoundation 合成示例 | Apple Developer |

### 工具

| 工具 | 用途 |
|------|------|
| Shadertoy | GLSL 着色器在线调试 |
| RenderDoc | Android GPU 渲染调试 |
| Xcode GPU Frame Capture | iOS Metal 调试 |
| FFmpeg 命令行 | LUT 格式转换、测试 |
| Lightroom | LUT 制作与导出 |

---

*文档最后更新：2026-05-29*
