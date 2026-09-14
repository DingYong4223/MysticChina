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

        // 主内容区（可滚动，顶部留状态栏间距）
        Scroller {
            attr {
                flex(1f)
                flexDirectionColumn()
                backgroundColor(MysticChinaColors.background)
                paddingTop(ctx.pagerData.statusBarHeight)
            }

            // 精选轮播
            FeaturedCarousel(ctx, FEATURED_CARDS)

            // 分区分割线 + 各主题分区
            EXPLORE_CATEGORIES.forEachIndexed { index, category ->
                // 分区分割线
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
