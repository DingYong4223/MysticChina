package com.yijian.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import com.yijian.android.view.VideoRenderViewImpl
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.render.android.IKuiklyRenderContext
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.adapter.IKRLogAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase
import com.tencent.kuikly.core.render.android.exception.ErrorReason
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderModuleExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback
import com.tencent.kuikly.core.render.android.performace.KRMonitorType
import com.tencent.kuikly.core.render.android.performace.KRPerformanceData
import com.tencent.kuikly.core.render.android.performace.launch.KRLaunchData
import com.yijian.module.GalleryModule
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var containerView: FrameLayout
    private lateinit var delegator: KuiklyRenderViewBaseDelegator
    private var pendingCallback: KuiklyRenderCallback? = null

    companion object {
        private const val TAG = "YijianMain"
        private const val REQUEST_CODE_PICK_VIDEO = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 注册 KuiklyUI 内部日志 → Android logcat
        KuiklyRenderAdapterManager.krLogAdapter = object : IKRLogAdapter {
            override val asyncLogEnable = false
            override fun d(tag: String, msg: String) { Log.d("Kuikly/$tag", msg) }
            override fun i(tag: String, msg: String) { Log.i("Kuikly/$tag", msg) }
            override fun e(tag: String, msg: String) { Log.e("Kuikly/$tag", msg) }
        }

        // 路由适配器 — 处理 RouterModule.openPage / closePage 的 Android 导航
        // openPage: 开启新 Activity（相当于 push），pageName 作为 Intent extra
        // closePage: finish 当前 Activity（相当于 pop）
        if (KuiklyRenderAdapterManager.krRouterAdapter == null) {
            KuiklyRenderAdapterManager.krRouterAdapter = object : IKRRouterAdapter {
                override fun openPage(context: Context, pageName: String, pageData: JSONObject) {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        putExtra("pageName", pageName)
                        putExtra("pageData", pageData.toString())
                    }
                    context.startActivity(intent)
                }
                override fun closePage(context: Context) {
                    (context as? Activity)?.finish()
                }
            }
        }

        // 容器 View — KuiklyRenderViewBaseDelegator 将 KuiklyRenderView 挂载到此容器
        containerView = FrameLayout(this)
        setContentView(containerView)

        // 标准委托模式：自动注册所有内置 native view 工厂 + module
        delegator = KuiklyRenderViewBaseDelegator(object : KuiklyRenderViewBaseDelegatorDelegate {
            override fun registerExternalRenderView(export: IKuiklyRenderExport) {
                export.renderViewExport("VideoRenderView", { VideoRenderViewImpl(it) }, null)
            }
            override fun registerExternalModule(export: IKuiklyRenderExport) {
                export.moduleExport(GalleryModule.MODULE_NAME) {
                    GalleryModuleExport()
                }
            }
            override fun registerTDFModule(export: IKuiklyRenderExport) {}
            override fun registerViewExternalPropHandler(export: IKuiklyRenderExport) {}
            override fun coreExecuteModeX() = KuiklyRenderCoreExecuteModeBase.JVM
            override fun performanceMonitorTypes() = listOf(KRMonitorType.LAUNCH)
            override fun onKuiklyRenderViewCreated() {
                Log.d(TAG, "onKuiklyRenderViewCreated")
            }
            override fun onKuiklyRenderContentViewCreated() {
                Log.d(TAG, "onKuiklyRenderContentViewCreated ✓")
            }
            override fun syncRenderingWhenPageAppear() = true
            override fun enablePreloadClass() = true
            override fun softInputMode() = null
            override fun onGetLaunchData(data: KRLaunchData) {}
            override fun onGetPerformanceData(data: KRPerformanceData) {}
            override fun onUnhandledException(t: Throwable, errorReason: ErrorReason, mode: KuiklyRenderCoreExecuteModeBase) {
                Log.e(TAG, "KuiklyRender EXCEPTION reason=$errorReason mode=$mode", t)
            }
            override fun syncSendEvent(event: String) = false
            override fun useHostDisplayMetrics() = true
            override fun enableContextReplace() = true
            override fun debugLogEnable() = true
        })

        val pageName = intent?.getStringExtra("pageName") ?: "HomePage"
        val pageDataStr = intent?.getStringExtra("pageData") ?: "{}"
        val pageData: Map<String, Any> = try {
            val json = JSONObject(pageDataStr)
            json.keys().asSequence().associateWith { json.get(it) }
        } catch (_: Exception) { emptyMap() }
        Log.d(TAG, "onAttach pageName='$pageName'")
        // contextCode: JVM 模式传 ""；size: null 让框架自行测量
        delegator.onAttach(containerView, "", pageName, pageData, null, null)
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

    override fun onResume() { super.onResume(); delegator.onResume() }
    override fun onPause() { super.onPause(); delegator.onPause() }
    override fun onDestroy() { super.onDestroy(); delegator.onDetach() }
    override fun onBackPressed() {
        // 让 Kuikly 先处理返回键
        delegator.sendEvent("onBackPressed", emptyMap())
        super.onBackPressed()
    }
}
