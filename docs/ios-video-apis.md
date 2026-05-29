# iOS/macOS 视频处理 API 详解

> **文档版本**：v1.0  
> **更新日期**：2026-05-29  

---

## 概述

Apple 平台提供了分层次的视频处理 API 体系：**AVFoundation** 负责高层编辑与导出，**VideoToolbox** 提供低层硬件编解码，**Core Image + Metal** 实现 GPU 渲染特效，**CoreMedia** 处理时间与帧数据。

```
┌─────────────────────────────────────────────────────────┐
│                   iOS 视频处理 API 层次                   │
├─────────────────────────────────────────────────────────┤
│  AVFoundation（高层：编辑、合成、导出）                    │
├─────────────────────────────────────────────────────────┤
│  VideoToolbox（中层：硬件编解码）                          │
├─────────────────────────────────────────────────────────┤
│  Core Image + Metal（GPU 渲染特效）                       │
├─────────────────────────────────────────────────────────┤
│  CoreMedia + CoreVideo（底层：帧数据、时间处理）            │
└─────────────────────────────────────────────────────────┘
```

---

## 一、AVFoundation（核心编辑 API）

### 1.1 多轨道合成

```swift
// 创建可编辑合成
let composition = AVMutableComposition()

// 添加视频轨道
let videoTrack = composition.addMutableTrack(
    withMediaType: .video,
    preferredTrackID: kCMPersistentTrackID_Invalid
)!

// 插入视频片段（裁剪）
let asset = AVAsset(url: videoURL)
let sourceTrack = asset.tracks(withMediaType: .video).first!
let timeRange = CMTimeRange(
    start: CMTime(seconds: 2.0, preferredTimescale: 600),
    duration: CMTime(seconds: 5.0, preferredTimescale: 600)
)
try videoTrack.insertTimeRange(timeRange, of: sourceTrack, at: .zero)

// 添加音频轨道
let audioTrack = composition.addMutableTrack(
    withMediaType: .audio,
    preferredTrackID: kCMPersistentTrackID_Invalid
)!
try audioTrack.insertTimeRange(timeRange, of: asset.tracks(withMediaType: .audio).first!, at: .zero)
```

### 1.2 视频特效（VideoComposition）

```swift
// 构建视频合成指令（控制变换、透明度、裁切等）
let videoComposition = AVMutableVideoComposition()
videoComposition.frameDuration = CMTime(value: 1, timescale: 30)  // 30fps
videoComposition.renderSize = CGSize(width: 1080, height: 1920)   // 输出尺寸

let instruction = AVMutableVideoCompositionInstruction()
instruction.timeRange = CMTimeRange(start: .zero, duration: composition.duration)

let layerInstruction = AVMutableVideoCompositionLayerInstruction(assetTrack: videoTrack)
// 设置变换（缩放/旋转/裁切）
layerInstruction.setTransform(CGAffineTransform(rotationAngle: .pi / 2), at: .zero)
// 设置透明度
layerInstruction.setOpacity(0.8, at: CMTime(seconds: 1.0, preferredTimescale: 600))

instruction.layerInstructions = [layerInstruction]
videoComposition.instructions = [instruction]
```

### 1.3 自定义帧级合成器（Custom Compositor）

用于实现 LUT 滤镜、Core Image 特效等：

