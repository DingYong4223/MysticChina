package com.fula.mysticchina.android

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.fula.mysticchina.android.view.HanziWebViewImpl
import android.widget.FrameLayout
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.adapter.IKRLogAdapter
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import com.tencent.kuikly.core.render.android.adapter.KuiklyRenderAdapterManager
import com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase
import com.tencent.kuikly.core.render.android.exception.ErrorReason
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegator
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.performace.KRMonitorType
import com.tencent.kuikly.core.render.android.performace.KRPerformanceData
import com.tencent.kuikly.core.render.android.performace.launch.KRLaunchData
import com.fula.mysticchina.App
import com.fula.mysticchina.data.MockAssets
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var containerView: FrameLayout
    private lateinit var delegator: KuiklyRenderViewBaseDelegator

    companion object {
        private const val TAG = "MysticChinaMain"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MockAssets.initialize(applicationContext)

        // ── 沉浸式状态栏 ──────────────────────────────────────
        // 内容延伸到状态栏和导航栏区域，由 Kuikly 页面自行控制 padding
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        // 默认黑白主题使用白底，系统栏图标需为深色。
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = true
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = true

        // 注册 KuiklyUI 内部日志 → Android logcat
        KuiklyRenderAdapterManager.krLogAdapter = object : IKRLogAdapter {
            override val asyncLogEnable = false
            override fun d(tag: String, msg: String) { Log.d("Kuikly/$tag", msg) }
            override fun i(tag: String, msg: String) { Log.i("Kuikly/$tag", msg) }
            override fun e(tag: String, msg: String) { Log.e("Kuikly/$tag", msg) }
        }

        if (KuiklyRenderAdapterManager.krImageAdapter == null) {
            KuiklyRenderAdapterManager.krImageAdapter = KuiklyImageAdapter(applicationContext)
        }

        // 路由适配器 — 处理 RouterModule.openPage / closePage 的 Android 导航
        // openPage: 开启新 Activity（相当于 push），pageName 作为 Intent extra
        // closePage: finish 当前 Activity（相当于 pop）
        if (KuiklyRenderAdapterManager.krRouterAdapter == null) {
            KuiklyRenderAdapterManager.krRouterAdapter = object : IKRRouterAdapter {
                override fun openPage(context: Context, pageName: String, pageData: JSONObject) {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        putExtra("pageName", App.resolvePageName(pageName, pageData.optString("pageID")))
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
                export.renderViewExport("HanziWebView", { HanziWebViewImpl(it) }, null)
            }
            override fun registerExternalModule(export: IKuiklyRenderExport) {}
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

        val pageName = intent?.getStringExtra("pageName") ?: App.START_PAGE
        val pageDataStr = intent?.getStringExtra("pageData") ?: "{}"
        val pageData: Map<String, Any> = try {
            val json = JSONObject(pageDataStr)
            json.keys().asSequence().associateWith { json.get(it) }
        } catch (_: Exception) { emptyMap() }
        Log.d(TAG, "onAttach pageName='$pageName'")
        // contextCode: JVM 模式传 ""；size: null 让框架自行测量
        delegator.onAttach(containerView, "", pageName, pageData, null, null)
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
