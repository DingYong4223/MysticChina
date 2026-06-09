package com.fula.exploringchina.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 裁剪面板 — 时间范围选择 UI
 *
 * 布局：
 *  ┌─────────────────────────────────────────┐
 *  │  00:05             00:32     00:27      │  时间标签
 *  │  ┌─┬─────────────────┬─┐               │
 *  │  │◁│  ▓▓▓▓▓▓▓▓▓▓▓▓  │▷│               │  缩略图轨道 + 手柄
 *  │  └─┴─────────────────┴─┘               │
 *  │      [取消]              [确认]         │  操作按钮
 *  └─────────────────────────────────────────┘
 */
internal class TrimPanel(
    internal val totalDurationMs: Long,
    internal val onConfirm: (startMs: Long, endMs: Long) -> Unit,
    internal val onCancel: () -> Unit,
) {
    var startMs by observable(0L)
    var endMs by observable(0L)
    var isDraggingLeft by observable(false)
    var isDraggingRight by observable(false)

    /** 缩略图帧数量 */
    internal val frameCount = 10
    /** 总宽度由外部传入，默认 360 */
    internal var panelWidth: Float = 360f

    private val thumbTrackWidth: Float get() = panelWidth - YijianTheme.Spacing.lg * 2
    private val pxPerMs: Float get() = if (totalDurationMs > 0) thumbTrackWidth / totalDurationMs else 1f

    init {
        endMs = totalDurationMs
    }

    val durationText: String
        get() = if (endMs > startMs) formatTrimTime(endMs - startMs) else "00:00"

    val startText: String get() = formatTrimTime(startMs)
    val endText: String get() = formatTrimTime(endMs)

    // ── 手柄拖动事件 ──

    internal fun onLeftHandleMove(totalDx: Float) {
        val newMs = (totalDx / pxPerMs).toLong().coerceAtLeast(0L)
        if (newMs < endMs) startMs = newMs
    }

    internal fun onRightHandleMove(totalDx: Float) {
        val deltaMs = (totalDx / pxPerMs).toLong()
        val newMs = (endMs + deltaMs).coerceAtMost(totalDurationMs)
        if (newMs > startMs) endMs = newMs
    }

    private fun formatTrimTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return buildString {
            if (minutes < 10) append('0')
            append(minutes)
            append(':')
            if (seconds < 10) append('0')
            append(seconds)
        }
    }
}

/**
 * 渲染裁剪面板
 */
