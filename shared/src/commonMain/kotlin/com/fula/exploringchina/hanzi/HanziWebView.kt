package com.fula.exploringchina.hanzi

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.base.event.Event

/**
 * 汉字练习 WebView — 通过 Kuikly expand-native-view 机制桥接到平台 WebView。
 *
 * Android 侧实现：HanziWebViewImpl（加载 assets/hanzi/index.html）
 *
 * 用法：
 *   HanziWeb {
 *       attr { flex(1f) }
 *   }
 */
class HanziWebView : DeclarativeBaseView<Attr, Event>() {
    override fun createAttr(): Attr = Attr()
    override fun createEvent(): Event = Event()
    override fun viewName(): String = "HanziWebView"
}

fun ViewContainer<*, *>.HanziWeb(init: HanziWebView.() -> Unit) {
    addChild(HanziWebView(), init)
}
