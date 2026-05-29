# Android 视频处理 API 详解

> **文档版本**：v1.0  
> **更新日期**：2026-05-29  

---

## 概述

Android 提供了从低层到高层的完整视频处理 API 栈，核心是 **MediaCodec**（硬件编解码）+ **OpenGL ES**（GPU 渲染）。自 API 29 起，Google 推出 **Media3 Transformer** 作为官方推荐的高层视频编辑 API。

---

## 一、MediaCodec（核心低层 API）

### 简介
Android 最底层的视频编解码接口，直接调用硬件编解码器（Qualcomm/MediaTek/Exynos 等芯片的 Video Engine）。

### 支持的视频编解码格式

| 编解码格式 | MIME 类型 | 编码 | 解码 | 备注 |
|-----------|----------|------|------|------|
| H.264/AVC | `video/avc` | ✅ | ✅ | 所有设备必须支持 |
| H.265/HEVC | `video/hevc` | ✅ | ✅ | API 21+，4K 推荐 |
| VP8 | `video/x-vnd.on2.vp8` | ✅ | ✅ | WebM 容器 |
| VP9 | `video/x-vnd.on2.vp9` | ✅ | ✅ | 更好压缩率 |
| AV1 | `video/av01` | ✅（API 34+） | ✅（API 29+） | 最新格式，更高压缩率 |

### 硬件 vs 软件编解码器

```
• 命名规则：
  - 硬件：OMX.<vendor>.video.* 或 c2.<vendor>.*
    例：OMX.qcom.video.encoder.avc（高通）
        c2.mtk.avc.encoder（联发科）
  - 软件：OMX.google.* 或 c2.android.*
    例：c2.android.avc.encoder

• 性能对比：
  - 硬件编码：快 5-10x，低功耗，适合长视频导出
  - 软件编码：兼容性好，但 CPU 密集，发热严重
```

### 典型使用模式

```kotlin
// Android 视频转码关键流程
class VideoTranscoder {
    
    // 1. 解码：MediaExtractor 读取 → MediaCodec 解码 → Surface/ByteBuffer
    fun setupDecoder(format: MediaFormat): MediaCodec {
        val codec = MediaCodec.createDecoderByType("video/avc")
        codec.configure(format, outputSurface, null, 0)
        codec.start()
        return codec
    }
    
    // 2. 编码：ByteBuffer/Surface → MediaCodec 编码 → MediaMuxer 写入
    fun setupEncoder(width: Int, height: Int, bitrate: Int): MediaCodec {
        val format = MediaFormat.createVideoFormat("video/avc", width, height).apply {
            setInteger(MediaFormat.KEY_BIT_RATE, bitrate)
            setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
        }
        val codec = MediaCodec.createEncoderByType("video/avc")
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        codec.start()
        return codec
    }
}
```

### 适用场景
- 自定义视频转码管线
- 与 OpenGL ES 集成实现实时滤镜
- 低延迟视频处理

---

## 二、Jetpack Media3 Transformer（官方推荐）

### 简介
Google 2022 年推出的官方视频编辑 API，构建于 MediaCodec 之上，提供声明式的高层接口。

### 核心功能

| 功能 | 支持状态 |
|------|---------|
| 视频裁剪（Trimming） | ✅ |
| 视频裁切（Cropping） | ✅ |
| 视频转码 | ✅ |
| 自定义视频特效 | ✅ |
| 自定义音频特效 | ✅ |
| 多素材合成（Composition） | ✅ |
| HDR 色调映射 | ✅ |
| 分辨率/码率控制 | ✅ |

### 系统要求
- **最低 SDK**：Android 6.0（API 23）
- **内部实现**：MediaCodec 硬件加速 + OpenGL ES 渲染

### 关键 API

```kotlin
// 基本使用
val transformer = Transformer.Builder(context)
    .addListener(object : Transformer.Listener {
        override fun onCompleted(composition: Composition, exportResult: ExportResult) {
            // 导出完成
        }
        override fun onError(composition: Composition, exportResult: ExportResult, 
                            exportException: ExportException) {
            // 错误处理
        }
    })
    .build()

// 单个素材编辑
val effects = Effects(
    audioProcessors = listOf(),
    videoEffects = listOf(
        RgbAdjustment.Builder().setRedScale(1.2f).build(),  // 色彩调节
        ScaleAndRotateTransformation.Builder().setRotationDegrees(90f).build()  // 旋转
    )
)

val editedItem = EditedMediaItem.Builder(MediaItem.fromUri(videoUri))
    .setRemoveAudio(false)
    .setEffects(effects)
    .setClippingConfiguration(
        ClippingConfiguration.Builder()
            .setStartPositionMs(1000)   // 裁剪起点 1s
            .setEndPositionMs(5000)     // 裁剪终点 5s
            .build()
    )
    .build()

// 执行转换
transformer.start(editedItem, outputPath)

// 多素材合成
val composition = Composition.Builder(
    ImmutableList.of(
        EditedMediaItemSequence(videoClip1),
        EditedMediaItemSequence(videoClip2)
    )
).build()
transformer.start(composition, outputPath)
```

### 优劣分析

**优点：**
- ✅ Google 官方支持，长期维护
- ✅ 硬件加速，性能优良
- ✅ API 简洁，降低开发复杂度
- ✅ 与 ExoPlayer/Media3 生态深度集成
- ✅ 持续更新特性（HDR、AV1 等）

**缺点：**
- ⚠️ 功能相对有限（相比 FFmpeg）
- ⚠️ 不支持跨平台（Android 专用）
- ⚠️ API 仍在演进中（部分 API 标注为实验性）

---

## 三、LiTr（LinkedIn 开源库）

