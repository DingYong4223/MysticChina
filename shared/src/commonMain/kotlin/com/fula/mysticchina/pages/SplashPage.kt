package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

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
                backgroundColor(MysticChinaColors.background)
            }

            View {
                attr {
                    flex(1f)
                    allCenter()
                    flexDirectionColumn()
                }

                // 中国结图标区域：红色圆形背景 + 中国结 emoji
                View {
                    attr {
                        size(90f, 90f)
                        backgroundColor(MysticChinaColors.primary)
                        borderRadius(45f)   // 圆形
                        allCenter()
                    }
                    Text {
                        attr {
                            text("🏮")      // 红灯笼，中国传统文化象征
                            fontSize(46f)
                        }
                    }
                }

                Text {
                    attr {
                        marginTop(MysticChinaTheme.Spacing.xl)
                        text("神秘中国")
                        fontSize(MysticChinaTheme.FontSize.display)
                        color(MysticChinaColors.textPrimary)
                        fontWeightBold()
                    }
                }

                Text {
                    attr {
                        marginTop(MysticChinaTheme.Spacing.sm)
                        text("探索中华文化之美")
                        fontSize(MysticChinaTheme.FontSize.body)
                        color(MysticChinaColors.textSecondary)
                    }
                }
            }

            Text {
                attr {
                    absolutePosition(bottom = 40f, left = 0f, right = 0f)
                    textAlignCenter()
                    text("v1.0.0 · Powered by KuiklyUI")
                    fontSize(MysticChinaTheme.FontSize.caption)
                    color(MysticChinaColors.textTertiary)
                }
            }
        }
    }
}
