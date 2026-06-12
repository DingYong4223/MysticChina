package com.fula.mysticchina.android.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.view.TextureView
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.fula.mysticchina.player.SurfaceRegistry

class VideoRenderViewImpl(context: Context) : TextureView(context), IKuiklyRenderViewExport {

    companion object {
        private const val TAG = "VideoRenderView"
        const val EV_SURFACE_READY = "surfaceReady"
        const val EV_SURFACE_DESTROYED = "surfaceDestroyed"
        const val EV_SURFACE_SIZE_CHANGED = "surfaceSizeChanged"
    }

    private var surfaceId: Long = 0L
    private val callbacks = mutableMapOf<String, KuiklyRenderCallback?>()
    private val pendingData = mutableMapOf<String, Any>()

    init {
        isOpaque = false
        isFocusable = false
        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(st: SurfaceTexture, w: Int, h: Int) {
                Log.d(TAG, "onSurfaceTextureAvailable: ${w}x${h}")
                surfaceId = SurfaceRegistry.register(Surface(st))
                val data = mapOf("surfaceId" to surfaceId, "width" to w, "height" to h)
                deliverOrBuffer(EV_SURFACE_READY, data)
            }

            override fun onSurfaceTextureSizeChanged(st: SurfaceTexture, w: Int, h: Int) {
                Log.d(TAG, "onSurfaceTextureSizeChanged: ${w}x${h}")
                val data = mapOf("surfaceId" to surfaceId, "width" to w, "height" to h)
                deliverOrBuffer(EV_SURFACE_SIZE_CHANGED, data)
            }

            override fun onSurfaceTextureDestroyed(st: SurfaceTexture): Boolean {
                Log.d(TAG, "onSurfaceTextureDestroyed: surfaceId=$surfaceId")
                deliverOrBuffer(EV_SURFACE_DESTROYED, mapOf("surfaceId" to surfaceId))
                SurfaceRegistry.remove(surfaceId)
                return true
            }

            override fun onSurfaceTextureUpdated(st: SurfaceTexture) {}
        }
    }

    private fun deliverOrBuffer(eventName: String, data: Any) {
        val cb = callbacks[eventName]
        if (cb != null) {
            Log.d(TAG, "deliver $eventName: invoking callback")
            cb.invoke(data)
        } else {
            Log.d(TAG, "buffer $eventName: callback not registered yet")
            pendingData[eventName] = data
        }
    }

    override fun setProp(propKey: String, propValue: Any): Boolean {
        return when (propKey) {
            EV_SURFACE_READY, EV_SURFACE_DESTROYED, EV_SURFACE_SIZE_CHANGED -> {
                callbacks[propKey] = propValue as? KuiklyRenderCallback
                // Replay any buffered event
                pendingData.remove(propKey)?.let { data ->
                    Log.d(TAG, "setProp $propKey: replaying buffered data")
                    (propValue as? KuiklyRenderCallback)?.invoke(data)
                }
                true
            }
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
