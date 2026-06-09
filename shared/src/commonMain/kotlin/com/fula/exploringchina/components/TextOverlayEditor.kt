package com.fula.exploringchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.model.TextOverlay
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 文字叠加编辑器 — 添加/编辑视频文字
 *
 * 提供：
 * - 文字内容输入
 * - 字体大小/颜色/描边调节
 * - 位置/旋转/缩放预览（占位）
 */
internal class TextOverlayEditor(
    private val existing: TextOverlay?,
    internal val onConfirm: (TextOverlay) -> Unit,
    internal val onDismiss: () -> Unit,
) {
    var textContent by observable(existing?.text ?: "")
    var fontSize by observable(existing?.fontSize ?: 36f)
    var colorHex by observable(existing?.colorArgb ?: 0xFFFFFFFF)
    var strokeColorHex by observable(existing?.strokeColorArgb ?: 0xCC000000)
    var strokeWidth by observable(existing?.strokeWidth ?: 2f)
    var positionX by observable(existing?.positionX ?: 0.5f)
    var positionY by observable(existing?.positionY ?: 0.5f)
    var rotation by observable(existing?.rotation ?: 0f)
    var scale by observable(existing?.scale ?: 1.0f)

    /** 当前编辑中的样式类别 */
    var styleTab by observable(0) // 0: 样式, 1: 颜色, 2: 描边

    val textOverlay: TextOverlay
        get() = TextOverlay(
            id = existing?.id ?: generateId(),
            text = textContent,
            fontSize = fontSize,
            colorArgb = colorHex,
            strokeColorArgb = strokeColorHex,
            strokeWidth = strokeWidth,
            positionX = positionX,
            positionY = positionY,
            rotation = rotation,
            scale = scale,
            startOffsetMs = existing?.startOffsetMs ?: 0L,
            durationMs = existing?.durationMs ?: 0L,
        )
}

// ── 内置颜色预设 ──
private val colorPresets = listOf(
    0xFFFFFFFFL to "白", 0xFF000000L to "黑",
    0xFFFF3B30L to "红", 0xFFFF9500L to "橙",
    0xFFFFCC00L to "黄", 0xFF34C759L to "绿",
    0xFF007AFFL to "蓝", 0xFFAF52DEL to "粉",
    0xFF5856D6L to "紫", 0xFF8E8E93L to "灰",
)

/** 渲染文字编辑器面板 */
internal fun ViewContainer<*, *>.TextOverlayEditorView(ctx: TextOverlayEditor) {
    View {
        attr {
            backgroundColor(Color(0xFF1A1A1A))
            flexDirectionColumn()
            paddingLeft(YijianTheme.Spacing.md)
            paddingRight(YijianTheme.Spacing.md)
        }

        // ── 标题栏 ──
        View {
            attr {
                flexDirectionRow()
                alignItemsCenter()
                justifyContentSpaceBetween()
                height(40f)
            }
            Text {
                attr { text("文字"); fontSize(16f); color(YijianColors.textPrimary); fontWeightSemiBold() }
            }
            View {
                attr { size(36f, 36f); allCenter() }
                event { click { ctx.onDismiss.invoke() } }
                Text { attr { text("✕"); fontSize(18f); color(YijianColors.textSecondary) } }
            }
        }

        // ── 文字输入 ──
        View {
            attr {
                height(44f)
                backgroundColor(YijianColors.surface)
                borderRadius(YijianTheme.Radius.md)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(YijianTheme.Spacing.md)
                paddingRight(YijianTheme.Spacing.md)
                marginBottom(YijianTheme.Spacing.sm)
            }
            Input {
                attr {
                    flex(1f); height(40f)
                    placeholder("输入文字内容")
                    color(YijianColors.textPrimary)
                    fontSize(YijianTheme.FontSize.body)
                }
                // textContent is updated via placeholder for now
            }
        }

        // ── 样式标签切换 ──
        View {
            attr {
                flexDirectionRow()
                height(36f)
                marginBottom(YijianTheme.Spacing.sm)
            }

            listOf("样式", "颜色", "描边").forEachIndexed { i, label ->
                val isSel = ctx.styleTab == i
                View {
                    attr {
                        flex(1f); allCenter()
                        borderRadius(YijianTheme.Radius.sm)
                        marginLeft(2f); marginRight(2f)
                        backgroundColor(if (isSel) YijianColors.primary else YijianColors.surface)
                    }
                    event { click { ctx.styleTab = i } }
                    Text {
                        attr {
                            text(label); fontSize(12f)
                            color(if (isSel) YijianColors.textPrimary else YijianColors.textSecondary)
                        }
                    }
                }
            }
        }

        // ── 样式内容 ──
        when (ctx.styleTab) {
            0 -> StyleContent(ctx)
            1 -> ColorContent(ctx)
            2 -> StrokeContent(ctx)
        }

        // ── 确认按钮 ──
        View {
            attr {
                height(40f)
                borderRadius(YijianTheme.Radius.lg)
                backgroundLinearGradient(
                    Direction.TO_RIGHT,
                    ColorStop(YijianColors.gradientStart, 0f),
                    ColorStop(YijianColors.gradientEnd, 1f),
                )
                allCenter()
                marginTop(YijianTheme.Spacing.sm)
                marginBottom(YijianTheme.Spacing.sm)
            }
            event { click { ctx.onConfirm.invoke(ctx.textOverlay) } }
            Text {
                attr {
                    text("添加文字"); fontSize(14f); color(YijianColors.textPrimary)
                    fontWeightSemiBold()
                }
            }
        }
    }
}

