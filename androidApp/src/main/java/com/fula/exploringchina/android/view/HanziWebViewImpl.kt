package com.fula.exploringchina.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.tencent.kuikly.core.render.android.export.IKuiklyRenderViewExport
import com.tencent.kuikly.core.render.android.export.KuiklyRenderCallback

/**
 * 汉字练习 WebView — 加载 assets/hanzi/index.html（内含 hanzi-writer.min.js）
 *
 * 注册名："HanziWebView"
 * 用法：在 Kuikly DSL 中通过 NativeView("HanziWebView") { } 嵌入
 */
@SuppressLint("SetJavaScriptEnabled")
class HanziWebViewImpl(context: Context) : WebView(context), IKuiklyRenderViewExport {

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            allowFileAccessFromFileURLs = true
            allowUniversalAccessFromFileURLs = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            builtInZoomControls = false
            displayZoomControls = false
        }
        webViewClient = WebViewClient()
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        loadUrl("file:///android_asset/hanzi/index.html")
    }

    override fun setProp(propKey: String, propValue: Any): Boolean = false

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? = null
}
