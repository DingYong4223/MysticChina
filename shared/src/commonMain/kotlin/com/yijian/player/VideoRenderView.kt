package com.yijian.player

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.base.event.Event

/**
 * 原生视频渲染 View — 通过 Kuikly DeclarativeBaseView 桥接到平台 TextureView。
 *
 * 该 View 本身不绘制视频帧，而是通过 Kuikly 的 expand-native-view 机制
 * 创建一个原生平台纹理视图（Android: TextureView, iOS: AVPlayerLayer），
 * 由平台侧 IVideoPlayer 将视频帧渲染到该 Surface。
 *
 * 用法:
 *   VideoRender {
 *       ref { ctx.videoRef = it }
 *       attr {
 *           size(w, h)
 *           surfaceReady { surface -> ctx.controller.onSurfaceAvailable(surface) }
 *       }
 *   }
 */
class VideoRenderView : DeclarativeBaseView<VideoRenderAttr, VideoRenderEvent>() {

    override fun createAttr(): VideoRenderAttr = VideoRenderAttr()
    override fun createEvent(): VideoRenderEvent = VideoRenderEvent()
    override fun viewName(): String = "VideoRenderView"

    /** 通知原生侧需要重新附件（show） */
    fun attachSurface() {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("attachSurface", null)
        }
    }

    /** 释放原生视频表面 */
    fun detachSurface() {
        performTaskWhenRenderViewDidLoad {
            renderView?.callMethod("detachSurface", null)
        }
    }
}

class VideoRenderAttr : Attr() {
    /** 设置背景色 */
    fun videoBackground(color: Color): VideoRenderAttr {
        "videoBackground" with color.toString()
        return this
    }
}

class VideoRenderEvent : Event() {
    companion object {
        const val SURFACE_READY = "surfaceReady"
        const val SURFACE_DESTROYED = "surfaceDestroyed"
        const val SURFACE_SIZE_CHANGED = "surfaceSizeChanged"
    }

    fun surfaceReady(handler: (surfaceId: Long) -> Unit) {
        register(SURFACE_READY) { params ->
            val id = when (params) {
                is Map<*, *> -> (params["surfaceId"] as? Number)?.toLong() ?: 0L
                is String -> extractJsonLong(params, "surfaceId")
                else -> extractJsonLong(params.toString(), "surfaceId")
            }
            handler(id)
        }
    }

    fun surfaceDestroyed(handler: () -> Unit) {
        register(SURFACE_DESTROYED) { handler() }
    }

    fun surfaceSizeChanged(handler: (width: Int, height: Int) -> Unit) {
        register(SURFACE_SIZE_CHANGED) { params ->
            val json = params as? String ?: ""
            val w = extractJsonLong(json, "width").toInt()
            val h = extractJsonLong(json, "height").toInt()
            handler(w, h)
        }
    }
}

/** 从简单 JSON 字符串中提取整数键值 */
private fun extractJsonLong(json: String, key: String): Long {
    val idx = json.indexOf("\"$key\"")
    if (idx < 0) return 0L
    val colon = json.indexOf(':', idx)
    if (colon < 0) return 0L
    var start = colon + 1
    while (start < json.length && !json[start].isDigit() && json[start] != '-') start++
    var end = start
    while (end < json.length && (json[end].isDigit() || json[end] == '-')) end++
    val value = json.substring(start, end).trim()
    return value.toLongOrNull() ?: 0L
}

fun ViewContainer<*, *>.VideoRender(init: VideoRenderView.() -> Unit) {
    addChild(VideoRenderView(), init)
}
