package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * BottomBar 属性
 */
internal class BottomBarAttr : ComposeAttr() {
    var backgroundColor: Color = MysticChinaColors.surface
    /** 主导按钮文字 ("导入视频" / "开始创作") */
    var primaryText: String = "导入视频"
}

/**
 * BottomBar 事件
 */
internal class BottomBarEvent : ComposeEvent() {
    var onPrimaryClick: (() -> Unit)? = null
    var onSecondaryClick: (() -> Unit)? = null
}

/**
 * 底部操作栏 — 剪映风格
 *
 * 布局:
 * ┌──────────────────────────────────────┐
 * │  [📂 草稿箱]          [🎬 导入视频]  │
 * └──────────────────────────────────────┘
 */
internal class BottomBarView : ComposeView<BottomBarAttr, BottomBarEvent>() {

    override fun createAttr(): BottomBarAttr = BottomBarAttr()
    override fun createEvent(): BottomBarEvent = BottomBarEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    size(pagerData.pageViewWidth, MysticChinaTheme.BarHeight.bottomBar)
                    backgroundColor(ctx.attr.backgroundColor)
                    flexDirectionRow()
                    alignItemsCenter()
                    justifyContentSpaceBetween()
                    padding(left = MysticChinaTheme.Spacing.lg, right = MysticChinaTheme.Spacing.lg)
                }

                // 左侧：草稿箱入口
                View {
                    attr {
                        flexDirectionRow()
                        alignItemsCenter()
                    }
                    event {
                        click { ctx.event.onSecondaryClick?.invoke() }
                    }
                    Text {
                        attr {
                            text("📂")
                            fontSize(20f)
                            marginRight(MysticChinaTheme.Spacing.sm)
                        }
                    }
                    Text {
                        attr {
                            text("草稿箱")
                            fontSize(MysticChinaTheme.FontSize.body)
                            color(MysticChinaColors.textSecondary)
                        }
                    }
                }

                // 右侧：导入按钮 (主操作)
                View {
                    attr {
                        height(40f)
                        padding(left = MysticChinaTheme.Spacing.xl, right = MysticChinaTheme.Spacing.xl)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(MysticChinaColors.gradientStart, 0f),
                            ColorStop(MysticChinaColors.gradientEnd, 1f)
                        )
                        borderRadius(MysticChinaTheme.Radius.round)
                        allCenter()
                    }
                    event {
                        click { ctx.event.onPrimaryClick?.invoke() }
                    }
                    Text {
                        attr {
                            text(ctx.attr.primaryText)
                            fontSize(MysticChinaTheme.FontSize.subtitle)
                            color(MysticChinaColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                }
            }

            // 安全区域底部间距 (Home Indicator)
            View {
                attr {
                    size(pagerData.pageViewWidth, MysticChinaTheme.Spacing.lg)
                    backgroundColor(ctx.attr.backgroundColor)
                }
            }
        }
    }
}

internal fun ViewContainer<*, *>.BottomBar(init: BottomBarView.() -> Unit) {
    addChild(BottomBarView(), init)
}
