package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * VideoThumbnail 属性
 */
internal class VideoThumbnailAttr : ComposeAttr() {
    /** 缩略图宽度 */
    var thumbWidth: Float = 160f
    /** 缩略图高度 (默认 16:9) */
    var thumbHeight: Float? = null
    /** 视频时长文本 (如 "03:25") */
    var durationText: String = ""
    /** 背景占位色 */
    var placeholderColor: Color = YijianColors.surface
}

/**
 * 视频缩略图组件 — 独立的缩略图卡片，用于媒体网格展示。
 */
internal class VideoThumbnailView : ComposeView<VideoThumbnailAttr, ComposeEvent>() {

    override fun createAttr(): VideoThumbnailAttr = VideoThumbnailAttr()
    override fun createEvent(): ComposeEvent = ComposeEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    width(ctx.attr.thumbWidth)
                    height(ctx.attr.thumbHeight ?: (ctx.attr.thumbWidth * 16f / 9f))
                    backgroundColor(ctx.attr.placeholderColor)
                    borderRadius(YijianTheme.Radius.md)
                    overflow(true)
                }

                // 缩略图（通过外部设置 src）
                Image {
                    attr {
                        absolutePositionAllZero()
                        resizeCover()
                        backgroundColor(YijianColors.backgroundLight)
                    }
                }

                // 播放图标叠加层
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

                // 时长标签
                if (ctx.attr.durationText.isNotEmpty()) {
                    View {
                        attr {
                            absolutePosition(
                                bottom = YijianTheme.Spacing.xs,
                                right = YijianTheme.Spacing.xs
                            )
                            padding(
                                left = YijianTheme.Spacing.sm,
                                right = YijianTheme.Spacing.sm,
                                top = YijianTheme.Spacing.xxs,
                                bottom = YijianTheme.Spacing.xxs
                            )
                            backgroundColor(Color(0xCC000000))
                            borderRadius(YijianTheme.Radius.sm)
                        }
                        Text {
                            attr {
                                text(ctx.attr.durationText)
                                fontSize(YijianTheme.FontSize.caption)
                                color(YijianColors.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

internal fun ViewContainer<*, *>.VideoThumbnailCard(init: VideoThumbnailView.() -> Unit) {
    addChild(VideoThumbnailView(), init)
}