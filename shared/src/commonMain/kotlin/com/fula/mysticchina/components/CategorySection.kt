package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.pages.FeatureCategory
import com.fula.mysticchina.pages.FeatureItem
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 主题分区：标题行 + 横向滑动功能卡片行
 *
 * @param ctx     用于点击跳转
 * @param category 分区数据（emoji + name + items）
 */
internal fun ViewContainer<*, *>.CategorySection(ctx: BasePager, category: FeatureCategory) {
    View {
        attr {
            flexDirectionColumn()
            // 显式 height 由子组件确定：标题行(36f) + Scroller(70f)
        }

        // 分区标题行
        View {
            attr {
                height(36f)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingRight(MysticChinaTheme.Spacing.lg)
            }
            Text {
                attr {
                    text(category.emoji)
                    fontSize(12f)
                    marginRight(MysticChinaTheme.Spacing.xs)
                }
            }
            Text {
                attr {
                    text(category.name)
                    fontSize(MysticChinaTheme.FontSize.small)
                    fontWeightBold()
                    color(MysticChinaColors.primaryLight)
                    flex(1f)
                }
            }
            Text {
                attr {
                    text("${category.items.size}个")
                    fontSize(10f)
                    color(MysticChinaColors.textDisabled)
                }
            }
        }

        // 横向滑动功能卡片行
        Scroller {
            attr {
                height(140f)  // 固定高度（2x）：卡片纵 padding(16f) + emoji(44f) + margin(8f) + 两行文字(~32f)
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.lg)
                paddingBottom(MysticChinaTheme.Spacing.md)
            }
            category.items.forEach { item ->
                FeatureCard(ctx, item)
            }
            // 末尾留白，防止最后一张卡片被裁切
            View { attr { width(MysticChinaTheme.Spacing.lg) } }
        }
    }
}

/**
 * 单个功能入口卡片（120dp 宽，2x 大小）
 * 未上线：opacity 0.4 + 右上角「即将」徽章，不响应点击
 */
private fun ViewContainer<*, *>.FeatureCard(ctx: BasePager, item: FeatureItem) {
    val available = item.pageName != null
    View {
        attr {
            width(170f)
            marginRight(MysticChinaTheme.Spacing.sm)
            flexDirectionColumn()
            alignItemsCenter()
            paddingTop(MysticChinaTheme.Spacing.sm)
            paddingBottom(MysticChinaTheme.Spacing.sm)
            paddingLeft(MysticChinaTheme.Spacing.xs)
            paddingRight(MysticChinaTheme.Spacing.xs)
            backgroundColor(
                if (available) MysticChinaColors.surfaceLight else MysticChinaColors.surface
            )
            borderRadius(MysticChinaTheme.Radius.md)
            if (!available) opacity(0.4f)
        }
        if (available) {
            event { click { ctx.jumpPage(item.pageName!!, item.pageParams) } }
        }

        Text {
            attr {
                text(item.emoji)
                fontSize(44f)
                marginBottom(8f)
            }
        }
        Text {
            attr {
                text(item.name)
                fontSize(16f)
                color(MysticChinaColors.cardText)
                textAlignCenter()
                lines(2)
            }
        }

        // 「即将」徽章 — 绝对定位右上角（2x 大小）
        if (!available) {
            View {
                attr {
                    positionAbsolute()
                    top(4f)
                    right(4f)
                    backgroundColor(MysticChinaColors.surface)
                    borderRadius(MysticChinaTheme.Radius.sm)
                    paddingLeft(4f)
                    paddingRight(4f)
                    paddingTop(2f)
                    paddingBottom(2f)
                }
                Text {
                    attr {
                        text("即将")
                        fontSize(12f)
                        color(MysticChinaColors.textDisabled)
                    }
                }
            }
        }
    }
}
