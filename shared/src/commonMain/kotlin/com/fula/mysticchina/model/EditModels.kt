package com.fula.mysticchina.model

/**
 * 视频片段 — 原始视频的一段裁剪范围
 */
data class VideoClip(
    val id: String,
    /** 原始视频路径 */
    val sourcePath: String,
    /** 裁剪开始时间（毫秒） */
    val startMs: Long = 0L,
    /** 裁剪结束时间（毫秒），0 表示到视频末尾 */
    val endMs: Long = 0L,
    /** 播放速率 (0.25 ~ 4.0) */
    val speed: Float = 1.0f,
    /** 音量 (0.0 ~ 1.0) */
    val volume: Float = 1.0f,
    /** 应用的滤镜 */
    val filter: FilterEffect? = null,
    /** 应用的色彩调节 */
    val colorAdjustment: ColorAdjustment? = null,
    /** 文字叠加列表 */
    val textOverlays: List<TextOverlay> = emptyList(),
) {
    /** 片段时长（毫秒），从 startMs 到 endMs */
    val durationMs: Long get() = if (endMs > startMs) endMs - startMs else 0L
}

/**
 * 编辑时间轴 — 由多个有序片段组成
 */
data class EditTimeline(
    /** 片段列表（按显示顺序） */
    val clips: List<VideoClip> = emptyList(),
    /** 全局视频尺寸 */
    val videoWidth: Int = 1920,
    val videoHeight: Int = 1080,
    /** 帧率 */
    val frameRate: Int = 30,
)

/**
 * LUT 滤镜效果
 */
data class FilterEffect(
    /** 滤镜唯一标识 */
    val id: String,
    /** 滤镜显示名称 */
    val name: String,
    /** 对应的 LUT 纹理资源路径（PNG 512×512） */
    val lutPath: String,
    /** 滤镜强度 0.0 ~ 1.0 */
    val intensity: Float = 1.0f,
)

/**
 * 色彩调节参数
 */
data class ColorAdjustment(
    /** 亮度 -1.0 ~ 1.0, 默认 0 */
    val brightness: Float = 0f,
    /** 对比度 0.0 ~ 2.0, 默认 1.0 */
    val contrast: Float = 1.0f,
    /** 饱和度 0.0 ~ 2.0, 默认 1.0 */
    val saturation: Float = 1.0f,
    /** 锐化 0.0 ~ 1.0, 默认 0 */
    val sharpen: Float = 0f,
    /** 色温 -1.0 ~ 1.0, 默认 0 */
    val temperature: Float = 0f,
    /** 曝光 -2.0 ~ 2.0, 默认 0 */
    val exposure: Float = 0f,
    /** 高光 -1.0 ~ 1.0, 默认 0 */
    val highlights: Float = 0f,
    /** 阴影 -1.0 ~ 1.0, 默认 0 */
    val shadows: Float = 0f,
    /** 暗角 0.0 ~ 1.0, 默认 0 */
    val vignette: Float = 0f,
)

/**
 * 文字叠加
 */
data class TextOverlay(
    val id: String,
    /** 文字内容 */
    val text: String,
    /** 字体大小（sp） */
    val fontSize: Float = 36f,
    /** 字体颜色 ARGB */
    val colorArgb: Long = 0xFFFFFFFF,
    /** 描边颜色 ARGB */
    val strokeColorArgb: Long = 0xCC000000,
    /** 描边宽度 */
    val strokeWidth: Float = 2f,
    /** 水平位置 (0.0 ~ 1.0, 相对于视频宽度比例) */
    val positionX: Float = 0.5f,
    /** 垂直位置 (0.0 ~ 1.0, 相对于视频高度比例) */
    val positionY: Float = 0.5f,
    /** 旋转角度（度） */
    val rotation: Float = 0f,
    /** 缩放 0.1 ~ 5.0 */
    val scale: Float = 1.0f,
    /** 叠加的时间范围起始（毫秒，相对于所在片段） */
    val startOffsetMs: Long = 0L,
    /** 叠加的时间范围结束（毫秒，相对于所在片段），0 表示到片段末尾 */
    val durationMs: Long = 0L,
)

/**
 * 导出配置
 */
data class ExportConfig(
    /** 输出宽度 */
    val width: Int = 1920,
    /** 输出高度 */
    val height: Int = 1080,
    /** 码率 (bps) */
    val bitrate: Long = 8_000_000L,
    /** 帧率 */
    val frameRate: Int = 30,
    /** 编码格式 */
    val codec: ExportCodec = ExportCodec.H264,
    /** 输出格式 */
    val format: ExportFormat = ExportFormat.MP4,
) {
    companion object {
        val SD = ExportConfig(width = 720, height = 720, bitrate = 4_000_000L)
        val HD = ExportConfig(width = 1920, height = 1080, bitrate = 8_000_000L)
        val K2 = ExportConfig(width = 2560, height = 1440, bitrate = 16_000_000L)
        val K4 = ExportConfig(width = 3840, height = 2160, bitrate = 32_000_000L)
    }
}

enum class ExportCodec { H264, H265 }
enum class ExportFormat { MP4 }

/**
 * 导出结果
 */
data class ExportResult(
    val outputPath: String,
    val durationMs: Long,
    val fileSize: Long,
    val width: Int,
    val height: Int,
)

/** 一组内置 LUT 滤镜预设（后续从资源加载） */
object BuiltinFilters {
    val none = FilterEffect("none", "原图", "", intensity = 0f)
    val list = listOf(
        none,
        FilterEffect("vintage", "复古", "luts/vintage.png", intensity = 0.8f),
        FilterEffect("film", "胶片", "luts/film.png", intensity = 0.9f),
        FilterEffect("fresh", "清新", "luts/fresh.png", intensity = 0.7f),
        FilterEffect("warm", "暖阳", "luts/warm.png", intensity = 0.85f),
        FilterEffect("cool", "冷色", "luts/cool.png", intensity = 0.8f),
        FilterEffect("drama", "戏剧", "luts/drama.png", intensity = 1.0f),
        FilterEffect("vivid", "鲜艳", "luts/vivid.png", intensity = 0.8f),
        FilterEffect("mono", "黑白", "luts/mono.png", intensity = 1.0f),
        FilterEffect("vapor", "赛博", "luts/vapor.png", intensity = 0.9f),
        FilterEffect("food", "美食", "luts/food.png", intensity = 0.75f),
        FilterEffect("portrait", "人像", "luts/portrait.png", intensity = 0.7f),
    )
}
