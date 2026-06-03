package com.yijian.android.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.yijian.player.SurfaceRegistry

class VideoRenderViewImpl(context: Context) : TextureView(context), IKuiklyRenderViewExport {

    companion object {
        private const val TAG = "VideoRenderView"
        const val KV_SURFACE_CREATED = "surfaceCreated"
        const val KV_SURFACE_DESTROYED = "surfaceDestroyed"
        const val KV_SURFACE_SIZE_CHANGED = "surfaceSizeChanged"
    }

    private var surfaceId: Long = 0L
    private val registeredEvents = mutableMapOf<String, KuiklyRenderCallback?>()

    init {
        isOpaque = false
        isFocusable = false
        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(st: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "onSurfaceTextureAvailable: ${width}x${height}")
                surfaceId = SurfaceRegistry.register(Surface(st))
                fireEvent(KV_SURFACE_CREATED, mapOf(
                    "surfaceId" to surfaceId, "width" to width, "height" to height,
                ))
            }

            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, width: Int, height: Int) {
                Log.d(TAG, "onSurfaceTextureSizeChanged: ${width}x${height}")
                fireEvent(KV_SURFACE_SIZE_CHANGED, mapOf(
                    "surfaceId" to surfaceId, "width" to width, "height" to height,
                ))
            }

            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                Log.d(TAG, "onSurfaceTextureDestroyed: surfaceId=$surfaceId")
                fireEvent(KV_SURFACE_DESTROYED, mapOf("surfaceId" to surfaceId))
                SurfaceRegistry.remove(surfaceId)
                return true
            }

            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
        }
    }

    private fun fireEvent(eventName: String, data: Map<String, Any>) {
        registeredEvents[eventName]?.invoke(data)
    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        return when (propKey) {
            KV_SURFACE_CREATED -> { registeredEvents[KV_SURFACE_CREATED] = propValue as? KuiklyRenderCallback; true }
            KV_SURFACE_DESTROYED -> { registeredEvents[KV_SURFACE_DESTROYED] = propValue as? KuiklyRenderCallback; true }
            KV_SURFACE_SIZE_CHANGED -> { registeredEvents[KV_SURFACE_SIZE_CHANGED] = propValue as? KuiklyRenderCallback; true }
            else -> super.setProp(propKey, propValue)
        }
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
        return when (method) {
            "attachSurface" -> { Log.d(TAG, "call: attachSurface"); null }
            "detachSurface" -> {
                Log.d(TAG, "call: detachSurface surfaceId=$surfaceId")
                SurfaceRegistry.remove(surfaceId); null
            }
            else -> null
        }
    }
}
