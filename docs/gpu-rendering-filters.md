# GPU 渲染与视频滤镜技术

> **文档版本**：v1.0  
> **更新日期**：2026-05-29  

---

## 概述

视频特效（滤镜、色彩调节、文字叠加等）必须通过 GPU 渲染才能达到实时预览的帧率要求（30fps+）。纯 CPU 处理 1080p 滤镜通常只能达到 3-5fps，而 GPU 可轻松达到 60fps+。

---

## 一、GPU API 对比

| GPU API | 平台 | 特点 | 最低版本 |
|---------|------|------|---------|
| **OpenGL ES 2.0/3.0** | Android（主流）| 兼容性最好 | Android 5.0 |
| **Vulkan** | Android 7.0+ | 低开销，现代 API | Android 7.0 |
| **Metal** | iOS/macOS | Apple 专属，高性能 | iOS 8.0 |
| **Core Image** | iOS/macOS | 高层 API，基于 Metal | iOS 5.0 |

### 推荐选择
- **Android**：OpenGL ES 3.0（兼容性+性能平衡），Vulkan 适合高端设备
- **iOS**：Metal（通过 Core Image 高层 API 或自定义 Metal Shader）

---

## 二、视频帧渲染管线

### Android 渲染管线

```
MediaCodec 解码
     │
     ▼ (SurfaceTexture/GL_TEXTURE_EXTERNAL_OES)
OpenGL ES 着色器
     │ 顶点着色器（坐标变换）
     │ 片元着色器（颜色处理）
     │
     ├──▶ 实时预览（GLSurfaceView/TextureView）
     │
     └──▶ MediaCodec 编码（Surface 模式）──▶ 输出文件
```

```kotlin
// Android OpenGL ES 渲染核心
class VideoFilterRenderer : GLSurfaceView.Renderer {
    
    private var surfaceTexture: SurfaceTexture? = null
    private var textureId: Int = 0
    private var program: Int = 0
    private var lutTextureId: Int = 0
    
    // 基础顶点着色器
    private val VERTEX_SHADER = """
        attribute vec4 aPosition;
        attribute vec2 aTextureCoord;
        varying vec2 vTextureCoord;
        void main() {
            gl_Position = aPosition;
            vTextureCoord = aTextureCoord;
        }
    """
    
    // LUT 滤镜片元着色器
    private val LUT_FRAGMENT_SHADER = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        uniform samplerExternalOES sVideoTexture;  // 视频帧（OES 格式）
        uniform sampler2D sLutTexture;             // LUT 纹理
        uniform float uLutSize;                    // LUT 尺寸（如 64.0）
        uniform float uIntensity;                  // 滤镜强度 0.0-1.0
        
        varying vec2 vTextureCoord;
        
        void main() {
            vec4 originalColor = texture2D(sVideoTexture, vTextureCoord);
            
            // LUT 采样（512×512 纹理，8×8 排列的 64×64 切片）
            float sliceSize = 1.0 / uLutSize;
            float slicePixelSize = sliceSize / uLutSize;
            float sliceInnerSize = slicePixelSize * (uLutSize - 1.0);
            
            float xOffset = 0.5 * slicePixelSize + originalColor.r * sliceInnerSize;
            float yOffset = 0.5 * slicePixelSize + originalColor.g * sliceInnerSize;
            float zOffset = originalColor.b * (uLutSize - 1.0);
            
            float zSlice0 = floor(zOffset);
            float zSlice1 = zSlice0 + 1.0;
            float interpZ = zOffset - zSlice0;
            
            float xCoord0 = mod(zSlice0, uLutSize) / uLutSize + xOffset;
            float yCoord0 = floor(zSlice0 / uLutSize) / uLutSize + yOffset;
            float xCoord1 = mod(zSlice1, uLutSize) / uLutSize + xOffset;
            float yCoord1 = floor(zSlice1 / uLutSize) / uLutSize + yOffset;
            
            vec4 lutColor0 = texture2D(sLutTexture, vec2(xCoord0, yCoord0));
            vec4 lutColor1 = texture2D(sLutTexture, vec2(xCoord1, yCoord1));
            vec4 lutColor = mix(lutColor0, lutColor1, interpZ);
            
            // 混合原始颜色与 LUT 颜色（强度控制）
            gl_FragColor = mix(originalColor, lutColor, uIntensity);
        }
    """
    
    // 亮度/对比度/饱和度调节着色器
    private val COLOR_ADJUST_SHADER = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        uniform samplerExternalOES sTexture;
        uniform float uBrightness;   // -1.0 ~ 1.0
        uniform float uContrast;     // 0.0 ~ 2.0
        uniform float uSaturation;   // 0.0 ~ 2.0
        
        varying vec2 vTextureCoord;
        
        vec3 rgb2hsv(vec3 c) {
            vec4 K = vec4(0.0, -1.0/3.0, 2.0/3.0, -1.0);
            vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
            vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
            float d = q.x - min(q.w, q.y);
            float e = 1.0e-10;
            return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
        }
        
        vec3 hsv2rgb(vec3 c) {
            vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
            vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
            return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
        }
        
        void main() {
            vec4 color = texture2D(sTexture, vTextureCoord);
            
            // 亮度
            color.rgb += uBrightness;
            
            // 对比度
            color.rgb = (color.rgb - 0.5) * uContrast + 0.5;
            
            // 饱和度（通过 HSV 调整）
            vec3 hsv = rgb2hsv(color.rgb);
            hsv.y *= uSaturation;
            color.rgb = hsv2rgb(hsv);
            
            gl_FragColor = clamp(color, 0.0, 1.0);
        }
    """
}
```

