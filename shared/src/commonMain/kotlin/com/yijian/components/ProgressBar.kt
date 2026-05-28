package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * ProgressBar 属性
 */
internal class ProgressBarAttr : ComposeAttr() {
    var progress: Float = 0f          // 0.0 ~ 1.0
    var trackColor: Color = YijianColors.progressTrack
    var fillColor: Color = YijianColors.progressFill
    var thumbColor: Color = YijianColors.progressThumb
    var showThumb: Boolean = true
    var barHeight: Float = YijianTheme.BarHeight.progressBar
    var enableDrag: Boolean = true
}

/**
 * ProgressBar 事件
 */
internal class ProgressBarEvent : ComposeEvent() {
    var onProgressChanged: ((Float) -> Unit)? = null
}

/**
 * 自定义进度条组件
 * 支持拖动交互
 */
internal class ProgressBarView : ComposeView<ProgressBarAttr, ProgressBarEvent>() {

    override fun createAttr(): ProgressBarAttr = ProgressBarAttr()

    override fun createEvent(): ProgressBarEvent = ProgressBarEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        val totalWidth = pagerData.pageViewWidth
        return {
            View {
                attr {
                    size(totalWidth, ctx.attr.barHeight + 20f)
                    allCenter()
                }

                // 轨道背景
                View {
                    attr {
                        size(totalWidth - 40f, ctx.attr.barHeight)
                        backgroundColor(ctx.attr.trackColor)
                        borderRadius(ctx.attr.barHeight / 2)
                        flexDirectionRow()
                        alignItemsCenter()
                    }

                    // 已播放进度
                    View {
                        attr {
                            width((totalWidth - 40f) * ctx.attr.progress.coerceIn(0f, 1f))
                            height(ctx.attr.barHeight)
                            backgroundColor(ctx.attr.fillColor)
                            borderRadius(ctx.attr.barHeight / 2)
                        }
                    }

                    // 拖动滑块
                    if (ctx.attr.showThumb && ctx.attr.progress > 0f) {
                        View {
                            attr {
                                size(16f, 16f)
                                borderRadius(8f)
                                backgroundColor(ctx.attr.thumbColor)
                                absolutePosition(
                                    left = (totalWidth - 40f) * ctx.attr.progress.coerceIn(0f, 1f) - 8f + 20f,
                                    top = (ctx.attr.barHeight + 20f - 16f) / 2f
                                )
                                boxShadow(BoxShadow(0f, 0f, 4f, Color(0x80000000)))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 扩展方法
 */
internal fun ViewContainer<*, *>.ProgressBar(init: ProgressBarView.() -> Unit) {
    addChild(ProgressBarView(), init)
}
