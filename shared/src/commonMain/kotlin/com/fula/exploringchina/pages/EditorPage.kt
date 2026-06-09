package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.App
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.components.*
import com.fula.exploringchina.model.ColorAdjustment
import com.fula.exploringchina.model.FilterEffect
import com.fula.exploringchina.model.TextOverlay
import com.fula.exploringchina.model.VideoClip
import com.fula.exploringchina.player.*
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 编辑页面 — 视频剪辑主界面（仿剪映设计）
 */
@Page("EditorPage", supportInLocal = true)
internal class EditorPage : BasePager() {

    internal val controller = PlayerController()
    internal var videoPath by observable("")
    internal var videoTitle by observable("剪辑")

    var selectedCategory by observable(EditCategory.CLIP)
    var undoEnabled by observable(false)
    var redoEnabled by observable(false)

    enum class EditCategory(val label: String, val icon: String) {
        CLIP("剪辑", "✂"), AUDIO("音频", "♪"), TEXT("文字", "T"),
        STICKER("贴纸", "☺"), PIP("画中画", "⊞"), EFFECTS("特效", "✦"),
        FILTER("滤镜", "◑"), RATIO("比例", "▭"), BACKGROUND("背景", "⬛"),
        ADJUST("调节", "⚙"),
    }

    data class SubTool(val label: String, val icon: String)

    val subTools: Map<EditCategory, List<SubTool>> = mapOf(
        EditCategory.CLIP to listOf(SubTool("分割", "⌷"), SubTool("删除", "✕"), SubTool("变速", "⏩"), SubTool("倒放", "⏮"), SubTool("动画", "◈"), SubTool("蒙版", "⊟"), SubTool("复制", "⧉"), SubTool("定格", "⏸")),
        EditCategory.AUDIO to listOf(SubTool("添加音乐", "♫"), SubTool("提取音乐", "⇥"), SubTool("录音", "⊙"), SubTool("音效", "♩"), SubTool("变声", "☻"), SubTool("降噪", "〰")),
        EditCategory.TEXT to listOf(SubTool("文本", "Aa"), SubTool("识别字幕", "⇌"), SubTool("识别歌词", "♫"), SubTool("文字模板", "⊟")),
        EditCategory.STICKER to listOf(SubTool("贴纸", "☺"), SubTool("表情包", "(**)")),
        EditCategory.PIP to listOf(SubTool("画中画", "⊞"), SubTool("蒙版", "⊟"), SubTool("智能抠图", "✂")),
        EditCategory.EFFECTS to listOf(SubTool("画面特效", "✦"), SubTool("人物特效", "◉")),
        EditCategory.FILTER to listOf(SubTool("滤镜", "◑"), SubTool("调节", "⚙"), SubTool("美颜美体", "✿")),
        EditCategory.RATIO to listOf(SubTool("原始", "□"), SubTool("9:16", "▯"), SubTool("16:9", "▭"), SubTool("1:1", "□"), SubTool("4:3", "▭"), SubTool("3:4", "▮")),
        EditCategory.BACKGROUND to listOf(SubTool("背景颜色", "⬤"), SubTool("背景样式", "⊟"), SubTool("背景模糊", "◌")),
        EditCategory.ADJUST to listOf(SubTool("亮度", "☀"), SubTool("对比度", "◑"), SubTool("饱和度", "◈"), SubTool("锐化", "⊛"), SubTool("高光", "△"), SubTool("阴影", "▽"), SubTool("色温", "⊙"), SubTool("暗角", "◎")),
    )

    var activePanel by observable(EditorPanel.NONE)
    var trimStartMs by observable(0L)
    var trimEndMs by observable(0L)
    var currentFilter by observable<FilterEffect?>(null)
    var currentAdjustment by observable(ColorAdjustment())
    var textOverlays by observableList<TextOverlay>()

    enum class EditorPanel { NONE, TRIM, FILTER, ADJUST, TEXT }

    val currentClip: VideoClip
        get() = VideoClip(
            id = "clip_0", sourcePath = videoPath,
            startMs = trimStartMs, endMs = trimEndMs,
            filter = currentFilter, colorAdjustment = currentAdjustment,
            textOverlays = textOverlays.toList(),
        )

