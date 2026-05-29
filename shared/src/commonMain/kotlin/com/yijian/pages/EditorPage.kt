package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.yijian.App
import com.yijian.base.BasePager
import com.yijian.player.*
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * 编辑页面 — 视频剪辑主界面（仿剪映设计）
 *
 * 布局（从上到下）：
 *  ┌─────────────────────────────────────┐
 *  │  ←  [撤销][重做]            [导出] │  TopBar
 *  ├─────────────────────────────────────┤
 *  │                                     │
 *  │        视频预览区 (16:9)             │  ~40%
 *  │                                     │
 *  ├─────────────────────────────────────┤
 *  │  00:01 ──────────┼──────── 00:15   │  时间轴（可滑动）
 *  │  [▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓]   │  片段缩略图轨道
 *  ├─────────────────────────────────────┤
 *  │ [分割][删除][变速][动画][蒙版]...   │  子工具栏（随类别切换）
 *  ├─────────────────────────────────────┤
 *  │ [剪辑][音频][文字][贴纸][特效][滤镜] │  主工具类别栏
 *  └─────────────────────────────────────┘
 */
@Page("EditorPage", supportInLocal = true)
internal class EditorPage : BasePager() {

    // —— 播放器 ——
    internal val controller = PlayerController()
    internal var videoPath by observable("")
    internal var videoTitle by observable("剪辑")

    // —— 界面状态 ——
    var selectedCategory by observable(EditCategory.CLIP)
    var undoEnabled by observable(false)
    var redoEnabled by observable(false)

    // —— 工具类别枚举 ——
    enum class EditCategory(val label: String, val icon: String) {
        CLIP("剪辑", "✂"),
        AUDIO("音频", "♪"),
        TEXT("文字", "T"),
        STICKER("贴纸", "☺"),
        PIP("画中画", "⊞"),
        EFFECTS("特效", "✦"),
        FILTER("滤镜", "◑"),
        RATIO("比例", "▭"),
        BACKGROUND("背景", "⬛"),
        ADJUST("调节", "⚙"),
    }

    // —— 子工具数据 ——
    data class SubTool(val label: String, val icon: String)

    val subTools: Map<EditCategory, List<SubTool>> = mapOf(
        EditCategory.CLIP to listOf(
            SubTool("分割", "⌷"), SubTool("删除", "✕"),
            SubTool("变速", "⏩"), SubTool("倒放", "⏮"),
            SubTool("动画", "◈"), SubTool("蒙版", "⊟"),
            SubTool("复制", "⧉"), SubTool("定格", "⏸"),
        ),
        EditCategory.AUDIO to listOf(
            SubTool("添加音乐", "♫"), SubTool("提取音乐", "⇥"),
            SubTool("录音", "⊙"), SubTool("音效", "♩"),
            SubTool("变声", "☻"), SubTool("降噪", "〰"),
        ),
        EditCategory.TEXT to listOf(
            SubTool("文本", "Aa"), SubTool("识别字幕", "⇌"),
            SubTool("识别歌词", "♫"), SubTool("文字模板", "⊟"),
        ),
        EditCategory.STICKER to listOf(
            SubTool("贴纸", "☺"), SubTool("表情包", "(**)"),
        ),
        EditCategory.PIP to listOf(
            SubTool("画中画", "⊞"), SubTool("蒙版", "⊟"),
            SubTool("智能抠图", "✂"),
        ),
        EditCategory.EFFECTS to listOf(
            SubTool("画面特效", "✦"), SubTool("人物特效", "◉"),
        ),
        EditCategory.FILTER to listOf(
            SubTool("滤镜", "◑"), SubTool("调节", "⚙"),
            SubTool("美颜美体", "✿"),
        ),
        EditCategory.RATIO to listOf(
            SubTool("原始", "□"), SubTool("9:16", "▯"),
            SubTool("16:9", "▭"), SubTool("1:1", "□"),
            SubTool("4:3", "▭"), SubTool("3:4", "▮"),
        ),
        EditCategory.BACKGROUND to listOf(
            SubTool("背景颜色", "⬤"), SubTool("背景样式", "⊟"),
            SubTool("背景模糊", "◌"),
        ),
        EditCategory.ADJUST to listOf(
            SubTool("亮度", "☀"), SubTool("对比度", "◑"),
            SubTool("饱和度", "◈"), SubTool("锐化", "⊛"),
            SubTool("高光", "△"), SubTool("阴影", "▽"),
            SubTool("色温", "⊙"), SubTool("暗角", "◎"),
        ),
    )

