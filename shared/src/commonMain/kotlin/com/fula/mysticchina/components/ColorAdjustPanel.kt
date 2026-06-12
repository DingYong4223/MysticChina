package com.fula.mysticchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.model.ColorAdjustment
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * 色彩调节面板 — 提供亮度/对比度/饱和度等滑块控制。
 *
 * 每项调节使用一个响应式条 + 数值显示，
 * 通过 View 的 touchMove/touchUp 事件模拟拖动。
 */
internal class ColorAdjustPanel(
    private val initial: ColorAdjustment = ColorAdjustment(),
    private val onChanged: (ColorAdjustment) -> Unit,
    internal val onDismiss: () -> Unit,
) {
    // 当前值
    var brightness by observable(initial.brightness)
    var contrast by observable(initial.contrast)
    var saturation by observable(initial.saturation)
    var sharpen by observable(initial.sharpen)
    var temperature by observable(initial.temperature)
    var exposure by observable(initial.exposure)
    var highlights by observable(initial.highlights)
    var shadows by observable(initial.shadows)
    var vignette by observable(initial.vignette)

    /** 当前选中的调节项索引 */
    var selectedIndex by observable(0)

    data class AdjustItem(
        val label: String,
        val value: Float,
        val range: ClosedFloatingPointRange<Float>,
        val displayFormatter: (Float) -> String,
    )

    val items: List<AdjustItem>
        get() = listOf(
            AdjustItem("亮度",  brightness, -1.0f..1.0f,  { "${(it * 100).toInt()}%" }),
            AdjustItem("对比度", contrast,   0.0f..2.0f,   { "${(it * 100).toInt()}%" }),
            AdjustItem("饱和度", saturation, 0.0f..2.0f,   { "${(it * 100).toInt()}%" }),
            AdjustItem("锐化",   sharpen,    0.0f..1.0f,   { "${(it * 100).toInt()}%" }),
            AdjustItem("色温",   temperature, -1.0f..1.0f, { if (it >= 0) "+${(it * 100).toInt()}%" else "${(it * 100).toInt()}%" }),
            AdjustItem("曝光",   exposure,   -2.0f..2.0f,  { if (it >= 0) "+${(it * 100).toInt()}" else "${(it * 100).toInt()}" }),
            AdjustItem("高光",   highlights, -1.0f..1.0f,  { if (it >= 0) "+${(it * 100).toInt()}%" else "${(it * 100).toInt()}%" }),
            AdjustItem("阴影",   shadows,    -1.0f..1.0f,  { if (it >= 0) "+${(it * 100).toInt()}%" else "${(it * 100).toInt()}%" }),
            AdjustItem("暗角",   vignette,   0.0f..1.0f,   { "${(it * 100).toInt()}%" }),
        )

    private val valueFieldName: String get() = when (selectedIndex) {
        0 -> "brightness"; 1 -> "contrast"; 2 -> "saturation"; 3 -> "sharpen"
        4 -> "temperature"; 5 -> "exposure"; 6 -> "highlights"
        7 -> "shadows"; 8 -> "vignette"
        else -> "brightness"
    }

    private fun getValue(index: Int): Float = when (index) {
        0 -> brightness; 1 -> contrast; 2 -> saturation; 3 -> sharpen
        4 -> temperature; 5 -> exposure; 6 -> highlights
        7 -> shadows; 8 -> vignette
        else -> 0f
    }

    private fun setValue(index: Int, v: Float) {
        when (index) {
            0 -> brightness = v; 1 -> contrast = v; 2 -> saturation = v
            3 -> sharpen = v; 4 -> temperature = v; 5 -> exposure = v
            6 -> highlights = v; 7 -> shadows = v; 8 -> vignette = v
        }
        emitChange()
    }

    fun applyDelta(delta: Float) {
        val item = items.getOrNull(selectedIndex) ?: return
        val rangeSize = item.range.endInclusive - item.range.start
        val deltaValue = delta * rangeSize / 200f // 200px 滑动范围
        val cur = getValue(selectedIndex)
        val newVal = (cur + deltaValue).coerceIn(item.range.start, item.range.endInclusive)
        setValue(selectedIndex, newVal)
    }

    private fun emitChange() {
        onChanged(
            ColorAdjustment(
                brightness = brightness, contrast = contrast,
                saturation = saturation, sharpen = sharpen,
                temperature = temperature, exposure = exposure,
                highlights = highlights, shadows = shadows,
                vignette = vignette,
            )
        )
    }

    /** 重置当前项到默认值 */
    fun resetCurrent() {
        val defaults = ColorAdjustment()
        val defaultVal = when (selectedIndex) {
            0 -> defaults.brightness; 1 -> defaults.contrast; 2 -> defaults.saturation
            3 -> defaults.sharpen; 4 -> defaults.temperature; 5 -> defaults.exposure
            6 -> defaults.highlights; 7 -> defaults.shadows; 8 -> defaults.vignette
            else -> 0f
        }
        setValue(selectedIndex, defaultVal)
    }
}