    fun showPanel(panel: EditorPanel) { activePanel = panel }
    fun dismissPanel() { activePanel = EditorPanel.NONE }

    fun onTrimConfirm(startMs: Long, endMs: Long) { trimStartMs = startMs; trimEndMs = endMs; controller.seekTo(startMs); dismissPanel() }
    fun onFilterSelected(filter: FilterEffect) { currentFilter = filter }
    fun onAdjustChanged(adjustment: ColorAdjustment) { currentAdjustment = adjustment }
    fun onTextAdded(overlay: TextOverlay) { textOverlays.add(overlay); dismissPanel() }
    fun onResetFilter() { currentFilter = null; currentAdjustment = ColorAdjustment(); textOverlays.clear() }

    override fun created() {
        super.created()
        videoPath = pageData.params.optString(App.Param.VIDEO_PATH, "")
        videoTitle = pageData.params.optString(App.Param.VIDEO_TITLE, "剪辑")
        setupPlayer()
    }

    override fun viewDestroyed() { super.viewDestroyed(); controller.release() }

    private fun setupPlayer() {
        val player = PlatformPlayerFactory.createPlayer()
        controller.bind(player)
        if (videoPath.isNotEmpty()) controller.loadVideo(videoPath)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }
            EditorTopBar(ctx)
            EditorPreview(ctx)
            EditorTimeline(ctx)
            vif({ ctx.activePanel != EditorPage.EditorPanel.NONE }) { EditorToolPanel(ctx) }
            velse { EditorSubToolBar(ctx) }
            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }
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
            backgroundColor(YijianColors.background); flexDirectionRow(); alignItemsCenter()
            paddingTop(ctx.pagerData.statusBarHeight)
            paddingLeft(YijianTheme.Spacing.md); paddingRight(YijianTheme.Spacing.md)
        }
        View { attr { size(40f, 40f); allCenter() }; event { click { ctx.closePage() } }
            Text { attr { text("←"); fontSize(22f); color(YijianColors.textPrimary) } } }
        View { attr { size(36f, 36f); allCenter(); borderRadius(18f); marginLeft(8f)
                backgroundColor(if (ctx.undoEnabled) YijianColors.surfaceLight else Color(0xFF1F1F1F)) }
            Text { attr { text("↩"); fontSize(17f); color(if (ctx.undoEnabled) YijianColors.textPrimary else YijianColors.textDisabled) } } }
        View { attr { size(36f, 36f); allCenter(); borderRadius(18f); marginLeft(6f)
                backgroundColor(if (ctx.redoEnabled) YijianColors.surfaceLight else Color(0xFF1F1F1F)) }
            Text { attr { text("↪"); fontSize(17f); color(if (ctx.redoEnabled) YijianColors.textPrimary else YijianColors.textDisabled) } } }
        View { attr { flex(1f) } }
        View { attr { height(32f); paddingLeft(18f); paddingRight(18f); borderRadius(16f); allCenter()
                backgroundLinearGradient(Direction.TO_RIGHT, ColorStop(YijianColors.gradientStart, 0f), ColorStop(YijianColors.gradientEnd, 1f)) }
            event { click { /* TODO: 导出逻辑 */ } }
            Text { attr { text("导出"); fontSize(14f); color(YijianColors.textPrimary); fontWeightBold() } } }
    }
}

