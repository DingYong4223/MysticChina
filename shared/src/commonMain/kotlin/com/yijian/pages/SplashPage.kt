package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * 启动页
 *
 * 功能：
 * - 展示应用 Logo / 品牌信息
 * - 短暂延迟后自动跳转到主页
 */
@Page("SplashPage", supportInLocal = true)
internal class SplashPage : BasePager() {

    override fun created() {
        super.created()
        // 延迟 1.5 秒后跳转到主页
        setTimeout(1500) {
            jumpPage("MainPage")
        }
    }

    override fun body(): ViewBuilder {
        return {
            attr {
                backgroundColor(YijianColors.background)
                allCenter()
            }

            // Logo 区域
            View {
                attr {
                    allCenter()
                }

                // 应用图标
                View {
                    attr {
                        size(80f, 80f)
                        backgroundLinearGradient(
                            Direction.TO_BOTTOM_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        borderRadius(20f)
                        allCenter()
                    }
                    Text {
                        attr {
                            text("Y")
                            fontSize(36f)
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
                        fontSize(YijianTheme.FontSize.largeTitle)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                    }
                }

                // 副标题
                Text {
                    attr {
                        marginTop(YijianTheme.Spacing.sm)
                        text("您的智能视频剪辑助手")
                        fontSize(YijianTheme.FontSize.body)
                        color(YijianColors.textSecondary)
                    }
                }
            }

            // 底部版本信息
            Text {
                attr {
                    absolutePosition(bottom = 40f)
                    text("v1.0.0")
                    fontSize(YijianTheme.FontSize.caption)
                    color(YijianColors.textTertiary)
                }
            }
        }
    }
}
