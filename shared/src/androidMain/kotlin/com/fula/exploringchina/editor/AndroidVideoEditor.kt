package com.fula.exploringchina.editor

import android.media.MediaCodecInfo
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaCodec
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import com.fula.exploringchina.model.ColorAdjustment
import com.fula.exploringchina.model.EditTimeline
import com.fula.exploringchina.model.ExportCodec
import com.fula.exploringchina.model.ExportConfig
import com.fula.exploringchina.model.ExportResult
import com.fula.exploringchina.model.FilterEffect
import com.fula.exploringchina.model.TextOverlay
import java.io.File
import java.io.FileOutputStream

/**
 * Android 视频编辑器 — actual 实现。
 *
 * 裁剪: MediaExtractor + MediaMuxer (仅 I 帧精度，快速无损)
 * 滤镜: 框架已搭建，实际 GLSL 渲染需 GLSurfaceView 上下文（二期后续实现）
 * 缩略图: MediaMetadataRetriever
 */
actual class VideoEditor {

    private var released = false

    // ════════════════════════════════════════════
    // Trim — I-frame precision (fast, lossless)
    // ════════════════════════════════════════════
    actual suspend fun trimVideo(
        input: String,
        startMs: Long,
        endMs: Long,
        output: String,
    ): Result<Unit> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        try {
            trimVideoInternal(input, startMs, endMs, output)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // ════════════════════════════════════════════
    // Filter + Color Adjust export (skeleton)
    // ════════════════════════════════════════════
    actual suspend fun exportWithFilter(
        input: String,
        filter: FilterEffect?,
        colorAdjust: ColorAdjustment?,
        output: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        try {
            // TODO: Phase 2.2 — 使用 OpenGL ES 3.0 渲染管线
            // 1. MediaCodec 解码 → SurfaceTexture
            // 2. GLSurfaceView / EGL 上下文 + 自定义 GLSL 着色器
            copyFile(input, output)
            onProgress?.invoke(1.0f)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // ════════════════════════════════════════════
    // Text overlay export (skeleton)
    // ════════════════════════════════════════════
    actual suspend fun exportWithTextOverlays(
        input: String,
        textOverlays: List<TextOverlay>,
        output: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<Unit> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        try {
            // TODO: Phase 2.3 — 文字叠加
            // 1. MediaCodec 解码
            // 2. 每帧渲染: Canvas Bitmap → GL 纹理合成
            copyFile(input, output)
            onProgress?.invoke(1.0f)
            return Result.success(Unit)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // ════════════════════════════════════════════
    // Full timeline export (skeleton)
    // ════════════════════════════════════════════
    actual suspend fun exportTimeline(
        timeline: EditTimeline,
        config: ExportConfig,
        output: String,
        onProgress: ((Float) -> Unit)?,
    ): Result<ExportResult> {
        if (released) return Result.failure(IllegalStateException("VideoEditor released"))
        try {
            // TODO: 多片段拼接 + 滤镜 + 文字完整管线
            if (timeline.clips.isEmpty()) {
                return Result.failure(IllegalArgumentException("No clips in timeline"))
            }
            // 单片段回退：直接复制
            val clip = timeline.clips.first()
            copyFile(clip.sourcePath, output)

            val fileLen = File(output).length()
            val result = ExportResult(
                outputPath = output,
                durationMs = clip.durationMs,
                fileSize = fileLen,
                width = config.width,
                height = config.height,
            )
            onProgress?.invoke(1.0f)
            return Result.success(result)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    // ════════════════════════════════════════════
    // Thumbnail extraction
    // ════════════════════════════════════════════
    actual suspend fun extractThumbnail(
        videoPath: String,
        timeMs: Long,
        width: Int,
        height: Int,
    ): ByteArray? {
        if (released) return null
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val bitmap = retriever.getScaledFrameAtTime(
                timeMs * 1000L,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                width,
                height,
            )
            retriever.release()
            if (bitmap == null) return null
            val stream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 90, stream)
            bitmap.recycle()
            stream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    actual fun release() {
        released = true
    }

    // ── Internal ──────────────────────────────

    /**
     * 无损裁剪：基于 MediaExtractor + MediaMuxer。
     * 精度受限于关键帧（I 帧）间隔。
     */
    private fun trimVideoInternal(input: String, startMs: Long, endMs: Long, output: String) {
        val extractor = MediaExtractor()
        extractor.setDataSource(input)

        // 找到视频轨道
        var videoTrackIndex = -1
        var videoFormat: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            if (mime.startsWith("video/")) {
                videoTrackIndex = i
                videoFormat = format
                break
            }
        }
        if (videoTrackIndex < 0) throw IllegalStateException("No video track found")

        // 定位到开始时间
        val startUs = startMs * 1000L
        val endUs = if (endMs > 0) endMs * 1000L else Long.MAX_VALUE
        extractor.selectTrack(videoTrackIndex)
        extractor.seekTo(startUs, MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

        val muxer = MediaMuxer(output, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        val trackIndex = muxer.addTrack(videoFormat!!)
        muxer.start()

        val buffer = java.nio.ByteBuffer.allocate(1024 * 1024) // 1MB
        val info = MediaCodec.BufferInfo()

        while (true) {
            info.set(0, 0, 0L, 0)
            val sampleSize = extractor.readSampleData(buffer, 0)
            if (sampleSize < 0) break

            val sampleTimeUs = extractor.sampleTime
            if (sampleTimeUs > endUs) break

            info.set(0, sampleSize, sampleTimeUs - startUs, extractor.sampleFlags)
            muxer.writeSampleData(trackIndex, buffer, info)
            extractor.advance()
        }

        muxer.stop()
        muxer.release()
        extractor.release()
    }

    private fun copyFile(input: String, output: String) {
        File(input).copyTo(File(output), overwrite = true)
    }
}