// ─────────────────────────────────────────────
// ② 视频预览区（含 VideoRender 原生视频渲染）
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorPreview(ctx: EditorPage) {
    val w = ctx.pagerData.pageViewWidth
    val h = w * 9f / 16f
    val ctrl = ctx.controller

    View {
        attr { size(w, h); backgroundColor(Color(0xFF000000)); allCenter() }

        // 视频原生渲染区域
        VideoRender {
            attr { absolutePositionAllZero() }
            event { surfaceReady { surfaceId -> ctrl.setSurfaceId(surfaceId) } }
        }

        // 触摸点击切换播放/暂停
        View {
            attr { absolutePositionAllZero() }
            event { click { ctrl.togglePlayPause() } }
        }

        vif({ ctrl.playerState != PlayerState.PLAYING }) {
            View { attr { absolutePositionAllZero(); allCenter() }
                View { attr { size(60f, 60f); borderRadius(30f); backgroundColor(Color(0xCC000000)); allCenter() }
                    Text { attr { text(if (ctrl.playerState == PlayerState.COMPLETED) "↻" else "▶"); fontSize(26f); color(YijianColors.textPrimary) } } } }
        }

        // 加载中
        vif({ ctrl.playerState == PlayerState.LOADING }) {
            View { attr { absolutePositionAllZero(); allCenter() }
                Text { attr { text("加载中..."); fontSize(14f); color(YijianColors.textSecondary) } } }
        }

        // 左下角时间
        View { attr { absolutePosition(bottom = 8f, left = 10f); paddingLeft(6f); paddingRight(6f); paddingTop(2f); paddingBottom(2f); backgroundColor(Color(0x99000000)); borderRadius(4f) }
            Text { attr { text("${ctrl.currentTimeText} / ${ctrl.durationText}"); fontSize(11f); color(YijianColors.textPrimary) } } }
    }
}

// ─────────────────────────────────────────────
// ③ 时间轴（含可拖拽进度条 Seek）
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorTimeline(ctx: EditorPage) {
    val screenW = ctx.pagerData.pageViewWidth
    val halfW = screenW / 2f
    val ctrl = ctx.controller

    View {
        attr { height(86f); backgroundColor(YijianColors.background); flexDirectionColumn(); overflow(false) }

        // 时间刻度
        View { attr { height(16f); flexDirectionRow(); alignItemsCenter(); paddingLeft(halfW) }
            for (i in 0..10) {
                View { attr { width(44f); alignItemsCenter() }
                    Text { attr { text(formatTimeMs(i * 2000L)); fontSize(9f); color(YijianColors.textTertiary) } } }
            }
        }

        // 轨道区（缩略图 + 进度条）
        View { attr { flex(1f); paddingLeft(halfW); paddingRight(halfW); flexDirectionColumn(); justifyContentCenter() }

            // 缩略图帧
            View { attr { height(24f); flexDirectionRow(); borderRadius(6f); overflow(true) }
                for (i in 0..9) {
                    View { attr { size(44f, 24f); backgroundColor(if (i % 2 == 0) Color(0xFF2D2D2D) else Color(0xFF252525)) } }
                }
            }

            // 可拖拽进度条
            ProgressBar {
                attr { progress = ctrl.currentProgress; barHeight = 8f; showThumb = true; enableDrag = true }
                event { onProgressChanged = { newProg -> ctrl.seekToProgress(newProg) } }
            }
        }

        // 播放头中心线
        View { attr { absolutePosition(top = 0f, bottom = 0f, left = halfW - 1f); width(2f); backgroundColor(YijianColors.primary) } }

        // 已播放区域指示
        if (ctrl.durationMs > 0) {
            val playedW = screenW * ctrl.currentProgress
            View { attr { absolutePosition(top = 0f, bottom = 0f, left = 0f); width(playedW.coerceAtMost(screenW)); height(2f); backgroundColor(YijianColors.primary) } }
        }
    }
}

// ─────────────────────────────────────────────
// ④ 子工具栏
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorSubToolBar(ctx: EditorPage) {
    val tools = ctx.subTools[ctx.selectedCategory] ?: emptyList()
    View { attr { height(80f); backgroundColor(Color(0xFF1E1E1E)); flexDirectionColumn(); justifyContentCenter() }
        Scroller { attr { flex(1f); flexDirectionRow(); alignItemsCenter(); paddingLeft(8f); paddingRight(8f); showScrollerIndicator(false) }
            for (tool in tools) { SubToolItem(ctx, tool) } }
    }
}

