package com.yijian.android

import android.os.Bundle
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import com.tencent.kuikly.core.render.android.IKuiklyRenderExport
import com.tencent.kuikly.core.render.android.KuiklyRenderView
import com.tencent.kuikly.core.render.android.context.KuiklyRenderCoreExecuteModeBase
import com.tencent.kuikly.core.render.android.exception.ErrorReason
import com.tencent.kuikly.core.render.android.expand.KuiklyRenderViewBaseDelegatorDelegate
import com.tencent.kuikly.core.render.android.performace.KRMonitorType
import com.tencent.kuikly.core.render.android.performace.KRPerformanceData
import com.tencent.kuikly.core.render.android.performace.launch.KRLaunchData

class MainActivity : AppCompatActivity() {

    private lateinit var renderView: KuiklyRenderView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        renderView = KuiklyRenderView(
            this,
            KuiklyRenderCoreExecuteModeBase.JVM,
            true,
            object : KuiklyRenderViewBaseDelegatorDelegate {
                override fun registerExternalRenderView(export: IKuiklyRenderExport) {}
                override fun registerExternalModule(export: IKuiklyRenderExport) {}
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
                override fun onUnhandledException(
                    t: Throwable,
                    errorReason: ErrorReason,
                    mode: KuiklyRenderCoreExecuteModeBase
                ) {}
                override fun syncSendEvent(event: String) = false
                override fun useHostDisplayMetrics() = true
                override fun enableContextReplace() = true
            }
        )

        val pageName = intent?.getStringExtra("pageName") ?: "MainPage"
        renderView.init(
            pageName,
            "{}",
            emptyMap<String, Any>(),
            Size(0, 0),
            ""
        )

        setContentView(renderView)
    }

    override fun onResume() {
        super.onResume()
        renderView.resume()
    }

    override fun onPause() {
        super.onPause()
        renderView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        renderView.destroy()
    }

    override fun onBackPressed() {
        renderView.onBackPressed()
        super.onBackPressed()
    }
}