```swift
class MetalVideoCompositor: NSObject, AVVideoCompositing {
    var sourcePixelBufferAttributes: [String: Any]? = [
        kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA
    ]
    var requiredPixelBufferAttributesForRenderContext: [String: Any] = [
        kCVPixelBufferPixelFormatTypeKey as String: kCVPixelFormatType_32BGRA
    ]
    
    private let ciContext = CIContext(mtlDevice: MTLCreateSystemDefaultDevice()!)
    
    func startRequest(_ request: AVAsynchronousVideoCompositionRequest) {
        guard let sourcePixelBuffer = request.sourceFrame(byTrackID: request.sourceTrackIDs[0].int32Value) else {
            request.finish(with: NSError(domain: "Compositor", code: -1))
            return
        }
        
        // 应用 Core Image 滤镜
        var ciImage = CIImage(cvPixelBuffer: sourcePixelBuffer)
        
        // LUT 滤镜
        if let lutFilter = CIFilter(name: "CIColorCube") {
            lutFilter.setValue(ciImage, forKey: kCIInputImageKey)
            lutFilter.setValue(lutData, forKey: "inputCubeData")
            lutFilter.setValue(64, forKey: "inputCubeDimension")
            ciImage = lutFilter.outputImage ?? ciImage
        }
        
        // 色彩调节
        if let colorFilter = CIFilter(name: "CIColorControls") {
            colorFilter.setValue(ciImage, forKey: kCIInputImageKey)
            colorFilter.setValue(1.2, forKey: kCIInputSaturationKey)
            colorFilter.setValue(0.1, forKey: kCIInputBrightnessKey)
            ciImage = colorFilter.outputImage ?? ciImage
        }
        
        // 渲染到输出 PixelBuffer
        guard let outputBuffer = request.renderContext.newPixelBuffer() else {
            request.finish(with: NSError(domain: "Compositor", code: -2))
            return
        }
        
        ciContext.render(ciImage, to: outputBuffer)
        request.finish(withComposedVideoFrame: outputBuffer)
    }
    
    func renderContextChanged(_ newRenderContext: AVVideoCompositionRenderContext) {}
    func cancelAllPendingVideoCompositionRequests() {}
}
```

### 1.4 文字与图层叠加

```swift
// 使用 Core Animation 叠加文字、贴纸等
let parentLayer = CALayer()
let videoLayer = CALayer()
let overlayLayer = CALayer()

parentLayer.frame = CGRect(origin: .zero, size: renderSize)
videoLayer.frame = CGRect(origin: .zero, size: renderSize)
overlayLayer.frame = CGRect(origin: .zero, size: renderSize)

// 添加文字图层
let textLayer = CATextLayer()
textLayer.string = "一剪 · 视频剪辑"
textLayer.font = CTFontCreateWithName("PingFangSC-Regular" as CFString, 36, nil)
textLayer.foregroundColor = UIColor.white.cgColor
textLayer.alignmentMode = .center
textLayer.frame = CGRect(x: 0, y: 100, width: renderSize.width, height: 60)
overlayLayer.addSublayer(textLayer)

parentLayer.addSublayer(videoLayer)
parentLayer.addSublayer(overlayLayer)

videoComposition.animationTool = AVVideoCompositionCoreAnimationTool(
    postProcessingAsVideoLayer: videoLayer,
    in: parentLayer
)
```

### 1.5 导出

```swift
// 高层导出（AVAssetExportSession）
let export = AVAssetExportSession(
    asset: composition,
    presetName: AVAssetExportPreset1920x1080
)!
export.outputURL = outputURL
export.outputFileType = .mp4
export.videoComposition = videoComposition
export.audioMix = audioMix  // 可选音频混合

export.exportAsynchronously {
    switch export.status {
    case .completed:
        print("导出成功：\(outputURL)")
    case .failed:
        print("导出失败：\(export.error?.localizedDescription ?? "")")
    default:
        break
    }
}

// 低层导出（逐帧控制，AVAssetWriter）
let writer = try AVAssetWriter(outputURL: outputURL, fileType: .mp4)
let videoInput = AVAssetWriterInput(
    mediaType: .video,
    outputSettings: [
        AVVideoCodecKey: AVVideoCodecType.h264,
        AVVideoWidthKey: 1080,
        AVVideoHeightKey: 1920,
        AVVideoCompressionPropertiesKey: [
            AVVideoAverageBitRateKey: 8_000_000,
            AVVideoProfileLevelKey: AVVideoProfileLevelH264HighAutoLevel
        ]
    ]
)
let adaptor = AVAssetWriterInputPixelBufferAdaptor(
    assetWriterInput: videoInput,
    sourcePixelBufferAttributes: nil
)
writer.add(videoInput)
writer.startWriting()
// 逐帧写入...
```

### 导出预设

| 预设名 | 分辨率 | 适用场景 |
|-------|-------|---------|
| `AVAssetExportPreset640x480` | 640×480 | 低清分享 |
| `AVAssetExportPreset1280x720` | 1280×720 | 720p |
| `AVAssetExportPreset1920x1080` | 1920×1080 | 1080p 标准 |
| `AVAssetExportPreset3840x2160` | 3840×2160 | 4K |
| `AVAssetExportPresetHighestQuality` | 最高原始质量 | 高质量导出 |
| `AVAssetExportPresetMediumQuality` | 中等质量 | 平衡导出 |
| `AVAssetExportPresetPassthrough` | 不转码 | 仅封装，最快 |

