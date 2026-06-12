package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.pages.EXPLORE_CATEGORIES
import com.fula.mysticchina.pages.FEATURED_CARDS
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 探索 Tab 内容。
 * 组装：标题栏 → 精选轮播 → 五个主题分区。
 *
 * 在 HomePage.body() 的 Tab 0 分支调用：ExploreTabContent(ctx)
 */
internal fun ViewContainer<*, *>.ExploreTabContent(ctx: BasePager) {
    View {
        attr {
            flex(1f)
            flexDirectionColumn()
            backgroundColor(MysticChinaColors.background)
        }

        // 顶部标题栏（statusBar + 44dp topBar）
        View {
            attr {
                height(MysticChinaTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
                backgroundColor(MysticChinaColors.backgroundLight)
                flexDirectionRow()
                alignItemsCenter()
                paddingTop(ctx.pagerData.statusBarHeight)
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingRight(MysticChinaTheme.Spacing.lg)
            }
            // 左侧白色竖条装饰
            View {
                attr {
                    width(3f)
                    height(16f)
                    borderRadius(2f)
                    backgroundColor(MysticChinaColors.textPrimary)
                    opacity(0.6f)
                    marginRight(MysticChinaTheme.Spacing.sm)
                }
            }
            Text {
                attr {
                    text("探索")
                    fontSize(MysticChinaTheme.FontSize.title)
                    fontWeightBold()
                    color(MysticChinaColors.textPrimary)
                }
            }
        }

        // 主内容区（可滚动）
        Scroller {
            attr {
                flex(1f)
                flexDirectionColumn()
                backgroundColor(MysticChinaColors.background)
            }

            // 精选轮播
            FeaturedCarousel(ctx, FEATURED_CARDS)

            // 分区分割线 + 各主题分区
            EXPLORE_CATEGORIES.forEachIndexed { index, category ->
                // 分割线
                View {
                    attr {
                        height(1f)
                        backgroundColor(MysticChinaColors.divider)
                        marginLeft(MysticChinaTheme.Spacing.lg)
                        marginRight(MysticChinaTheme.Spacing.lg)
                        marginTop(if (index == 0) MysticChinaTheme.Spacing.xs else 0f)
                    }
                }
                CategorySection(ctx, category)
            }

            // 底部安全间距
            View { attr { height(MysticChinaTheme.Spacing.xxl) } }
        }
    }
}
