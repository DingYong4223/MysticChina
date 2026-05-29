package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * IconButton 属性
 */
internal class IconButtonAttr : ComposeAttr() {
    var icon: String = "▶"
    var iconSize: Float = 24f
    var iconColor: Color = YijianColors.textPrimary
    var buttonSize: Float = YijianTheme.BarHeight.controlButton
    var backgroundColor: Color = YijianColors.overlay
    var borderRadius: Float = YijianTheme.Radius.round
}

/**
 * 图标按钮组件 — 播放/暂停/返回等圆形图标按钮，剪映风格。
 */
internal class IconButtonView : ComposeView<IconButtonAttr, ComposeEvent>() {

    override fun createAttr(): IconButtonAttr = IconButtonAttr()
    override fun createEvent(): ComposeEvent = ComposeEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    size(ctx.attr.buttonSize, ctx.attr.buttonSize)
                    backgroundColor(ctx.attr.backgroundColor)
                    borderRadius(ctx.attr.borderRadius)
                    allCenter()
                }
                event { click { /* 点击事件由外部监听 */ } }
                Text {
                    attr {
                        text(ctx.attr.icon)
                        fontSize(ctx.attr.iconSize)
                        color(ctx.attr.iconColor)
                    }
                }
            }
        }
    }
}

internal fun ViewContainer<*, *>.IconButton(init: IconButtonView.() -> Unit) {
    addChild(IconButtonView(), init)
}