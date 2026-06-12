package com.fula.mysticchina.module

import com.tencent.kuikly.core.module.CallbackFn
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

/**
 * GalleryModule — 让 CommonMain 页面触发平台相册/文件选择。
 *
 * Android: 通过 Intent 打开系统文件选择器 (Intent.ACTION_OPEN_DOCUMENT)
 * iOS:     (预留) 通过 PHPickerViewController
 */
class GalleryModule : Module() {

    override fun moduleName(): String = MODULE_NAME

    /**
     * 打开视频选择器。选择完成后通过 callback 返回路径。
     *
     * callback 参数 JSON: { "path": "/storage/emulated/0/Movies/video.mp4", "name": "video.mp4" }
     * 取消时 callback 参数 JSON: { "cancelled": true }
     */
    fun pickVideo(callbackFn: CallbackFn) {
        toNative(
            keepCallbackAlive = false,
            methodName = METHOD_PICK_VIDEO,
            param = null,
            callback = callbackFn,
            syncCall = false
        )
    }

    companion object {
        const val MODULE_NAME = "YijianGallery"
        const val METHOD_PICK_VIDEO = "pickVideo"
    }
}