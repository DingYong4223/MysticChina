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
        attr { flexDirectionColumn() }

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
 * 单个功能入口卡片（60dp 宽）
 * 未上线：opacity 0.4 + 右上角「即将」徽章，不响应点击
 */
private fun ViewContainer<*, *>.FeatureCard(ctx: BasePager, item: FeatureItem) {
    val available = item.pageName != null
    View {
        attr {
            width(60f)
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
            event { click { ctx.jumpPage(item.pageName!!) } }
        }

        Text {
            attr {
                text(item.emoji)
                fontSize(22f)
                marginBottom(4f)
            }
        }
        Text {
            attr {
                text(item.name)
                fontSize(8f)
                color(MysticChinaColors.textSecondary)
                textAlignCenter()
                lines(2)
            }
        }

        // 「即将」徽章 — 绝对定位右上角
        if (!available) {
            View {
                attr {
                    positionAbsolute()
                    top(2f)
                    right(2f)
                    backgroundColor(MysticChinaColors.surface)
                    borderRadius(MysticChinaTheme.Radius.sm)
                    paddingLeft(2f)
                    paddingRight(2f)
                    paddingTop(1f)
                    paddingBottom(1f)
                }
                Text {
                    attr {
                        text("即将")
                        fontSize(6f)
                        color(MysticChinaColors.textDisabled)
                    }
                }
            }
        }
    }
}
