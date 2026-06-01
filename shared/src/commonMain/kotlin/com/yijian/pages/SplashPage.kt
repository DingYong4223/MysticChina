package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

private const val TAG = "SplashPage"

/**
 * 启动页 — 品牌展示 → 自动跳转主页
 */
@Page("SplashPage", supportInLocal = true)
internal class SplashPage : BasePager() {

    override fun created() {
        super.created()
        KLog.i(TAG, "SplashPage created — 1.8s 后跳转 HomePage")
        setTimeout(1800) {
            KLog.i(TAG, "跳转 → HomePage")
            jumpPage("HomePage")
        }
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(TAG, "SplashPage 已显示")
    }

    override fun pageDidDisappear() {
        super.pageDidDisappear()
        KLog.i(TAG, "SplashPage 已离开")
    }

    override fun body(): ViewBuilder {
        return {
            attr {
                backgroundColor(YijianColors.background)
            }

            // 居中容器
            View {
                attr {
                    flex(1f)
                    allCenter()
                    flexDirectionColumn()
                }

                // Logo 图标
                View {
                    attr {
                        size(90f, 90f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
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
                        color(YijianColors.textSecondary)
                    }
                }
            }

            // 底部版本
            Text {
                attr {
                    absolutePosition(bottom = 40f, left = 0f, right = 0f)
                    textAlignCenter()
                    text("v1.0.0 · Powered by KuiklyUI")
                    fontSize(YijianTheme.FontSize.caption)
                    color(YijianColors.textTertiary)
                }
            }
        }
    }
}