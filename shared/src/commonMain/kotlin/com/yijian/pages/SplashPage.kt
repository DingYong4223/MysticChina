package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * 启动页 — 品牌展示 → 自动跳转主页
 */
@Page("SplashPage", supportInLocal = true)
internal class SplashPage : BasePager() {

    private var showContent by observable(false)

    override fun created() {
        super.created()
        // 延迟展示内容 + 跳转
        setTimeout(300) { showContent = true }
        setTimeout(1800) { jumpPage("MainPage") }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                size(pagerData.pageViewWidth, pagerData.pageViewHeight)
                backgroundLinearGradient(
                    Direction.TO_BOTTOM_RIGHT,
                    ColorStop(YijianColors.background, 0f),
                    ColorStop(Color(0xFF0D0D1A), 0.5f),
                    ColorStop(YijianColors.background, 1f)
                )
                allCenter()
                flexDirectionColumn()
            }

            // Logo 图标
            View {
                attr {
                    size(90f, 90f)
                    backgroundLinearGradient(
                        Direction.TO_BOTTOM_RIGHT,
                        ColorStop(YijianColors.gradientStart, 0f),
                        ColorStop(YijianColors.gradientEnd, 1f)
                    )
                    borderRadius(22f)
                    allCenter()
                }
                Text {
                    attr {
                        text("W")
                        fontSize(42f)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                    }
                }
            }

            // 应用名称
            Text {
                attr {
                    marginTop(YijianTheme.Spacing.xl)
                    text("一剪")
                    fontSize(YijianTheme.FontSize.display)
                    color(YijianColors.textPrimary)
                    fontWeightBold()
                }
            }

            // 副标题
            Text {
                attr {
                    marginTop(YijianTheme.Spacing.sm)
                    text("你的智能视频剪辑助手")
                    fontSize(YijianTheme.FontSize.body)
                    color(YijianColors.textTertiary)
                }
            }

            // 底部文字
            View {
                attr {
                    absolutePosition(bottom = 60f)
                    allCenter()
                }
                Text {
                    attr {
                        text("v1.0.0 · Powered by KuiklyUI")
                        fontSize(YijianTheme.FontSize.caption)
                        color(YijianColors.textTertiary)
                    }
                }
            }
        }
    }
}