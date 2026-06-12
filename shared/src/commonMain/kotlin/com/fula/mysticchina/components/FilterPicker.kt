package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.model.BuiltinFilters
import com.fula.mysticchina.model.FilterEffect
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 滤镜选择面板 — 以横向滚动网格展示所有内置 LUT 滤镜。
 *
 * 当前用图标占位符表示预览缩略图，
 * 正式版替换为抽取的视频帧 + LUT 实时预览。
 */
internal class FilterPicker(
    internal val onSelected: (FilterEffect) -> Unit,
    internal val onDismiss: () -> Unit,
) {
    var selectedId by observable("none")

    val filters: List<FilterEffect> get() = BuiltinFilters.list
}

internal fun ViewContainer<*, *>.FilterPickerView(ctx: FilterPicker) {
    View {
        attr {
            backgroundColor(Color(0xFF1A1A1A))
            flexDirectionColumn()
        }

        // 标题栏
        View {
            attr {
                flexDirectionRow()
                alignItemsCenter()
                justifyContentSpaceBetween()
                height(40f)
                paddingLeft(MysticChinaTheme.Spacing.md)
                paddingRight(MysticChinaTheme.Spacing.md)
            }
            Text {
                attr {
                    text("滤镜"); fontSize(16f); color(MysticChinaColors.textPrimary)
                    fontWeightSemiBold()
                }
            }
            View {
                attr { size(36f, 36f); allCenter() }
                event { click { ctx.onDismiss.invoke() } }
                Text {
                    attr {
                        text("✕"); fontSize(18f); color(MysticChinaColors.textSecondary)
                    }
                }
            }
        }

        // 滤镜网格（横向滚动）
        Scroller {
            attr {
                height(100f)
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.sm)
                paddingRight(MysticChinaTheme.Spacing.sm)
                showScrollerIndicator(false)
            }

            for (filter in ctx.filters) {
                FilterThumb(ctx, filter)
            }
        }
    }
}

private fun ViewContainer<*, *>.FilterThumb(ctx: FilterPicker, filter: FilterEffect) {
    val isSelected = ctx.selectedId == filter.id
    val cellW = 72f

    View {
        attr {
            width(cellW)
            flexDirectionColumn()
            alignItemsCenter()
            marginLeft(MysticChinaTheme.Spacing.xxs)
            marginRight(MysticChinaTheme.Spacing.xxs)
        }
        event {
            click {
                ctx.selectedId = filter.id
                ctx.onSelected.invoke(filter)
            }
        }

        // 滤镜缩略图占位（正式版替换为带滤镜效果的预览帧）
        View {
            attr {
                size(64f, 64f)
                borderRadius(MysticChinaTheme.Radius.md)
                backgroundColor(
                    if (isSelected) MysticChinaColors.primary else MysticChinaColors.surface
                )
                allCenter()
            }

            // 滤镜名称首字母作为图标占位
            Text {
                attr {
                    text(filter.name.take(1))
                    fontSize(22f)
                    color(
                        if (isSelected) MysticChinaColors.textPrimary else MysticChinaColors.textSecondary
                    )
                    fontWeightBold()
                }
            }

            // 选中边框
            if (isSelected) {
                View {
                    attr {
                        size(64f, 64f)
                        borderRadius(MysticChinaTheme.Radius.md)
                        border(Border(2f, BorderStyle.SOLID, MysticChinaColors.primary))
                        absolutePositionAllZero()
                    }
                }
            }
        }

        // 滤镜名称
        Text {
            attr {
                text(filter.name)
                fontSize(10f)
                color(if (isSelected) MysticChinaColors.primary else MysticChinaColors.textSecondary)
                textAlignCenter()
                marginTop(4f)
            }
        }
    }
}