---

## 二、VideoToolbox（低层硬件编解码）

### 2.1 支持的编解码格式

| 格式 | 编码 | 解码 | 备注 |
|------|------|------|------|
| H.264/AVC | ✅ | ✅ | 全系支持 |
| H.265/HEVC | ✅（A9+） | ✅ | iPhone 7+ |
| ProRes | ✅（M1+） | ✅ | 专业格式 |
| VP9 | ❌ | ✅ | 仅解码 |
| AV1 | ❌ | ✅（A14+/M1+） | 仅解码 |

### 2.2 H.264 硬件编码示例

```swift
// 创建压缩会话
var compressionSession: VTCompressionSession?

let status = VTCompressionSessionCreate(
    allocator: nil,
    width: Int32(1920),
    height: Int32(1080),
    codecType: kCMVideoCodecType_H264,
    encoderSpecification: nil,
    imageBufferAttributes: nil,
    compressedDataAllocator: nil,
    outputCallback: { _, _, status, _, sampleBuffer in
        guard status == noErr, let sb = sampleBuffer else { return }
        // 处理编码后的数据
    },
    refcon: nil,
    compressionSessionOut: &compressionSession
)

if let session = compressionSession {
    // 配置编码参数
    VTSessionSetProperty(session, key: kVTCompressionPropertyKey_RealTime, value: false as CFBoolean)
    VTSessionSetProperty(session, key: kVTCompressionPropertyKey_AverageBitRate, value: 8_000_000 as CFNumber)
    VTSessionSetProperty(session, key: kVTCompressionPropertyKey_ProfileLevel, 
                         value: kVTProfileLevel_H264_High_AutoLevel)
    VTSessionSetProperty(session, key: kVTCompressionPropertyKey_AllowFrameReordering, 
                         value: true as CFBoolean)
    VTCompressionSessionPrepareToEncodeFrames(session)
}
```

### 2.3 适用场景

| 场景 | 建议 |
|------|------|
| 视频导出/转码 | AVAssetExportSession（更简单） |
| 直播/录制（低延迟） | **VideoToolbox**（精细控制） |
| 自定义编码参数 | **VideoToolbox** |
| 视频会议 | **VideoToolbox** |

---

## 三、Core Image（GPU 滤镜）

### 3.1 概述
- **内置滤镜数量**：200+
- **GPU 加速**：Metal（主要）/ OpenGL（老设备）
- **实时性能**：可用于 AVCapture 实时滤镜，30fps+ 无压力

### 3.2 常用滤镜分类

| 分类 | 代表滤镜 | 视频编辑用途 |
|------|---------|------------|
| 色彩调节 | `CIColorControls` | 亮度/对比度/饱和度 |
| 曝光 | `CIExposureAdjust` | 曝光补偿 |
| 色温 | `CITemperatureAndTint` | 暖色/冷色调 |
| 锐化 | `CISharpenLuminance` | 锐化 |
| 模糊 | `CIGaussianBlur` | 背景虚化 |
| LUT | `CIColorCube`, `CIColorCubeWithColorSpace` | LUT 滤镜 |
| 渐晕 | `CIVignette` | 暗角效果 |
| 复古 | `CISepiaTone` | 黑白/复古 |
| 合成 | `CISourceOverCompositing` | 文字/贴纸叠加 |

### 3.3 LUT 滤镜实现

```swift
// 加载 .cube 格式 LUT 文件
func loadLUT(named name: String) -> CIFilter? {
    guard let url = Bundle.main.url(forResource: name, withExtension: "cube"),
          let data = try? Data(contentsOf: url) else { return nil }
    
    // 解析 .cube 文件头获取尺寸
    let dimension = 64  // 常见 64x64x64
    
    let lutFilter = CIFilter(name: "CIColorCube")
    lutFilter?.setValue(dimension, forKey: "inputCubeDimension")
    lutFilter?.setValue(data as NSData, forKey: "inputCubeData")
    return lutFilter
}

// 应用 LUT 到视频帧
let ciContext = CIContext(mtlDevice: MTLCreateSystemDefaultDevice()!)

func applyLUT(to pixelBuffer: CVPixelBuffer, lut: CIFilter) -> CVPixelBuffer? {
    let ciImage = CIImage(cvPixelBuffer: pixelBuffer)
    lut.setValue(ciImage, forKey: kCIInputImageKey)
    guard let output = lut.outputImage else { return nil }
    
    var outputBuffer: CVPixelBuffer?
    CVPixelBufferCreate(nil, 
        CVPixelBufferGetWidth(pixelBuffer),
        CVPixelBufferGetHeight(pixelBuffer),
        kCVPixelFormatType_32BGRA, nil, &outputBuffer)
    
    if let buf = outputBuffer {
        ciContext.render(output, to: buf)
    }
    return outputBuffer
}
```