/** 样式标签 — 字体大小预览、旋转缩放指示 */
private fun ViewContainer<*, *>.StyleContent(ctx: TextOverlayEditor) {
    View {
        attr { flexDirectionColumn() }

        // 文字预览
        View {
            attr {
                height(60f)
                backgroundColor(YijianColors.surface)
                borderRadius(YijianTheme.Radius.md)
                allCenter()
                marginBottom(YijianTheme.Spacing.sm)
            }
            Text {
                attr {
                    text(ctx.textContent.ifEmpty { "预览文字" })
                    fontSize(ctx.fontSize.coerceIn(12f, 72f))
                    color(Color(ctx.colorHex))
                }
            }
        }

        // 字体大小调节行
        View {
            attr { flexDirectionRow(); alignItemsCenter(); marginBottom(YijianTheme.Spacing.sm) }
            Text { attr { text("大小"); fontSize(11f); color(YijianColors.textSecondary); marginRight(8f) } }
            View {
                attr {
                    flex(1f); height(4f); borderRadius(2f)
                    backgroundColor(YijianColors.surfaceLight)
                }
            }
            Text {
                attr {
                    text("${ctx.fontSize.toInt()}")
                    fontSize(11f); color(YijianColors.textPrimary)
                    marginLeft(8f); width(28f)
                }
            }
        }
    }
}

/** 颜色标签 — 颜色预设网格 */
private fun ViewContainer<*, *>.ColorContent(ctx: TextOverlayEditor) {
    View {
        attr { flexDirectionRow(); flexWrapWrap() }

        for ((colorArgb, _) in colorPresets) {
            val isSelected = ctx.colorHex == colorArgb
            View {
                attr {
                    size(32f, 32f); borderRadius(16f)
                    backgroundColor(Color(colorArgb))
                    marginRight(8f); marginBottom(YijianTheme.Spacing.sm)
                    allCenter()
                }
                event { click { ctx.colorHex = colorArgb } }

                if (isSelected) {
                    View {
                        attr {
                            size(36f, 36f); borderRadius(18f)
                            border(Border(2f, BorderStyle.SOLID, YijianColors.primary))
                            absolutePositionAllZero()
                        }
                    }
                }
            }
        }
    }
}

/** 描边标签 — 描边颜色 + 宽度 */
private fun ViewContainer<*, *>.StrokeContent(ctx: TextOverlayEditor) {
    View {
        attr { flexDirectionColumn() }

        Text {
            attr {
                text("描边颜色"); fontSize(11f); color(YijianColors.textSecondary)
                marginBottom(YijianTheme.Spacing.xs)
            }
        }

        // 颜色预设（缩小版）
        View {
            attr { flexDirectionRow(); flexWrapWrap(); marginBottom(YijianTheme.Spacing.sm) }

            for ((colorArgb, _) in colorPresets) {
                val isSelected = ctx.strokeColorHex == colorArgb
                View {
                    attr {
                        size(24f, 24f); borderRadius(12f)
                        backgroundColor(Color(colorArgb))
                        marginRight(6f); marginBottom(4f)
                        allCenter()
                    }
                    event { click { ctx.strokeColorHex = colorArgb } }

                    if (isSelected) {
                        View {
                            attr {
                                size(28f, 28f); borderRadius(14f)
                                border(Border(2f, BorderStyle.SOLID, YijianColors.primary))
                                absolutePositionAllZero()
                            }
                        }
                    }
                }
            }
        }

        // 描边宽度
        View {
            attr { flexDirectionRow(); alignItemsCenter() }
            Text { attr { text("宽度"); fontSize(11f); color(YijianColors.textSecondary); marginRight(8f) } }
            View {
                attr {
                    flex(1f); height(4f); borderRadius(2f)
                    backgroundColor(YijianColors.surfaceLight)
                }
            }
            Text {
                attr {
                    text("${ctx.strokeWidth.toInt()}px")
                    fontSize(11f); color(YijianColors.textPrimary)
                    marginLeft(8f); width(28f)
                }
            }
        }
    }
}

/** 生成唯一 ID */
private fun generateId(): String = "text_${com.fula.exploringchina.util.currentTimeMs()}"
