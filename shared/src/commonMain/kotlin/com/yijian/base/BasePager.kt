package com.yijian.base

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.*

/**
 * 一剪基础 Pager — 所有页面继承此类
 */
internal abstract class BasePager : Pager() {

    override fun createExternalModules(): Map<String, Module>? {
        return null
    }

    override fun created() {
        super.created()
    }

    // 不开启调试UI模式
    override fun debugUIInspector(): Boolean {
        return false
    }

    /**
     * 跳转到指定页面
     */
    fun jumpPage(pageName: String, params: String = "{}") {
        getPager().openPage(pageName, params)
    }

    /**
     * 关闭当前页面
     */
    fun closePage() {
        getPager().closeCurrentPage()
    }
}