### 3.4 实时预览管线

```
CIImage ──▶ CIFilter Chain ──▶ Metal Texture ──▶ MTKView（显示）
    │
    └── CVPixelBuffer（来自 AVPlayerItemVideoOutput 或 AVCaptureVideoDataOutput）
```

---

## 四、相册访问（PHPhotoLibrary）

```swift
// 请求相册权限（iOS 14+）
PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
    switch status {
    case .authorized, .limited:
        // 可以访问
    default:
        break
    }
}

// 获取视频资源
let fetchResult = PHAsset.fetchAssets(
    with: .video,
    options: PHFetchOptions()
)

// 获取视频 URL
let requestOptions = PHVideoRequestOptions()
requestOptions.isNetworkAccessAllowed = true
requestOptions.deliveryMode = .highQualityFormat

PHImageManager.default().requestAVAsset(
    forVideo: phAsset,
    options: requestOptions
) { avAsset, _, _ in
    guard let urlAsset = avAsset as? AVURLAsset else { return }
    let videoURL = urlAsset.url
}
```

---

## 五、iOS 视频编辑完整工作流

```
┌─────────────────────────────────────────────────────────────┐
│                      iOS 视频编辑工作流                       │
├────────────┬────────────┬────────────┬────────────┬──────────┤
│    读取     │    编辑     │    预览     │    特效     │   导出   │
├────────────┼────────────┼────────────┼────────────┼──────────┤
│PHPhotoLib  │AVMutable   │AVPlayer +  │Core Image  │AVAsset  │
│AVURLAsset  │Composition │MTKView     │Metal       │Export   │
│            │ 裁剪/拼接  │CompositionP│ LUT/色彩   │Session  │
│            │ 音量控制   │layer       │ 文字/贴纸  │ MP4/MOV │
└────────────┴────────────┴────────────┴────────────┴──────────┘
```

---

## 六、性能特点

| 操作 | 性能 | 备注 |
|------|------|------|
| AVAssetExportSession 1080p 30s | 约 10-15s | A15 芯片，MediaEngine 硬件加速 |
| VideoToolbox H.264 编码 1080p | 实时 60fps+ | 硬件编码器 |
| Core Image 滤镜链（3-5个）| 16ms/帧 @ 1080p | Metal 渲染 |
| AVMutableComposition 拼接 | < 100ms | 仅创建引用，无 I/O |
| passthrough 导出（无重编码） | < 1s | 仅容器操作 |

---

## 七、KMP 集成模式

在 yijian 项目中，iOS 视频处理通过 `actual` 实现桥接：

```kotlin
// commonMain: expect 接口
expect class VideoProcessor {
    fun trimVideo(inputPath: String, start: Double, end: Double, outputPath: String)
    fun applyLUT(inputPath: String, lutName: String, outputPath: String)
    fun exportVideo(inputPath: String, options: ExportOptions, outputPath: String)
}

// iosMain: actual 实现（调用 Swift/Objective-C）
actual class VideoProcessor {
    actual fun trimVideo(inputPath: String, start: Double, end: Double, outputPath: String) {
        // 调用 Objective-C 桥接层 → AVMutableComposition → AVAssetExportSession
    }
}
```

---

## 参考链接

- [AVFoundation Programming Guide](https://developer.apple.com/av-foundation/)
- [AVMutableComposition](https://developer.apple.com/documentation/avfoundation/avmutablecomposition)
- [AVAssetExportSession](https://developer.apple.com/documentation/avfoundation/avassetexportsession)
- [VideoToolbox](https://developer.apple.com/documentation/videotoolbox)
- [Core Image Filter Reference](https://developer.apple.com/library/archive/documentation/GraphicsImaging/Reference/CoreImageFilterReference/)
- [Metal Performance Shaders](https://developer.apple.com/documentation/metalperformanceshaders)
