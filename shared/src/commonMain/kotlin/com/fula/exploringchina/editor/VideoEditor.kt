package com.fula.exploringchina.editor

import com.fula.exploringchina.model.ColorAdjustment
import com.fula.exploringchina.model.EditTimeline
import com.fula.exploringchina.model.ExportConfig
import com.fula.exploringchina.model.ExportResult
import com.fula.exploringchina.model.FilterEffect
import com.fula.exploringchina.model.TextOverlay

/**
 * 跨平台视频编辑器 — expect 声明。
 *
 * 各平台通过 actual class 提供原生实现：
 * - Android: MediaExtractor+MediaMuxer (裁剪), OpenGL ES 3.0 (滤镜)
 * - iOS:     AVAssetExportSession (裁剪), Core Image + Metal (滤镜)
 * - macOS:   暂缺 (直接复用 appleMain 中的 iOS 实现)
 */
expect class VideoEditor {

    /**
     * 无损裁剪视频（基于关键帧，适用于起点/终点对齐 I 帧的场景）。
     * 精度为最近的 I 帧边界。
     *
     * @param input  源视频路径
     * @param startMs  裁剪起始时间（毫秒）
     * @param endMs    裁剪结束时间（毫秒）
     * @param output   输出路径
     */
    suspend fun trimVideo(
        input: String,
        startMs: Long,
        endMs: Long,
        output: String,
    ): Result<Unit>

    /**
     * 应用滤镜和色彩调节并导出。
     *
     * @param input      源视频路径
     * @param filter     滤镜效果（null 表示无滤镜）
     * @param colorAdjust 色彩调节参数（null 表示无调节）
     * @param output     输出路径
     * @param onProgress 进度回调 0.0 ~ 1.0
     */
    suspend fun exportWithFilter(
        input: String,
        filter: FilterEffect?,
        colorAdjust: ColorAdjustment?,
        output: String,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<Unit>

    /**
     * 导出带文字叠加的视频。
     *
     * @param input       源视频路径
     * @param textOverlays 文字叠加列表
     * @param output      输出路径
     * @param onProgress  进度回调 0.0 ~ 1.0
     */
    suspend fun exportWithTextOverlays(
        input: String,
        textOverlays: List<TextOverlay>,
        output: String,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<Unit>

    /**
     * 导出完整时间轴（多片段 + 滤镜 + 文字）。
     *
     * @param timeline   完整编辑时间轴
     * @param config     导出配置
     * @param output     输出路径
     * @param onProgress 进度回调 0.0 ~ 1.0
     */
    suspend fun exportTimeline(
        timeline: EditTimeline,
        config: ExportConfig,
        output: String,
        onProgress: ((Float) -> Unit)? = null,
    ): Result<ExportResult>

    /**
     * 提取视频指定时间点的缩略图。
     *
     * @param videoPath 视频路径
     * @param timeMs    时间点（毫秒）
     * @param width     目标宽度
     * @param height    目标高度
     * @return PNG 编码的字节数组，失败返回 null
     */
    suspend fun extractThumbnail(
        videoPath: String,
        timeMs: Long,
        width: Int,
        height: Int,
    ): ByteArray?

    /** 释放视频编辑器资源 */
    fun release()
}
