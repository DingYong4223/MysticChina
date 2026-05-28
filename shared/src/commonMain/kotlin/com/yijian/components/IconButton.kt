package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * IconButton 属性
 */
internal class IconButtonAttr : ComposeAttr() {
    var icon: String = "▶"           // 使用Unicode符号作为图标
    var iconSize: Float = 24f
    var iconColor: Color = YijianColors.textPrimary
    var buttonSize: Float = YijianTheme.BarHeight.controlButton
    var backgroundColor: Color = YijianColors.overlay
    var borderRadius: Float = YijianTheme.Radius.round
}

/**
 * 图标按钮组件 — 播放/暂停等
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
                event {
                    click {
                        // 点击事件由外部监听
                    }
                }
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

/**
 * 扩展方法
 */
internal fun ViewContainer<*, *>.IconButton(init: IconButtonView.() -> Unit) {
    addChild(IconButtonView(), init)
}

/**
 * 视频缩略图组件
 */
internal class VideoThumbnailView : ComposeView<ComposeAttr, ComposeEvent>() {

    override fun createAttr(): ComposeAttr = ComposeAttr()

    override fun createEvent(): ComposeEvent = ComposeEvent()

    override fun body(): ViewBuilder {
        return {
            View {
                attr {
                    width(pagerData.pageViewWidth / 2 - 20f)
                    height((pagerData.pageViewWidth / 2 - 20f) * 16f / 9f)
                    backgroundColor(YijianColors.surface)
                    borderRadius(YijianTheme.Radius.md)
                }
                // 缩略图（支持外部设置src）
                Image {
                    attr {
                        size(width = pagerData.pageViewWidth / 2 - 20f, height = (pagerData.pageViewWidth / 2 - 20f) * 16f / 9f)
                        resizeCover()
                        backgroundColor(YijianColors.backgroundLight)
                        borderRadius(YijianTheme.Radius.md)
                    }
                }
                // 播放图标覆盖
                View {
                    attr {
                        absolutePositionAllZero()
                        allCenter()
                    }
                    View {
                        attr {
                            size(36f, 36f)
                            borderRadius(18f)
                            backgroundColor(Color(0x80000000))
                            allCenter()
                        }
                        Text {
                            attr {
                                text("▶")
                                fontSize(16f)
                                color(YijianColors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun ViewContainer<*, *>.VideoThumbnail(init: VideoThumbnailView.() -> Unit) {
    addChild(VideoThumbnailView(), init)
}