### 简介
LinkedIn 开源的 Android 硬件加速视频/音频转换库，BSD 2-Clause 开源协议。

- **GitHub**：https://github.com/linkedin/LiTr
- **最新版本**：v1.5.7（2024年8月）
- **License**：BSD 2-Clause（商业友好）

### 架构特点

```
┌──────────────────────────────────────────────────────┐
│                    LiTr 模块化架构                     │
├──────────┬──────────┬──────────┬──────────┬──────────┤
│MediaSource│ Decoder  │ Renderer │ Encoder  │MediaTarget│
│（读取）   │（解码）  │（GPU渲染）│（编码）  │（写入）   │
└──────────┴──────────┴──────────┴──────────┴──────────┘
     每个模块均可替换为自定义实现
```

### 核心功能

- **视频/音频转码**：支持分辨率、码率、采样率配置
- **GPU 滤镜**：内置 40+ 滤镜，基于 OpenGL ES
- **水印叠加**：Bitmap 水印，支持自定义着色器
- **Track 操作**：Mux/Demux，视频裁剪，单独 Track 处理
- **Camera2 录制支持**
- **WAV 音频导出**

### 典型代码

```kotlin
// LiTr 视频转码 + 水印
val transcoder = MediaTransformer(applicationContext)

transcoder.transform(
    requestId,
    sourceVideoUri,
    targetVideoFilePath,
    targetVideoFormat,
    targetAudioFormat,
    MediaTransformationListener { id, progress, mediaTarget ->
        // 进度回调
    },
    MediaTransformer.GRANULARITY_DEFAULT,
    listOf(
        GlFilterTransform(WatermarkOverlayFilter(watermarkBitmap))
    )
)
```

---

## 四、MediaExtractor & MediaMuxer

视频处理的基础工具类。

```kotlin
// MediaExtractor：读取视频轨道
val extractor = MediaExtractor()
extractor.setDataSource(videoPath)

// 找到视频轨道
for (i in 0 until extractor.trackCount) {
    val format = extractor.getTrackFormat(i)
    if (format.getString(MediaFormat.KEY_MIME)!!.startsWith("video/")) {
        extractor.selectTrack(i)
        break
    }
}

// MediaMuxer：写入 MP4
val muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
val trackIndex = muxer.addTrack(format)
muxer.start()
// 写入数据帧...
muxer.stop()
muxer.release()
```

**重要：** 对于仅裁剪（不重新编码），可以使用 MediaExtractor + MediaMuxer 直接复制 I 帧，速度极快（无需转码）。

---

## 五、OpenGL ES（GPU 渲染）

用于在视频帧上进行 GPU 加速的实时特效处理。

```kotlin
// OpenGL ES 渲染管线核心
class VideoRenderer : GLSurfaceView.Renderer {
    private var textureId: Int = 0
    private var program: Int = 0
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // 创建外部纹理（接收视频帧）
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureId = textures[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
        
        // 编译着色器
        program = createShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)
    }
    
    // LUT 滤镜着色器示例
    val FRAGMENT_SHADER = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;  // 视频帧
        uniform sampler2D lutTexture;          // LUT 纹理
        varying vec2 vTextureCoord;
        
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            // 应用 LUT
            float r = color.r * (63.0/64.0) + (0.5/64.0);
            float g = color.g * (63.0/64.0) + (0.5/64.0);
            float b = color.b * (63.0/64.0) + (0.5/64.0);
            float blue_slice = floor(b * 64.0);
            float x = (mod(blue_slice, 8.0) + r) / 8.0;
            float y = (floor(blue_slice / 8.0) + g) / 8.0;
            gl_FragColor = texture2D(lutTexture, vec2(x, y));
        }
    """.trimIndent()
}
```

---

## 六、方案对比总结

| 特性 | Media3 Transformer | MediaCodec 直接使用 | LiTr | FFmpegKit（停维） |
|------|-------------------|-------------------|------|-----------------|
| 开发难度 | 低 ⭐⭐⭐⭐⭐ | 高 ⭐⭐ | 中 ⭐⭐⭐⭐ | 低 ⭐⭐⭐⭐⭐ |
| 性能（硬件加速） | ✅ | ✅ | ✅ | ⚠️（软件为主） |
| 功能完整性 | 中 | 高 | 中高 | 高 |
| License | Apache 2.0 | Android SDK | BSD 2-Clause | LGPL/GPL |
| 维护状态 | ✅ Google 官方 | ✅ | ✅（活跃） | ❌ 已归档 |
| 最低 API | 23 | 21 | 18 | 24 |
| 跨平台 | Android 专用 | Android 专用 | Android 专用 | iOS + Android |

---

## 七、推荐架构（Android 端）

```
应用层
   │
   ▼
KMP expect/actual 接口（VideoProcessor）
   │
   ▼
Android 实现层
   ├── 高层编辑：Media3 Transformer
   │     ├── 裁剪、转码、合成
   │     └── 色彩调节等基础特效
   │
   ├── GPU 特效：OpenGL ES 着色器
   │     ├── LUT 滤镜
   │     ├── 自定义特效
   │     └── 文字/贴纸叠加
   │
   ├── 精细控制：MediaCodec
   │     ├── 自定义编码参数
   │     └── 帧级别处理
   │
   └── 工具类：MediaExtractor + MediaMuxer
         ├── 快速裁剪（无需重编码）
         └── 缩略图提取
```

---

## 参考链接

- [Android Media3 Transformer](https://developer.android.com/media/media3/transformer)
- [Android MediaCodec](https://developer.android.com/reference/android/media/MediaCodec)
- [LiTr GitHub](https://github.com/linkedin/LiTr)
- [Android GPUImage](https://github.com/CyberAgent/android-gpuimage)