    // ——————————————————————————————————————————
    // Lifecycle
    // ——————————————————————————————————————————

    override fun created() {
        super.created()
        videoPath = pageData.params.optString(App.Param.VIDEO_PATH, "")
        videoTitle = pageData.params.optString(App.Param.VIDEO_TITLE, "剪辑")
        setupPlayer()
    }

    override fun viewDestroyed() {
        super.viewDestroyed()
        controller.release()
    }

    private fun setupPlayer() {
        val player = PlatformPlayerFactory.createPlayer()
        controller.bind(player)
        if (videoPath.isNotEmpty()) controller.loadVideo(videoPath)
    }

    // ——————————————————————————————————————————
    // Body
    // ——————————————————————————————————————————

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(YijianColors.background)
                flexDirectionColumn()
            }

            // ① TopBar
            EditorTopBar(ctx)

            // ② 视频预览
            EditorPreview(ctx)

            // ③ 时间轴
            EditorTimeline(ctx)

            // ④ 子工具栏
            EditorSubToolBar(ctx)

            // ⑤ 类别栏分割线
            View {
                attr {
                    height(1f)
                    backgroundColor(YijianColors.surfaceLight)
                }
            }

            // ⑥ 主工具类别栏
            EditorCategoryBar(ctx)
        }
    }
}

