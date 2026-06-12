package com.fula.mysticchina.android.view

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
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
 *
 * XHR 兼容：通过 loadDataWithBaseURL 设置虚拟 HTTP origin (http://hanzi/)，
 *           使 JS 的 XHR/fetch 请求变为同源，配合 shouldInterceptRequest
 *           拦截虚拟域名的请求并从本地 assets 返回数据，绕过 file:// 协议下
 *           CORS 拦截（API 31+ Chromium 安全策略）。
 */
@SuppressLint("SetJavaScriptEnabled")
class HanziWebViewImpl(context: Context) : WebView(context), IKuiklyRenderViewExport {

    /** 虚拟 base URL，用于让 JS 的 XHR 视为同源请求 */
    private companion object {
        const val BASE_URL = "http://hanzi/"
    }

    init {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_NO_CACHE
            builtInZoomControls = false
            displayZoomControls = false
        }
        webViewClient = object : WebViewClient() {
            /**
             * 拦截虚拟域名 http://hanzi/ 下的所有请求，
             * 从 assets/hanzi/ 加载对应的本地文件（HTML 内的 JS、JSON 等）。
             */
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                if (url.startsWith(BASE_URL)) {
                    val assetPath = "hanzi/" + java.net.URLDecoder.decode(
                        url.removePrefix(BASE_URL), "UTF-8"
                    )
                    val mimeType = when {
                        assetPath.endsWith(".js") -> "application/javascript"
                        assetPath.endsWith(".json") -> "application/json"
                        assetPath.endsWith(".css") -> "text/css"
                        assetPath.endsWith(".html") -> "text/html"
                        else -> "application/octet-stream"
                    }
                    return try {
                        val inputStream = context.assets.open(assetPath)
                        WebResourceResponse(mimeType, "UTF-8", inputStream)
                    } catch (e: Exception) {
                        super.shouldInterceptRequest(view, request)
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
        }
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
    }

    /**
     * 响应 "src" prop：加载 assets/hanzi/{value} 页面。
     * 使用 loadDataWithBaseURL 将基础 URL 设为 http://hanzi/，
     * 使 XHR /fetch 请求变为同源，避开 file:// 的 CORS 限制。
     */
    override fun setProp(propKey: String, propValue: Any): Boolean {
        if (propKey == "src") {
            val filename = propValue.toString()
            // 防路径遍历：文件名不得包含目录分隔符或上级引用
            if (filename.contains('/') || filename.contains("..")) return false
            val html = try {
                context.assets.open("hanzi/$filename").bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                return false
            }
            loadDataWithBaseURL(BASE_URL, html, "text/html", "UTF-8", null)
            return true
        }
        // 将 FRAME 等布局属性交给基类处理
        return super.setProp(propKey, propValue)
    }

    override fun call(method: String, params: String?, callback: KuiklyRenderCallback?): Any? = null
}
