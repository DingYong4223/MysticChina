package com.fula.exploringchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.module.ModuleConst
import com.tencent.kuikly.core.module.RouterModule
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * TopBar 属性
 */
internal class TopBarAttr : ComposeAttr() {
    var title: String = ""
    var showBack: Boolean = true
    var backgroundColor: Color = YijianColors.surface
}

/**
 * TopBar 事件
 */
internal class TopBarEvent : ComposeEvent()

/**
 * 顶部导航栏组件
 * 仿剪映风格 — 深色背景，左侧返回按钮，中央标题
 */
internal class TopBarView : ComposeView<TopBarAttr, TopBarEvent>() {

    override fun createAttr(): TopBarAttr = TopBarAttr()

    override fun createEvent(): TopBarEvent = TopBarEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            View {
                attr {
                    size(pagerData.pageViewWidth, YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(ctx.attr.backgroundColor)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                }

                // 返回按钮
                if (ctx.attr.showBack) {
                    View {
                        attr {
                            size(YijianTheme.BarHeight.topBar, YijianTheme.BarHeight.topBar)
                            allCenter()
                        }
                        event {
                            click {
                                ctx.getPager().acquireModule<RouterModule>(ModuleConst.ROUTER).closePage()
                            }
                        }
                        // 返回箭头 (使用Unicode字符或自定义绘制)
                        Text {
                            attr {
                                text("<")
                                fontSize(22f)
                                color(YijianColors.textPrimary)
                            }
                        }
                    }
                }

                // 标题
                Text {
                    attr {
                        flex(1f)
                        text(ctx.attr.title)
                        fontSize(YijianTheme.FontSize.title)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                        textAlignCenter()
                        marginRight(if (ctx.attr.showBack) YijianTheme.BarHeight.topBar else 0f)
                    }
                }
            }
        }
    }
}

/**
 * 扩展方法 — 方便在 Pager 中使用 TopBar
 */
internal fun ViewContainer<*, *>.TopBar(init: TopBarView.() -> Unit) {
    addChild(TopBarView(), init)
}