internal fun ViewContainer<*, *>.ColorAdjustView(ctx: ColorAdjustPanel) {
    View {
        attr {
            backgroundColor(Color(0xFF1A1A1A))
            flexDirectionColumn()
        }

        // ── 标题栏 ──
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
                attr { text("调节"); fontSize(16f); color(MysticChinaColors.textPrimary); fontWeightSemiBold() }
            }
            View {
                attr { size(36f, 36f); allCenter() }
                event { click { ctx.onDismiss.invoke() } }
                Text { attr { text("✕"); fontSize(18f); color(MysticChinaColors.textSecondary) } }
            }
        }

        // ── 中间滑块区域 ──
        if (ctx.selectedIndex < ctx.items.size) {
            val item = ctx.items[ctx.selectedIndex]
            SliderRow(ctx, item)
        }

        // ── 底部调节项网格 ──
        Scroller {
            attr {
                height(68f)
                flexDirectionRow()
                paddingLeft(MysticChinaTheme.Spacing.sm)
                paddingRight(MysticChinaTheme.Spacing.sm)
                showScrollerIndicator(false)
            }

            for ((i, item) in ctx.items.withIndex()) {
                val idx = i
                val isSelected = ctx.selectedIndex == idx
                val curVal = item.value

                View {
                    attr {
                        width(66f)
                        marginLeft(3f); marginRight(3f)
                    }
                    event { click { ctx.selectedIndex = idx } }

                    // 数值
                    Text {
                        attr {
                            text(item.displayFormatter(curVal))
                            fontSize(11f)
                            color(if (isSelected) MysticChinaColors.primary else MysticChinaColors.textSecondary)
                            textAlignCenter()
                        }
                    }

                    // 进度条指示
                    View {
                        attr {
                            height(48f); width(6f); borderRadius(3f)
                            backgroundColor(MysticChinaColors.surfaceLight)
                            marginTop(2f); marginLeft(26f); marginRight(26f)
                            overflow(false); allCenter()
                        }

                        val fillRatio = (curVal - item.range.start) / (item.range.endInclusive - item.range.start)
                        View {
                            attr {
                                width(6f)
                                height(48f * fillRatio.coerceIn(0f, 1f))
                                borderRadius(3f)
                                backgroundColor(if (isSelected) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                                absolutePosition(bottom = 0f)
                            }
                        }
                    }

                    // 标签
                    Text {
                        attr {
                            text(item.label)
                            fontSize(10f)
                            color(if (isSelected) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                            textAlignCenter()
                            marginTop(2f)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 滑块行 — 显示当前选中的调节项的拖动条
 */
private fun ViewContainer<*, *>.SliderRow(ctx: ColorAdjustPanel, item: ColorAdjustPanel.AdjustItem) {
    val range = item.range
    val curVal = item.value
    val fillRatio = ((curVal - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val sliderW = 280f
    val thumbSize = 20f

    View {
        attr {
            height(50f)
            flexDirectionRow()
            alignItemsCenter()
            justifyContentCenter()
            paddingLeft(MysticChinaTheme.Spacing.md)
            paddingRight(MysticChinaTheme.Spacing.md)
        }

        // 最小值标签
        Text {
            attr {
                text(item.displayFormatter(range.start))
                fontSize(10f); color(MysticChinaColors.textTertiary)
                marginRight(MysticChinaTheme.Spacing.sm)
            }
        }

        // 滑块轨道
        View {
            attr {
                width(sliderW); height(4f); borderRadius(2f)
                backgroundColor(MysticChinaColors.surfaceLight)
                allCenter()
            }

            // 填充
            View {
                attr {
                    height(4f); borderRadius(2f)
                    absolutePosition(left = 0f)
                    width(sliderW * fillRatio)
                    backgroundColor(MysticChinaColors.primary)
                }
            }

            // 拖动按钮
            View {
                attr {
                    size(thumbSize, thumbSize); borderRadius(thumbSize / 2f)
                    backgroundColor(MysticChinaColors.textPrimary)
                    zIndex(10)
                }
            }
        }

        // 最大值标签
        Text {
            attr {
                text(item.displayFormatter(range.endInclusive))
                fontSize(10f); color(MysticChinaColors.textTertiary)
                marginLeft(MysticChinaTheme.Spacing.sm)
            }
        }
    }
}