### iOS Metal 渲染管线

```swift
// iOS Metal 视频滤镜渲染
class MetalVideoFilter {
    
    private let device: MTLDevice
    private let commandQueue: MTLCommandQueue
    private let ciContext: CIContext
    
    // 滤镜链
    private var filterChain: [CIFilter] = []
    
    init() {
        device = MTLCreateSystemDefaultDevice()!
        commandQueue = device.makeCommandQueue()!
        ciContext = CIContext(mtlDevice: device, options: [
            .workingColorSpace: CGColorSpace(name: CGColorSpace.linearSRGB)!
        ])
    }
    
    // 构建 LUT 滤镜（.cube 格式）
    func loadLUTFilter(from cubeURL: URL, intensity: Float = 1.0) -> CIFilter? {
        guard let cubeData = try? Data(contentsOf: cubeURL) else { return nil }
        
        let filter = CIFilter(name: "CIColorCubeWithColorSpace")
        filter?.setValue(64, forKey: "inputCubeDimension")
        filter?.setValue(cubeData as NSData, forKey: "inputCubeData")
        filter?.setValue(CGColorSpace(name: CGColorSpace.sRGB), forKey: "inputColorSpace")
        return filter
    }
    
    // 色彩调节滤镜
    func colorAdjustmentFilter(
        brightness: Float = 0,  // -1.0 ~ 1.0
        contrast: Float = 1,    // 0.0 ~ 2.0
        saturation: Float = 1   // 0.0 ~ 2.0
    ) -> CIFilter? {
        let filter = CIFilter(name: "CIColorControls")
        filter?.setValue(brightness, forKey: kCIInputBrightnessKey)
        filter?.setValue(contrast, forKey: kCIInputContrastKey)
        filter?.setValue(saturation, forKey: kCIInputSaturationKey)
        return filter
    }
    
    // 处理视频帧
    func processFrame(_ pixelBuffer: CVPixelBuffer) -> CVPixelBuffer? {
        var ciImage = CIImage(cvPixelBuffer: pixelBuffer)
        
        // 应用滤镜链
        for filter in filterChain {
            filter.setValue(ciImage, forKey: kCIInputImageKey)
            if let output = filter.outputImage {
                ciImage = output
            }
        }
        
        // 渲染到新的 PixelBuffer
        var outputBuffer: CVPixelBuffer?
        let attrs: [String: Any] = [
            kCVPixelBufferMetalCompatibilityKey as String: true,
            kCVPixelBufferIOSurfacePropertiesKey as String: [:]
        ]
        CVPixelBufferCreate(
            nil,
            CVPixelBufferGetWidth(pixelBuffer),
            CVPixelBufferGetHeight(pixelBuffer),
            kCVPixelFormatType_32BGRA,
            attrs as CFDictionary,
            &outputBuffer
        )
        
        if let output = outputBuffer {
            ciContext.render(ciImage, to: output)
        }
        return outputBuffer
    }
}
```

---

## 三、LUT 滤镜详解

LUT（Look-Up Table）是业界最常用的视频调色方案，剪映、抖音等主流平台均采用此方案。

### LUT 格式

```
1. .cube 格式（最通用）：
   LUT_3D_SIZE 64
   0.000000 0.000000 0.000000
   0.015873 0.000000 0.000000
   ...（共 64×64×64 = 262144 个 RGB 值）

2. 512×512 PNG 纹理格式（移动端常用）：
   - 8×8 的 64×64 切片网格
   - 每个切片对应一个 B 通道值
   - 总计 64 个切片

3. Hald CLUT 格式（兼容 Lightroom）
```

### LUT 制作流程

```
1. 从 Lightroom/Photoshop 导出 .cube 文件
2. 离线工具转换为 512×512 PNG（供移动端高效使用）
3. 打包为 App 资源

推荐免费 LUT 资源：
- https://fixthephoto.com/free-luts
- Adobe Creative Cloud（部分免费）
- DaVinci Resolve 内置 LUT
```

### 性能对比

