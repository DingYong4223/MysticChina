package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.pages.FeaturedCard
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 顶部精选轮播：横向可滑动的精选卡片 + 静态圆点指示器。
 * 圆点为静态显示（首个高亮），动态追踪滚动位置标记为 future enhancement。
 *
 * @param ctx   用于点击跳转已上线卡片
 * @param cards 精选卡片列表（来自 FEATURED_CARDS）
 */
internal fun ViewContainer<*, *>.FeaturedCarousel(ctx: BasePager, cards: List<FeaturedCard>) {
    val cardWidth = ctx.pagerData.pageViewWidth * 0.68f

    View {
        attr { flexDirectionColumn() }

        // 横向滑动卡片区
        Scroller {
            attr {
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingTop(MysticChinaTheme.Spacing.sm)
                paddingBottom(MysticChinaTheme.Spacing.xs)
            }
            cards.forEach { card ->
                FeaturedCarouselCard(ctx, card, cardWidth)
            }
            // 末尾留白，确保最后一张卡片可完整滑入视图
            View { attr { width(MysticChinaTheme.Spacing.lg) } }
        }

        // 圆点指示器（静态：首个圆点高亮，其余暗淡）
        View {
            attr {
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingTop(MysticChinaTheme.Spacing.xs)
                paddingBottom(MysticChinaTheme.Spacing.sm)
            }
            cards.forEachIndexed { index, _ ->
                View {
                    attr {
                        height(4f)
                        width(if (index == 0) 14f else 4f)
                        borderRadius(2f)
                        marginRight(4f)
                        backgroundColor(
                            if (index == 0) MysticChinaColors.dotActive
                            else MysticChinaColors.dotInactive
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单张精选卡片：渐变背景 + 标签 + 标题 + 副标题。
 * 已上线（pageName != null）点击跳转，未上线不响应。
 */
private fun ViewContainer<*, *>.FeaturedCarouselCard(
    ctx: BasePager,
    card: FeaturedCard,
    width: Float,
) {
    val available = card.pageName != null
    View {
        attr {
            width(width)
            height(88f)
            marginRight(MysticChinaTheme.Spacing.sm)
            borderRadius(MysticChinaTheme.Radius.lg)
            overflow(true)
            backgroundLinearGradient(
                Direction.TO_BOTTOM_RIGHT,
                ColorStop(card.gradientStart, 0f),
                ColorStop(card.gradientEnd, 1f),
            )
            flexDirectionColumn()
            padding(all = MysticChinaTheme.Spacing.md)
        }
        if (available) {
            event { click { ctx.jumpPage(card.pageName!!) } }
        }

        // 标签行（左上角半透明背景）
        View {
            attr {
                backgroundColor(Color(0x2EFFFFFF))
                borderRadius(MysticChinaTheme.Radius.sm)
                paddingLeft(MysticChinaTheme.Spacing.xs)
                paddingRight(MysticChinaTheme.Spacing.xs)
                paddingTop(2f)
                paddingBottom(2f)
                marginBottom(MysticChinaTheme.Spacing.md)
            }
            Text {
                attr {
                    text(card.tag)
                    fontSize(8f)
                    color(Color(0xF2FFFFFF))
                }
            }
        }

        // 标题 + 副标题（靠底部）
        View {
            attr { flexDirectionColumn(); flex(1f); justifyContentFlexEnd() }
            Text {
                attr {
                    text(card.title)
                    fontSize(MysticChinaTheme.FontSize.subtitle)
                    fontWeightBold()
                    color(MysticChinaColors.textPrimary)
                    marginBottom(2f)
                }
            }
            Text {
                attr {
                    text(card.subtitle)
                    fontSize(MysticChinaTheme.FontSize.small)
                    color(Color(0xA6FFFFFF))
                    lines(1)
                }
            }
        }
    }
}