// ─────────────────────────────────────────────
// ① TopBar
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorTopBar(ctx: EditorPage) {
    View {
        attr {
            height(YijianTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
            backgroundColor(YijianColors.background)
            flexDirectionRow()
            alignItemsCenter()
            paddingTop(ctx.pagerData.statusBarHeight)
            paddingLeft(YijianTheme.Spacing.md)
            paddingRight(YijianTheme.Spacing.md)
        }

        // 返回
        View {
            attr { size(40f, 40f); allCenter() }
            event { click { ctx.closePage() } }
            Text {
                attr { text("←"); fontSize(22f); color(YijianColors.textPrimary) }
            }
        }

        // 撤销
        View {
            attr {
                size(36f, 36f); allCenter(); borderRadius(18f)
                backgroundColor(if (ctx.undoEnabled) YijianColors.surfaceLight else Color(0xFF1F1F1F))
                marginLeft(8f)
            }
            Text {
                attr {
                    text("↩"); fontSize(17f)
                    color(if (ctx.undoEnabled) YijianColors.textPrimary else YijianColors.textDisabled)
                }
            }
        }

        // 重做
        View {
            attr {
                size(36f, 36f); allCenter(); borderRadius(18f)
                backgroundColor(if (ctx.redoEnabled) YijianColors.surfaceLight else Color(0xFF1F1F1F))
                marginLeft(6f)
            }
            Text {
                attr {
                    text("↪"); fontSize(17f)
                    color(if (ctx.redoEnabled) YijianColors.textPrimary else YijianColors.textDisabled)
                }
            }
        }

        // 弹性空白
        View { attr { flex(1f) } }

        // 导出按钮
        View {
            attr {
                height(32f)
                paddingLeft(18f); paddingRight(18f)
                borderRadius(16f)
                backgroundLinearGradient(
                    Direction.TO_RIGHT,
                    ColorStop(YijianColors.gradientStart, 0f),
                    ColorStop(YijianColors.gradientEnd, 1f)
                )
                allCenter()
            }
            event { click { /* TODO: 导出逻辑 */ } }
            Text {
                attr {
                    text("导出"); fontSize(14f)
                    color(YijianColors.textPrimary)
                    fontWeightBold()
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// ② 视频预览区
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorPreview(ctx: EditorPage) {
    val w = ctx.pagerData.pageViewWidth
    val h = w * 9f / 16f

    View {
        attr {
            size(w, h)
            backgroundColor(Color(0xFF000000))
            allCenter()
        }
        event { click { ctx.controller.togglePlayPause() } }

        // 视频画面（正式版接入 VideoPlayerView）
        View { attr { absolutePositionAllZero(); backgroundColor(Color(0xFF0D0D0D)) } }

        // 中央播放/暂停按钮
        if (ctx.controller.playerState == PlayerState.PAUSED
            || ctx.controller.playerState == PlayerState.READY
            || ctx.controller.playerState == PlayerState.IDLE
        ) {
            View {
                attr { size(60f, 60f); borderRadius(30f); backgroundColor(Color(0xCC000000)); allCenter() }
                Text { attr { text("▶"); fontSize(26f); color(YijianColors.textPrimary) } }
            }
        }

        if (ctx.controller.playerState == PlayerState.COMPLETED) {
            View {
                attr { size(60f, 60f); borderRadius(30f); backgroundColor(Color(0xCC000000)); allCenter() }
                Text { attr { text("↻"); fontSize(26f); color(YijianColors.textPrimary) } }
            }
        }

        // 左下角时间显示
        View {
            attr {
                absolutePosition(bottom = 8f, left = 10f)
                paddingLeft(6f); paddingRight(6f); paddingTop(2f); paddingBottom(2f)
                backgroundColor(Color(0x99000000))
                borderRadius(4f)
            }
            Text {
                attr {
                    text("${ctx.controller.currentTimeText} / ${ctx.controller.durationText}")
                    fontSize(11f); color(YijianColors.textPrimary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// ③ 时间轴
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorTimeline(ctx: EditorPage) {
    val screenW = ctx.pagerData.pageViewWidth
    val halfW = screenW / 2f

    View {
        attr {
            height(86f)
            backgroundColor(YijianColors.background)
            flexDirectionColumn()
            overflow(false)
        }

        // 时间刻度
        View {
            attr {
                height(16f)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(halfW)
            }
            for (i in 0..10) {
                View {
                    attr { width(44f); alignItemsCenter() }
                    Text {
                        attr {
                            text(formatTimeMs(i * 2000L))
                            fontSize(9f); color(YijianColors.textTertiary)
                        }
                    }
                }
            }
        }

        // 视频片段轨道（Scroller 横向滚动）
        Scroller {
            attr {
                flex(1f)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(halfW)
                paddingRight(halfW)
                showScrollerIndicator(false)
            }

            // 片段块（示意 10 帧）
            View {
                attr {
                    height(44f)
                    flexDirectionRow()
                    borderRadius(6f)
                    overflow(true)
                }

                for (i in 0..9) {
                    View {
                        attr {
                            size(44f, 44f)
                            backgroundColor(
                                if (i % 2 == 0) Color(0xFF2D2D2D) else Color(0xFF252525)
                            )
                            allCenter()
                        }
                        // 帧图标（正式版替换为缩略图）
                        View {
                            attr {
                                size(36f, 36f)
                                backgroundColor(Color(0xFF3A3A3A))
                                borderRadius(3f)
                            }
                        }
                    }
                }

                // 末尾「+」添加片段
                View {
                    attr {
                        size(36f, 36f)
                        borderRadius(18f)
                        backgroundColor(YijianColors.surface)
                        allCenter()
                        marginLeft(8f)
                    }
                    Text {
                        attr { text("+"); fontSize(20f); color(YijianColors.textSecondary) }
                    }
                }
            }
        }

        // 播放头中心线（绝对覆盖）
        View {
            attr {
                absolutePosition(top = 0f, bottom = 0f, left = halfW - 1f)
                width(2f)
                backgroundColor(YijianColors.primary)
            }
        }
    }
}

// ─────────────────────────────────────────────
// ④ 子工具栏
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorSubToolBar(ctx: EditorPage) {
    val tools = ctx.subTools[ctx.selectedCategory] ?: emptyList()

    View {
        attr {
            height(80f)
            backgroundColor(Color(0xFF1E1E1E))
            flexDirectionColumn()
            justifyContentCenter()
        }

        Scroller {
            attr {
                flex(1f)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(8f)
                paddingRight(8f)
                showScrollerIndicator(false)
            }

            for (tool in tools) {
                SubToolItem(ctx, tool)
            }
        }
    }
}

private fun ViewContainer<*, *>.SubToolItem(ctx: EditorPage, tool: EditorPage.SubTool) {
    View {
        attr {
            width(62f); height(72f)
            flexDirectionColumn(); alignItemsCenter(); justifyContentCenter()
            marginLeft(2f); marginRight(2f)
        }
        event { click { /* TODO: 子工具逻辑 */ } }

        // 图标圆形背景
        View {
            attr {
                size(38f, 38f); borderRadius(19f)
                backgroundColor(Color(0xFF2E2E2E))
                allCenter(); marginBottom(5f)
            }
            Text {
                attr { text(tool.icon); fontSize(17f); color(YijianColors.textPrimary) }
            }
        }

        // 标签
        Text {
            attr {
                text(tool.label); fontSize(10f); color(YijianColors.textSecondary)
                textAlignCenter()
            }
        }
    }
}

// ─────────────────────────────────────────────
// ⑤ 主工具类别栏
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorCategoryBar(ctx: EditorPage) {
    View {
        attr {
            height(62f + ctx.pagerData.safeAreaInsets.bottom)
            backgroundColor(YijianColors.background)
            flexDirectionColumn()
        }

        Scroller {
            attr {
                flex(1f)
                flexDirectionRow()
                alignItemsCenter()
                paddingLeft(4f); paddingRight(4f)
                showScrollerIndicator(false)
            }

            for (category in EditorPage.EditCategory.values()) {
                CategoryItem(ctx, category)
            }
        }
    }
}

private fun ViewContainer<*, *>.CategoryItem(
    ctx: EditorPage,
    category: EditorPage.EditCategory
) {
    val selected = ctx.selectedCategory == category

    View {
        attr {
            width(64f); height(58f)
            flexDirectionColumn(); alignItemsCenter(); justifyContentCenter()
        }
        event { click { ctx.selectedCategory = category } }

        // 图标
        View {
            attr {
                size(42f, 42f); borderRadius(21f)
                backgroundColor(
                    if (selected) Color(0xFF2A2A2A) else Color(0x00000000)
                )
                allCenter(); marginBottom(3f)
            }
            Text {
                attr {
                    text(category.icon); fontSize(20f)
                    color(if (selected) YijianColors.primary else YijianColors.textSecondary)
                }
            }
        }

        // 标签
        Text {
            attr {
                text(category.label); fontSize(10f)
                color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                if (selected) fontWeightSemiBold() else fontWeightNormal()
            }
        }

        // 选中指示点
        if (selected) {
            View {
                attr {
                    size(4f, 4f); borderRadius(2f)
                    backgroundColor(YijianColors.primary)
                    marginTop(1f)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────
// 工具函数
// ─────────────────────────────────────────────
private fun formatTimeMs(ms: Long): String {
    val sec = ms / 1000
    val min = sec / 60
    val s = sec % 60
    return if (min > 0) "${min}:${s.toString().padStart(2, '0')}" else "${s}s"
}
