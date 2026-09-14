package com.fula.mysticchina.pages

import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.protocol.LEGO_SAMPLE_MODES
import com.fula.mysticchina.protocol.PROTOCOL_PAGE_NAME
import com.fula.mysticchina.protocol.protocolPageParams
import com.fula.mysticchina.theme.MysticChinaColors
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

@Page("LegoPageModesPage", supportInLocal = true)
internal class LegoPageModesPage : BasePager() {
    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { flexDirectionColumn(); backgroundColor(MysticChinaColors.background) }
            View {
                attr {
                    height(52f + ctx.pagerData.statusBarHeight)
                    paddingTop(ctx.pagerData.statusBarHeight)
                    paddingLeft(16f)
                    flexDirectionRow()
                    alignItemsCenter()
                    backgroundColor(MysticChinaColors.background)
                }
                View {
                    attr { size(44f, 44f); allCenter(); accessibility("返回") }
                    event { click { ctx.closePage() } }
                    Text { attr { text("‹"); fontSize(30f); color(MysticChinaColors.textPrimary) } }
                }
                Text { attr { text("LEGO 二级页统一框架"); fontSize(18f); fontWeightSemiBold(); color(MysticChinaColors.textPrimary) } }
            }
            Scroller {
                attr { flex(1f); flexDirectionColumn(); paddingLeft(24f); paddingRight(24f) }
                Text {
                    attr {
                        text("选择页面模式")
                        fontSize(24f)
                        fontWeightBold()
                        color(MysticChinaColors.textPrimary)
                        marginTop(24f)
                        marginBottom(8f)
                    }
                }
                Text {
                    attr {
                        text("来自参考工程的 Puzzle 协议样例")
                        fontSize(13f)
                        color(MysticChinaColors.textSecondary)
                        marginBottom(24f)
                    }
                }
                LEGO_SAMPLE_MODES.forEachIndexed { index, mode ->
                    View {
                        attr {
                            height(64f)
                            flexDirectionRow()
                            alignItemsCenter()
                            borderBottom(com.tencent.kuikly.core.base.Border(1f, com.tencent.kuikly.core.base.BorderStyle.SOLID, MysticChinaColors.divider))
                            accessibility("打开${mode.name}")
                        }
                        event { click { ctx.jumpPage(PROTOCOL_PAGE_NAME, protocolPageParams(mode.protocolJson)) } }
                        Text { attr { text((index + 1).toString().padStart(2, '0')); fontSize(12f); color(MysticChinaColors.textTertiary); width(38f) } }
                        Text { attr { text(mode.name); fontSize(15f); color(MysticChinaColors.textPrimary); flex(1f) } }
                        Text { attr { text("›"); fontSize(24f); color(MysticChinaColors.textSecondary) } }
                    }
                }
                View { attr { height(24f + ctx.pagerData.safeAreaInsets.bottom) } }
            }
        }
    }
}
