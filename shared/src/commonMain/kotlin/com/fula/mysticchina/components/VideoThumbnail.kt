package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

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
    var placeholderColor: Color = MysticChinaColors.surface
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
                    borderRadius(MysticChinaTheme.Radius.md)
                    overflow(true)
                }

                // 缩略图（通过外部设置 src）
                Image {
                    attr {
                        absolutePositionAllZero()
                        resizeCover()
                        backgroundColor(MysticChinaColors.backgroundLight)
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
                                color(MysticChinaColors.textPrimary)
                            }
                        }
                    }
                }

                // 时长标签
                if (ctx.attr.durationText.isNotEmpty()) {
                    View {
                        attr {
                            absolutePosition(
                                bottom = MysticChinaTheme.Spacing.xs,
                                right = MysticChinaTheme.Spacing.xs
                            )
                            padding(
                                left = MysticChinaTheme.Spacing.sm,
                                right = MysticChinaTheme.Spacing.sm,
                                top = MysticChinaTheme.Spacing.xxs,
                                bottom = MysticChinaTheme.Spacing.xxs
                            )
                            backgroundColor(Color(0xCC000000))
                            borderRadius(MysticChinaTheme.Radius.sm)
                        }
                        Text {
                            attr {
                                text(ctx.attr.durationText)
                                fontSize(MysticChinaTheme.FontSize.caption)
                                color(MysticChinaColors.textPrimary)
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
