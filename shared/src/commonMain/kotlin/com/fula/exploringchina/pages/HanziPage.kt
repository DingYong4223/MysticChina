package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.hanzi.HanziWeb
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 汉字练习页 — 顶部导航栏 + 原生 WebView（加载 practise.html 练字测验）
 */
@Page("HanziPage", supportInLocal = true)
internal class HanziPage : BasePager() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }

            // ── 顶部导航栏 ──────────────────────────────────────
            View {
                attr {
                    height(YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(YijianColors.background)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    paddingLeft(YijianTheme.Spacing.sm)
                    paddingRight(YijianTheme.Spacing.lg)
                }

                // 返回按钮
                View {
                    attr { size(44f, 44f); allCenter() }
                    event { click { ctx.closePage() } }
                    Text {
                        attr {
                            text("‹")
                            fontSize(30f)
                            color(YijianColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                }

                // 标题
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

            // 分隔线
            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }

            // ── 汉字 WebView（占满剩余高度） ─────────────────────
            // flex(1f) 填充高度；width 显式指定宽度（KuiklyUI 原生 View 不自动 stretch）
            // src 指定加载 assets/hanzi/ 下的 HTML 文件
            HanziWeb {
                attr {
                    flex(1f)
                    width(ctx.pagerData.pageViewWidth)
                    src("practise.html")
                }
            }
        }
    }
}