| 实现方式 | 每帧耗时（1080p） | CPU 占用 | 适用场景 |
|---------|----------------|---------|---------|
| CPU 遍历像素 | ~800ms | 高 | ❌ 不可用 |
| OpenGL ES 着色器 | ~3ms | 低 | ✅ 实时预览 |
| Core Image CIColorCube | ~4ms | 低 | ✅ 实时预览 |
| Metal 计算着色器 | ~2ms | 最低 | ✅ 最优性能 |

---

## 四、特效类型与实现方案

### 4.1 基础色彩调节

| 特效 | Android 实现 | iOS 实现 |
|------|------------|---------|
| 亮度/对比度/饱和度 | GLSL 着色器 | `CIColorControls` |
| 色温/色调 | GLSL 着色器 | `CITemperatureAndTint` |
| 曝光 | GLSL 着色器 | `CIExposureAdjust` |
| 高光/阴影 | GLSL 着色器 | `CIHighlightShadowAdjust` |
| 锐化 | `CISharpenLuminance` GLSL | `CISharpenLuminance` |

### 4.2 特殊效果

| 特效 | 实现难度 | 技术方案 |
|------|---------|---------|
| 粒子效果 | 高 | 自定义着色器 + 粒子系统 |
| 转场动画 | 中 | 两帧混合着色器 |
| 复古效果 | 低 | 组合滤镜 + 噪点纹理 |
| 光晕/漏光 | 中 | 混合模式着色器 |
| 背景虚化 | 高 | 深度估计 + Gaussian Blur |
| 人像美化 | 高 | AI 分割 + 滤镜 |

### 4.3 文字叠加渲染

```kotlin
// Android 文字渲染到 Bitmap 再叠加到 OpenGL
fun renderTextOverlay(text: String, config: TextConfig): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint().apply {
        color = config.color
        textSize = config.fontSize * density
        typeface = Typeface.createFromAsset(assets, config.fontPath)
        isAntiAlias = true
    }
    
    // 添加描边
    val strokePaint = Paint(paint).apply {
        style = Paint.Style.STROKE
        strokeWidth = config.strokeWidth
        color = config.strokeColor
    }
    
    canvas.drawText(text, config.x, config.y, strokePaint)
    canvas.drawText(text, config.x, config.y, paint)
    
    return bitmap
}
```

---

## 五、转场效果实现

```glsl
// 通用淡入淡出转场着色器（Android GLSL）
#extension GL_OES_EGL_image_external : require
precision mediump float;

uniform samplerExternalOES sFromTexture;   // 前一个片段
uniform samplerExternalOES sToTexture;     // 下一个片段
uniform float uProgress;    // 0.0（全显前段）→ 1.0（全显后段）

varying vec2 vTextureCoord;

void main() {
    vec4 fromColor = texture2D(sFromTexture, vTextureCoord);
    vec4 toColor = texture2D(sToTexture, vTextureCoord);
    gl_FragColor = mix(fromColor, toColor, uProgress);
}
```

```glsl
// 划入转场着色器
void main() {
    vec4 fromColor = texture2D(sFromTexture, vTextureCoord);
    vec4 toColor = texture2D(sToTexture, vTextureCoord);
    // 从右向左划入
    gl_FragColor = (vTextureCoord.x < uProgress) ? toColor : fromColor;
}
```

---

## 六、渲染性能优化

### 6.1 纹理缓存
```kotlin
// Android：使用 LruCache 缓存已加载的 LUT 纹理
private val lutTextureCache = LruCache<String, Int>(10)

fun getLutTexture(lutName: String): Int {
    return lutTextureCache.get(lutName) ?: loadLutTexture(lutName).also {
        lutTextureCache.put(lutName, it)
    }
}
```

### 6.2 离屏渲染
- 使用 FBO（Frame Buffer Object）进行离屏渲染
- 多个特效一次性渲染，减少 GPU 状态切换
- 复用 PixelBuffer 池，减少内存分配

### 6.3 分辨率策略
```
实时预览：以 720p 渲染（降低 GPU 负担，节省电量）
最终导出：以目标分辨率（1080p/4K）渲染
缩略图：256×144 快速预览
```

---

## 七、工具与资源

### LUT 资源
- [Free LUTs Collection](https://fixthephoto.com/free-luts)
- [Koji LUTs](https://koji.to/)（付费高质量）

### 着色器工具
- [Shadertoy](https://www.shadertoy.com/)（在线 GLSL 调试）
- [GLSL Sandbox](http://glslsandbox.com/)

### 移动端 GPU 参考
- [Android GPUImage 滤镜参考](https://github.com/CyberAgent/android-gpuimage)（100+ 现成 GLSL 着色器）
- [GPUImage for iOS](https://github.com/BradLarson/GPUImage)（大量 Metal/GLSL 参考）
