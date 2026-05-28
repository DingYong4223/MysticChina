package com.yijian.theme

import com.tencent.kuikly.core.base.Color

/**
 * 主题配置 — 集中管理字体、间距、圆角等设计 Token
 */
object YijianTheme {

    // 字体大小
    object FontSize {
        val caption = 11f
        val small = 12f
        val body = 14f
        val subtitle = 16f
        val title = 18f
        val largeTitle = 24f
        val display = 32f
    }

    // 间距
    object Spacing {
        val xxs = 2f
        val xs = 4f
        val sm = 8f
        val md = 12f
        val lg = 16f
        val xl = 20f
        val xxl = 24f
        val xxxl = 32f
    }

    // 圆角
    object Radius {
        val sm = 4f
        val md = 8f
        val lg = 12f
        val xl = 16f
        val round = 999f
    }

    // 控制栏高度
    object BarHeight {
        val topBar = 44f
        val bottomBar = 70f
        val progressBar = 3f
        val controlButton = 48f
    }
}
