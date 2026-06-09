package com.fula.exploringchina.editor

import com.fula.exploringchina.model.ColorAdjustment
import com.fula.exploringchina.model.EditTimeline
import com.fula.exploringchina.model.ExportConfig
import com.fula.exploringchina.model.ExportResult
import com.fula.exploringchina.model.FilterEffect
import com.fula.exploringchina.model.TextOverlay

/**
 * iOS 视频编辑器 — actual 实现（骨架）。
 *
 * 裁剪: AVAssetExportSession + AVMutableComposition
 * 滤镜: Core Image CIColorCube + CIColorControls
 * 文字: CATextLayer → AVVideoCompositionCoreAnimationTool
 *
 * TODO: 二期后续实现完整 AVFoundation 管线
 */
actual class VideoEditor {

    private var released = false

    actual suspend fun trimVideo(
        input: String,
        startMs: Long,
        endMs: Long,
        output: String,
    ): Result<Unit> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        // TODO: AVAssetExportSession 实现
        return Result.success(Unit)
    }

    actual suspend fun exportWithFilter(
        input: String,
        filter: FilterEffect?,
        colorAdjust: ColorAdjustment?,
        output: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        // TODO: CIColorCube + CIColorControls 滤镜链
        return Result.success(Unit)
    }

    actual suspend fun exportWithTextOverlays(
        input: String,
        textOverlays: List<TextOverlay>,
        output: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        // TODO: CATextLayer 渲染
        return Result.success(Unit)
    }

    actual suspend fun exportTimeline(
        timeline: EditTimeline,
        config: ExportConfig,
        output: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<ExportResult> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        // TODO: AVMutableComposition 多片段拼接
        return Result.success(
            ExportResult(
                outputPath = output,
                durationMs = timeline.clips.sumOf { it.durationMs },
                fileSize = 0L,
                width = config.width,
                height = config.height,
            )
        )
    }

    actual suspend fun extractThumbnail(
        videoPath: String,
        timeMs: Long,
        width: Int,
        height: Int,
    ): ByteArray? {
        if (released) return null
        // TODO: AVAssetImageGenerator
        return null
    }

    actual fun release() {
        released = true
    }
}
