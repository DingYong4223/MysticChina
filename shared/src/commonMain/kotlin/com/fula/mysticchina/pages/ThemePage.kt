package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme
import com.fula.mysticchina.theme.ThemeManager

private const val SP_THEME_INDEX = "theme_index"

/**
 * 主题选择页 — 提供多套配色方案供用户切换
 * 选中主题后自动保存并生效
 */
@Page("ThemePage", supportInLocal = true)
internal class ThemePage : BasePager() {

    private val sp by lazy { acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME) }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(MysticChinaColors.background); flexDirectionColumn() }

            // ── 顶部导航栏 ──
            View {
                attr {
                    height(MysticChinaTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(MysticChinaColors.backgroundLight)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    paddingLeft(MysticChinaTheme.Spacing.sm)
                    paddingRight(MysticChinaTheme.Spacing.lg)
                }
                View {
                    attr { size(44f, 44f); allCenter() }
                    event { click { ctx.closePage() } }
                    Text { attr { text("‹"); fontSize(28f); color(MysticChinaColors.textPrimary) } }
                }
                View {
                    attr { width(3f); height(16f); borderRadius(2f); backgroundColor(MysticChinaColors.textPrimary); opacity(0.6f); marginRight(MysticChinaTheme.Spacing.sm) }
                }
                Text { attr { text("主题选择"); fontSize(MysticChinaTheme.FontSize.title); fontWeightBold(); color(MysticChinaColors.textPrimary); flex(1f) } }
            }

            // ── 主题列表 ──
            Scroller {
                attr { flex(1f); backgroundColor(MysticChinaColors.background); paddingTop(MysticChinaTheme.Spacing.md); paddingLeft(MysticChinaTheme.Spacing.lg); paddingRight(MysticChinaTheme.Spacing.lg) }

                Text {
                    attr {
                        text("选择你喜欢的配色风格")
                        fontSize(MysticChinaTheme.FontSize.body)
                        color(MysticChinaColors.textSecondary)
                        marginBottom(MysticChinaTheme.Spacing.lg)
                    }
                }

                THEME_OPTIONS.forEachIndexed { index, theme ->
                    ThemeSelectableCard(index, theme, ctx)
                    View { attr { height(MysticChinaTheme.Spacing.md) } }
                }

                View { attr { height(MysticChinaTheme.Spacing.xxl) } }
            }
        }
    }

    /** 保存主题并关闭页面（不更新 ThemeManager，由 HomePage.pageDidAppear 检测后统一更新） */
    internal fun applyAndClose(index: Int) {
        sp.setInt(SP_THEME_INDEX, index)
        closePage()
    }
}

// ═══════════════════════════════════════════════════════════
// 数据模型
// ═══════════════════════════════════════════════════════════

internal data class ThemeOption(
    val name: String,
    val emoji: String,
    val desc: String,
    val bgColor: Long,
    val accentColor: Long,
    val cardColor: Long,
)

internal val THEME_OPTIONS: List<ThemeOption> = listOf(
    ThemeOption("中国红", "🇨🇳", "正红为主，热烈喜庆",
        bgColor = 0xFFCC1111L, accentColor = 0xFFFFD700L, cardColor = 0xFFFFFFFFL),
    ThemeOption("墨玉黑", "🖤", "深邃墨黑背景，沉稳内敛",
        bgColor = 0xFF1A1A1AL, accentColor = 0xFFD4A574L, cardColor = 0xFF2D2D2DL),
    ThemeOption("青花瓷", "🏺", "素雅青白配色，清雅脱俗",
        bgColor = 0xFFE8F0F8L, accentColor = 0xFF264D7CL, cardColor = 0xFFFFFFFFL),
    ThemeOption("水墨灰", "🎨", "水墨丹青，淡雅写意",
        bgColor = 0xFF2C2C2CL, accentColor = 0xFF8B8B8BL, cardColor = 0xFF3E3E3EL),
)

// ═══════════════════════════════════════════════════════════
// UI 组件
// ═══════════════════════════════════════════════════════════

private fun textColorForBg(bgColor: Long): Color {
    return if (bgColor == 0xFFE8F0F8L) Color(0xFF333333) else Color(0xFFFFFFFF)
}

private fun textSecondaryForBg(bgColor: Long): Color {
    return if (bgColor == 0xFFE8F0F8L) Color(0xCC333333.toLong()) else Color(0xCCFFFFFF.toLong())
}

/** 可点击选中的主题卡片 */
private fun ViewContainer<*, *>.ThemeSelectableCard(index: Int, theme: ThemeOption, ctx: ThemePage) {
    val isActive = index == ThemeManager.currentThemeIndex

    View {
        attr {
            flex(1f)
            borderRadius(MysticChinaTheme.Radius.lg)
            overflow(true)
            backgroundColor(Color(theme.bgColor))
            flexDirectionColumn()
            padding(all = MysticChinaTheme.Spacing.lg)
        }
        event { click { ctx.applyAndClose(index) } }

        // 选中指示：右上角打勾
        if (isActive) {
            View {
                attr {
                    positionAbsolute()
                    top(8f)
                    right(8f)
                    size(20f, 20f)
                    borderRadius(10f)
                    backgroundColor(Color(0xCCFFFFFF.toLong()))
                    allCenter()
                }
                Text { attr { text("✓"); fontSize(12f); fontWeightBold(); color(Color(0xFF333333)) } }
            }
        }

        // 标题行
        View {
            attr { flexDirectionRow(); alignItemsCenter(); marginBottom(MysticChinaTheme.Spacing.sm) }
            Text { attr { text(theme.emoji); fontSize(24f); marginRight(MysticChinaTheme.Spacing.sm) } }
            Text {
                attr {
                    text(theme.name); fontSize(MysticChinaTheme.FontSize.subtitle); fontWeightBold()
                    color(textColorForBg(theme.bgColor))
                }
            }
        }

        // 描述
        Text {
            attr {
                text(theme.desc); fontSize(MysticChinaTheme.FontSize.small)
                color(textSecondaryForBg(theme.bgColor))
                marginBottom(MysticChinaTheme.Spacing.md)
            }
        }

        // 色块预览
        View {
            attr { flexDirectionRow(); alignItemsCenter() }
            View {
                attr {
                    size(24f, 24f); borderRadius(6f); backgroundColor(Color(theme.bgColor))
                    marginRight(MysticChinaTheme.Spacing.xs)
                    border(Border(1f, BorderStyle.SOLID, Color(0x33FFFFFF)))
                }
            }
            View {
                attr { size(24f, 24f); borderRadius(6f); backgroundColor(Color(theme.accentColor)); marginRight(MysticChinaTheme.Spacing.xs) }
            }
            View {
                attr {
                    size(24f, 24f); borderRadius(6f); backgroundColor(Color(theme.cardColor))
                    border(Border(1f, BorderStyle.SOLID, Color(0x33FFFFFF)))
                }
            }
        }
    }
}
