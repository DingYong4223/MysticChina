package com.yijian.android

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.render.android.IKuiklyRenderContext
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.KuiklyRenderView
import com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase
import com.tencent.kuikly.core.render.android.exception.ErrorReason
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderModuleExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuikly.core.render.android.performace.KRMonitorType
import com.tencent.kuikly.core.render.android.performace.KRPerformanceData
import com.tencent.kuikly.core.render.android.performace.launch.KRLaunchData
import com.yijian.module.GalleryModule
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var renderView: KuiklyRenderView
    private var pendingCallback: KuiklyRenderCallback? = null

    companion object {
        private const val REQUEST_CODE_PICK_VIDEO = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        renderView = KuiklyRenderView(
            this, KuiklyRenderCoreExecuteModeBase.JVM, true,
            object : KuiklyRenderViewBaseDelegatorDelegate {
                override fun registerExternalRenderView(export: IKuiklyRenderExport) {}
                override fun registerExternalModule(export: IKuiklyRenderExport) {
                    export.moduleExport(GalleryModule.MODULE_NAME) {
                        GalleryModuleExport()
                    }
                }
                override fun registerTDFModule(export: IKuiklyRenderExport) {}
                override fun registerViewExternalPropHandler(export: IKuiklyRenderExport) {}
                override fun coreExecuteModeX() = KuiklyRenderCoreExecuteModeBase.JVM
                override fun performanceMonitorTypes() = listOf(KRMonitorType.LAUNCH)
                override fun onKuiklyRenderViewCreated() {}
                override fun onKuiklyRenderContentViewCreated() {}
                override fun syncRenderingWhenPageAppear() = true
                override fun enablePreloadClass() = true
                override fun softInputMode() = null
                override fun onGetLaunchData(data: KRLaunchData) {}
                override fun onGetPerformanceData(data: KRPerformanceData) {}
                override fun onUnhandledException(t: Throwable, errorReason: ErrorReason, mode: KuiklyRenderCoreExecuteModeBase) {}
                override fun syncSendEvent(event: String) = false
                override fun useHostDisplayMetrics() = true
                override fun enableContextReplace() = true
            }
        )

        val pageName = intent?.getStringExtra("pageName") ?: "SplashPage"
        renderView.init(pageName, "{}", emptyMap(), Size(0, 0), "")
        setContentView(renderView)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_CODE_PICK_VIDEO) return
        val cb = pendingCallback ?: return
        pendingCallback = null
        if (resultCode != RESULT_OK || data?.data == null) {
            cb.invoke("""{"cancelled":true}""")
            return
        }
        try {
            val uri: Uri = data.data!!
            val fileName = getFileName(uri) ?: "video_${System.currentTimeMillis()}.mp4"
            val cacheFile = File(cacheDir, fileName)
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(cacheFile).use { output -> input.copyTo(output) }
            }
            cb.invoke("""{"path":"${cacheFile.absolutePath}","name":"$fileName","size":${cacheFile.length()}}""")
        } catch (e: Exception) {
            cb.invoke("""{"cancelled":true,"error":"${e.message}"}""")
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = cursor.getString(idx)
            }
        }
        return name
    }

    /** GalleryModule 的 Android 实现 */
    inner class GalleryModuleExport : IKuiklyRenderModuleExport {
        override var kuiklyRenderContext: IKuiklyRenderContext? = null
        override val activity: Activity? get() = this@MainActivity

        override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? {
            if (method != GalleryModule.METHOD_PICK_VIDEO) return null
            pendingCallback = callback
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "video/*"
            }
            startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO)
            return null
        }
    }

    override fun onResume() { super.onResume(); renderView.resume() }
    override fun onPause() { super.onPause(); renderView.pause() }
    override fun onDestroy() { super.onDestroy(); renderView.destroy() }
    override fun onBackPressed() { renderView.onBackPressed(); super.onBackPressed() }
}