internal fun ViewContainer<*, *>.TrimPanelView(
    ctx: TrimPanel,
    pageWidth: Float,
) {
    ctx.panelWidth = pageWidth

    val trackW = pageWidth - YijianTheme.Spacing.lg * 2
    val handleSize = 24f
    val trackH = 56f
    val thumbH = 44f


    View {
        attr {
            backgroundColor(Color(0xFF1A1A1A))
            flexDirectionColumn()
            paddingLeft(YijianTheme.Spacing.lg)
            paddingRight(YijianTheme.Spacing.lg)
        }

        // ── 时间标签行 ──
        View {
            attr {
                flexDirectionRow()
                alignItemsCenter()
                justifyContentSpaceBetween()
                height(32f)
            }

            Text {
                attr {
                    text(ctx.startText)
                    fontSize(12f)
                    color(YijianColors.textSecondary)
                }
            }

            Text {
                attr {
                    text("时长 ${ctx.durationText}")
                    fontSize(12f)
                    color(YijianColors.primary)
                }
            }

            Text {
                attr {
                    text(ctx.endText)
                    fontSize(12f)
                    color(YijianColors.textSecondary)
                }
            }
        }

        // ── 缩略图轨道 + 手柄 ──
        View {
            attr {
                height(trackH)
                flexDirectionRow()
                alignItemsCenter()
                overflow(false)
            }

            // 灰色背景轨道
            View {
                attr {
                    size(trackW, thumbH)
                    borderRadius(6f)
                    backgroundColor(Color(0xFF2A2A2A))
                    overflow(true)
                }

                // 缩略图帧（示意）
                for (i in 0 until ctx.frameCount) {
                    val frameW = trackW / ctx.frameCount
                    View {
                        attr {
                            size(frameW, thumbH)
                            absolutePosition(left = frameW * i)
                            backgroundColor(if (i % 2 == 0) Color(0xFF333333) else Color(0xFF2D2D2D))
                        }
                    }
                }

                // 选中区域高亮
                View {
                    attr {
                        height(thumbH)
                        absolutePosition(
                            left = trackW * (if (ctx.totalDurationMs > 0) ctx.startMs.toFloat() / ctx.totalDurationMs else 0f),
                            top = 0f,
                        )
                        width(trackW * ((if (ctx.totalDurationMs > 0) ctx.endMs.toFloat() / ctx.totalDurationMs else 1f) - (if (ctx.totalDurationMs > 0) ctx.startMs.toFloat() / ctx.totalDurationMs else 0f)))
                        backgroundColor(Color(0x3323D3FD))
                    }
                }
            }

            // 左手柄（绝对覆盖）
            View {
                attr {
                    size(handleSize, trackH)
                    absolutePosition(left = trackW * (if (ctx.totalDurationMs > 0) ctx.startMs.toFloat() / ctx.totalDurationMs else 0f) - handleSize / 2f)
                    zIndex(10)
                }
                // 手柄拖动圆
                View {
                    attr {
                        size(handleSize, handleSize)
                        borderRadius(handleSize / 2f)
                        backgroundColor(YijianColors.primary)
                        allCenter()
                    }
                    // 白色竖线指示
                    View {
                        attr {
                            width(2f); height(14f)
                            backgroundColor(YijianColors.textPrimary)
                            borderRadius(1f)
                        }
                    }
                }
            }

            // 右手柄（绝对覆盖）
            View {
                attr {
                    size(handleSize, trackH)
                    absolutePosition(left = trackW * (if (ctx.totalDurationMs > 0) ctx.endMs.toFloat() / ctx.totalDurationMs else 1f) - handleSize / 2f)
                    zIndex(10)
                }
                View {
                    attr {
                        size(handleSize, handleSize)
                        borderRadius(handleSize / 2f)
                        backgroundColor(YijianColors.primary)
                        allCenter()
                    }
                    View {
                        attr {
                            width(2f); height(14f)
                            backgroundColor(YijianColors.textPrimary)
                            borderRadius(1f)
                        }
                    }
                }
            }
        }

        // ── 操作按钮 ──
        View {
            attr {
                flexDirectionRow()
                alignItemsCenter()
                justifyContentSpaceBetween()
                height(48f)
                marginTop(YijianTheme.Spacing.sm)
                marginBottom(YijianTheme.Spacing.sm)
            }

            // 取消
            View {
                attr {
                    height(36f)
                    paddingLeft(24f); paddingRight(24f)
                    borderRadius(18f)
                    backgroundColor(YijianColors.surface)
                    allCenter()
                }
                event { click { ctx.onCancel.invoke() } }
                Text {
                    attr {
                        text("取消")
                        fontSize(14f); color(YijianColors.textSecondary)
                    }
                }
            }

            // 确认裁剪
            View {
                attr {
                    height(36f)
                    paddingLeft(24f); paddingRight(24f)
                    borderRadius(18f)
                    backgroundLinearGradient(
                        Direction.TO_RIGHT,
                        ColorStop(YijianColors.gradientStart, 0f),
                        ColorStop(YijianColors.gradientEnd, 1f),
                    )
                    allCenter()
                }
                event { click { ctx.onConfirm.invoke(ctx.startMs, ctx.endMs) } }
                Text {
                    attr {
                        text("确认裁剪")
                        fontSize(14f); color(YijianColors.textPrimary)
                        fontWeightSemiBold()
                    }
                }
            }
        }
    }
}