private fun ViewContainer<*, *>.SubToolItem(ctx: EditorPage, tool: EditorPage.SubTool) {
    val panelToOpen = when (ctx.selectedCategory to tool.label) {
        EditorPage.EditCategory.FILTER to "滤镜" -> EditorPage.EditorPanel.FILTER
        EditorPage.EditCategory.FILTER to "调节" -> EditorPage.EditorPanel.ADJUST
        EditorPage.EditCategory.CLIP to "分割" -> EditorPage.EditorPanel.TRIM
        EditorPage.EditCategory.TEXT to "文本" -> EditorPage.EditorPanel.TEXT
        else -> null
    }
    View { attr { width(62f); height(72f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter(); marginLeft(2f); marginRight(2f) }
        event { click { if (panelToOpen != null) ctx.showPanel(panelToOpen) } }
        View { attr { size(38f, 38f); borderRadius(19f); backgroundColor(Color(0xFF2E2E2E)); allCenter(); marginBottom(5f) }
            Text { attr { text(tool.icon); fontSize(17f); color(YijianColors.textPrimary) } } }
        Text { attr { text(tool.label); fontSize(10f); color(YijianColors.textSecondary); textAlignCenter() } } }
}

// ─────────────────────────────────────────────
// ⑤ 主工具类别栏
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorCategoryBar(ctx: EditorPage) {
    View { attr { height(62f + ctx.pagerData.safeAreaInsets.bottom); backgroundColor(YijianColors.background); flexDirectionColumn() }
        Scroller { attr { flex(1f); flexDirectionRow(); alignItemsCenter(); paddingLeft(4f); paddingRight(4f); showScrollerIndicator(false) }
            for (category in EditorPage.EditCategory.values()) { CategoryItem(ctx, category) } }
    }
}

private fun ViewContainer<*, *>.CategoryItem(ctx: EditorPage, category: EditorPage.EditCategory) {
    val selected = ctx.selectedCategory == category
    View { attr { width(64f); height(58f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter() }
        event { click { ctx.selectedCategory = category } }
        View { attr { size(42f, 42f); borderRadius(21f); backgroundColor(if (selected) Color(0xFF2A2A2A) else Color(0x00000000)); allCenter(); marginBottom(3f) }
            Text { attr { text(category.icon); fontSize(20f); color(if (selected) YijianColors.primary else YijianColors.textSecondary) } } }
        Text { attr { text(category.label); fontSize(10f); color(if (selected) YijianColors.primary else YijianColors.textTertiary); if (selected) fontWeightSemiBold() else fontWeightNormal() } }
        if (selected) View { attr { size(4f, 4f); borderRadius(2f); backgroundColor(YijianColors.primary); marginTop(1f) } } }
}

// ─────────────────────────────────────────────
// ⑥ 编辑工具面板
// ─────────────────────────────────────────────
private fun ViewContainer<*, *>.EditorToolPanel(ctx: EditorPage) {
    val w = ctx.pagerData.pageViewWidth
    when (ctx.activePanel) {
        EditorPage.EditorPanel.TRIM -> {
            val tp = TrimPanel(ctx.controller.durationMs, { s, e -> ctx.onTrimConfirm(s, e) }, { ctx.dismissPanel() })
            tp.startMs = ctx.trimStartMs; tp.endMs = if (ctx.trimEndMs > 0) ctx.trimEndMs else ctx.controller.durationMs
            TrimPanelView(tp, w)
        }
        EditorPage.EditorPanel.FILTER -> {
            val pk = FilterPicker({ f -> ctx.onFilterSelected(f); ctx.dismissPanel() }, { ctx.dismissPanel() })
            if (ctx.currentFilter != null) pk.selectedId = ctx.currentFilter!!.id
            FilterPickerView(pk)
        }
        EditorPage.EditorPanel.ADJUST -> { ColorAdjustView(ColorAdjustPanel(ctx.currentAdjustment, { ctx.onAdjustChanged(it) }, { ctx.dismissPanel() })) }
        EditorPage.EditorPanel.TEXT -> { TextOverlayEditorView(TextOverlayEditor(null, { ctx.onTextAdded(it) }, { ctx.dismissPanel() })) }
        EditorPage.EditorPanel.NONE -> {}
    }
}

// ─────────────────────────────────────────────
// 工具函数
// ─────────────────────────────────────────────
private fun formatTimeMs(ms: Long): String {
    val sec = ms / 1000; val min = sec / 60; val s = sec % 60
    return if (min > 0) "${min}:${s.toString().padStart(2, '0')}" else "${s}s"
}
