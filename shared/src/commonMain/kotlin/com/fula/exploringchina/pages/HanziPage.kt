package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 汉字练习页 — 占位，待后续实现
 */
@Page("HanziPage", supportInLocal = true)
internal class HanziPage : BasePager() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }

            // 顶部导航栏
            View {
                attr {
                    height(YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(YijianColors.background)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    paddingLeft(YijianTheme.Spacing.lg)
                    paddingRight(YijianTheme.Spacing.lg)
                }
                View {
                    attr {
                        size(40f, 40f)
                        allCenter()
                        marginRight(YijianTheme.Spacing.sm)
                    }
                    event { click { ctx.closePage() } }
                    Text {
                        attr {
                            text("‹")
                            fontSize(28f)
                            color(YijianColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                }
                Text {
                    attr {
                        text("汉字练习")
                        fontSize(YijianTheme.FontSize.title)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                        flex(1f)
                    }
                }
            }

            // 内容区 — 占位
            View {
                attr { flex(1f); allCenter(); flexDirectionColumn() }
                Text { attr { text("🖊"); fontSize(64f); marginBottom(YijianTheme.Spacing.xl) } }
                Text {
                    attr {
                        text("汉字练习")
                        fontSize(YijianTheme.FontSize.largeTitle)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                        marginBottom(YijianTheme.Spacing.md)
                    }
                }
                Text {
                    attr {
                        text("精彩内容即将上线")
                        fontSize(YijianTheme.FontSize.body)
                        color(YijianColors.textSecondary)
                    }
                }
            }
        }
    }
}
