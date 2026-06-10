package com.fula.exploringchina.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

/**
 * 汉字练习 WebView — 加载 assets/hanzi/ 下的 HTML 文件。
 *
 * 注册名："HanziWebView"
 * 加载时机：KuiklyUI 完成 FlexBox 布局后调用 setProp("src", filename)，
 *           此时 WebView 已有正确的 Android 视图宽度，viewport 计算更准确。
 */
@SuppressLint("SetJavaScriptEnabled")
class HanziWebViewImpl(context: Context) : WebView(context), IKuiklyRenderViewExport {

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            // allowFileAccessFromFileURLs / allowUniversalAccessFromFileURLs 已在 API 30 废弃，
            // API 31+ 被静默忽略；同源的 file:// XHR 不需要这两个标志。
            cacheMode = WebSettings.LOAD_NO_CACHE
            builtInZoomControls = false
            displayZoomControls = false
        }
        webViewClient = WebViewClient()
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        // loadUrl 不在此处调用 — 等待 KuiklyUI 通过 setProp("src", …) 触发
    }

    /**
     * 响应 "src" prop：加载 assets/hanzi/{value} 页面。
     * KuiklyUI 在完成布局后才调用此方法，WebView 届时已有正确宽度。
     */
    override fun setProp(propKey: String, propValue: Any): Boolean {
        if (propKey == "src") {
            val filename = propValue.toString()
            // 防路径遍历：文件名不得包含目录分隔符或上级引用
            if (filename.contains('/') || filename.contains("..")) return false
            loadUrl("file:///android_asset/hanzi/$filename")
            return true
        }
        return false
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? = null
}
