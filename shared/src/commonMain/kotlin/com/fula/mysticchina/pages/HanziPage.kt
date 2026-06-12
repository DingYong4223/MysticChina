package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.hanzi.HanziWeb
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 汉字练习页 — 顶部导航栏 + 原生 WebView（加载 game.html 练字测验）
 */
@Page("HanziPage", supportInLocal = true)
internal class HanziPage : BasePager() {

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(MysticChinaColors.background); flexDirectionColumn() }

            // ── 顶部导航栏 ──────────────────────────────────────
            View {
                attr {
                    height(MysticChinaTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(MysticChinaColors.background)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    paddingLeft(MysticChinaTheme.Spacing.sm)
                    paddingRight(MysticChinaTheme.Spacing.lg)
                }

                // 返回按钮
                View {
                    attr { size(44f, 44f); allCenter() }
                    event { click { ctx.closePage() } }
                    Text {
                        attr {
                            text("‹")
                            fontSize(30f)
                            color(MysticChinaColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                }

                // 标题
                Text {
                    attr {
                        text("汉字练习")
                        fontSize(MysticChinaTheme.FontSize.title)
                        color(MysticChinaColors.textPrimary)
                        fontWeightBold()
                        flex(1f)
                    }
                }
            }

            // 分隔线
            View { attr { height(1f); backgroundColor(MysticChinaColors.surfaceLight) } }

            // ── 汉字 WebView（占满剩余高度） ─────────────────────
            // flex(1f) 填充高度；width 显式指定宽度（KuiklyUI 原生 View 不自动 stretch）
            // src 指定加载 assets/hanzi/ 下的 HTML 文件
            HanziWeb {
                attr {
                    flex(1f)
                    width(ctx.pagerData.pageViewWidth)
                    src("game.html")
                }
            }
        }
    }
}
