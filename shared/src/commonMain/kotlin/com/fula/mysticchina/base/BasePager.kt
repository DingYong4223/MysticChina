package com.fula.mysticchina.base

import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.module.Module
import com.tencent.kuikly.core.module.ModuleConst
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.pager.Pager
import com.fula.mysticchina.module.GalleryModule

/**
 * 一剪基础 Pager — 所有页面继承此类
 */
internal abstract class BasePager : Pager() {

    private val router by lazy { acquireModule<RouterModule>(ModuleConst.ROUTER) }

    override fun createExternalModules(): Map<String, Module>? {
        return mapOf(
            GalleryModule.MODULE_NAME to GalleryModule()
        )
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
        router.openPage(pageName, JSONObject(params))
    }

    /**
     * 关闭当前页面
     */
    fun closePage() {
        router.closePage()
    }
}
