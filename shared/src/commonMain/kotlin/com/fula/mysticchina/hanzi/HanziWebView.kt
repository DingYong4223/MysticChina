package com.fula.mysticchina.hanzi

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.base.event.Event

/**
 * 汉字练习 WebView — 通过 Kuikly expand-native-view 机制桥接到平台 WebView。
 *
 * Android 侧实现：HanziWebViewImpl
 *
 * 用法：
 *   HanziWeb {
 *       attr {
 *           flex(1f)
 *           width(pageViewWidth)
 *           src("game.html")   // 指定 assets/hanzi/ 下的 HTML 文件名
 *       }
 *   }
 */

/** 控制 HanziWebView 行为的属性类。 */
class HanziWebAttr : Attr() {
    /**
     * 指定要加载的 HTML 文件名（相对于 assets/hanzi/），例如 "game.html"。
     * 通过 KuiklyUI prop 机制发往 Android 端，触发 HanziWebViewImpl.setProp("src", …)。
     */
    fun src(url: String): HanziWebAttr {
        "src" with url
        return this
    }
}

class HanziWebView : DeclarativeBaseView<HanziWebAttr, Event>() {
    override fun createAttr(): HanziWebAttr = HanziWebAttr()
    override fun createEvent(): Event = Event()
    override fun viewName(): String = "HanziWebView"
}

fun ViewContainer<*, *>.HanziWeb(init: HanziWebView.() -> Unit) {
    addChild(HanziWebView(), init)
}
