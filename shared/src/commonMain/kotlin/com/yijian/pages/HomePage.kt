package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.components.DraftActionBar
import com.yijian.components.DraftActionBarConfig
import com.yijian.manager.DraftManager
import com.yijian.model.UserProfile
import com.yijian.model.VideoInfo
import com.yijian.module.GalleryModule
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme
import com.yijian.util.currentTimeMs
import com.yijian.util.fileExists

private const val SP_NICKNAME = "yijian_nickname"
private const val SP_BIO = "yijian_bio"
private const val SP_AVATAR = "yijian_avatar"

private enum class HomeTab(val label: String, val icon: String) {
    CLIP("剪辑", "✂"),
    LEARN("学习", "🎓"),
    PROFILE("我的", "👤")
}

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    var selectedTab by observable(0)

    var userProfile by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio by observable(false)
    var editingText by observable("")
    var errorMessages by observableList<String>()

    fun showError(msg: String) { errorMessages.add(msg) }

    fun pickAndEditVideo() {
        val gallery = acquireModule<GalleryModule>(GalleryModule.MODULE_NAME)
        gallery.pickVideo { result ->
            val res = result ?: return@pickVideo
            val path = res.optString("path", "")
            if (res.optBoolean("cancelled", false) || path.isEmpty()) return@pickVideo
            val title = res.optString("name", "未知.mp4")
            val params = """{"videoPath":"$path","videoTitle":"$title"}"""
            jumpPage("EditorPage", params)
        }
    }

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    private val backCallback: BackPressCallback = object : BackPressCallback() {
        override fun handleOnBackPressed() {
            draftMgr.exitEditing()
        }
    }

    val draftMgr by lazy {
        DraftManager(sp).also {
            it.onEditingStateChanged = { editing ->
                KLog.d("HomePage", "onEditingStateChanged: editing=$editing")
                if (editing) getBackPressHandler().addCallback(backCallback)
                else getBackPressHandler().removeCallback(backCallback)
            }
        }
    }

    override fun created() {
        super.created()
        userProfile = UserProfile(
            nickname = sp.getString(SP_NICKNAME) ?: "创作者",
            bio = sp.getString(SP_BIO) ?: "记录生活的每一刻",
            avatarEmoji = sp.getString(SP_AVATAR) ?: "🎬"
        )
        draftMgr.load()
        if (draftMgr.draftList.isEmpty()) {
            draftMgr.add(VideoInfo("1", "午后阳光.mp4", "test1", duration = 15200L, createTime = currentTimeMs() - 7200000L))
            draftMgr.add(VideoInfo("2", "城市街景.mp4", "test2", duration = 45000L, createTime = currentTimeMs() - 86400000L))
            draftMgr.add(VideoInfo("3", "旅行记录.mp4", "test3", duration = 120000L, createTime = currentTimeMs() - 172800000L))
            draftMgr.add(VideoInfo("4", "美食制作.mp4", "test4", duration = 32000L, createTime = currentTimeMs() - 3600000L))
        }
    }

    private fun saveNickname() {
        val newVal = editingText.trim()
        if (newVal.isNotEmpty()) {
            userProfile = userProfile.copy(nickname = newVal)
            sp.setString(SP_NICKNAME, newVal)
        }
        showEditNickname = false; editingText = ""
    }

    private fun saveBio() {
        val newVal = editingText.trim()
        if (newVal.isNotEmpty()) {
            userProfile = userProfile.copy(bio = newVal)
            sp.setString(SP_BIO, newVal)
        }
        showEditBio = false; editingText = ""
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }

            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 0 }) { ClipTabContent(ctx, ctx.draftMgr) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }

            vif({ !ctx.draftMgr.isEditing }) { BottomTabBar(ctx) }

            vif({ ctx.showEditNickname }) {
                EditOverlay(ctx, "修改昵称", ctx.userProfile.nickname, { ctx.saveNickname() }, { ctx.showEditNickname = false; ctx.editingText = "" })
            }
            vif({ ctx.showEditBio }) {
                EditOverlay(ctx, "修改简介", ctx.userProfile.bio, { ctx.saveBio() }, { ctx.showEditBio = false; ctx.editingText = "" })
            }

            vfor({ ctx.errorMessages }) { msg ->
                View {
                    attr { height(44f); padding(top = 12f, bottom = 12f, left = 16f, right = 16f); backgroundColor(Color(0xE8FF3B30)); flexDirectionRow(); alignItemsCenter() }
                    event { click { ctx.errorMessages.clear() } }
                    View { attr { size(20f, 20f); borderRadius(10f); backgroundColor(Color(0xFFFFFFFF)); allCenter(); marginRight(10f) }
                        Text { attr { text("!"); fontSize(13f); color(Color(0xFFFF3B30)); fontWeightBold() } } }
                    Text { attr { text(msg); fontSize(13f); color(Color(0xFFFFFFFF)); flex(1f) } }
                    View { attr { size(24f, 24f); allCenter() }
                        Text { attr { text("✕"); fontSize(14f); color(Color(0xAAFFFFFF)) } } }
                }
            }

            vif({ ctx.draftMgr.isEditing }) {
                DraftActionBar(
                    config = DraftActionBarConfig(
                        isAllSelected = ctx.draftMgr.isAllSelected,
                        selectedCount = ctx.draftMgr.selectedCount,
                        onToggleSelectAll = { if (ctx.draftMgr.isAllSelected) ctx.draftMgr.deselectAll() else ctx.draftMgr.selectAll() },
                        onDelete = { ctx.draftMgr.remove(ctx.draftMgr.selectedIds.toList()) },
                    ),
                    safeAreaBottom = ctx.pagerData.safeAreaInsets.bottom,
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 底部 Tab Bar
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.BottomTabBar(ctx: HomePage) {
    View {
        attr { height(56f + ctx.pagerData.safeAreaInsets.bottom); backgroundColor(YijianColors.background); flexDirectionRow(); alignItemsCenter(); paddingBottom(ctx.pagerData.safeAreaInsets.bottom) }
        HomeTab.values().forEachIndexed { index, tab ->
            val selected = ctx.selectedTab == index
            View {
                attr { flex(1f); height(56f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter() }
                event { click { ctx.selectedTab = index } }
                View { attr { size(4f, 4f); borderRadius(2f); backgroundColor(if (selected) YijianColors.primary else Color(0x00000000)); marginBottom(2f) } }
                Text { attr { text(tab.icon); fontSize(22f); color(if (selected) YijianColors.primary else YijianColors.textTertiary); marginBottom(2f) } }
                Text { attr { text(tab.label); fontSize(10f); color(if (selected) YijianColors.primary else YijianColors.textTertiary); if (selected) fontWeightSemiBold() else fontWeightNormal() } }
            }
        }
    }
}

private fun ViewContainer<*, *>.EditOverlay(ctx: HomePage, title: String, placeholder: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    View {
        attr { absolutePositionAllZero(); backgroundColor(Color(0xCC000000)); allCenter() }
        event { click { onDismiss() } }
        View {
            attr { width(ctx.pagerData.pageViewWidth - 48f); backgroundColor(YijianColors.surface); borderRadius(16f); padding(all = YijianTheme.Spacing.lg); flexDirectionColumn() }
            event { click {} }
            Text { attr { text(title); fontSize(16f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(YijianTheme.Spacing.md) } }
            View { attr { flexDirectionRow(); justifyContentFlexEnd(); marginTop(YijianTheme.Spacing.lg) }
                View { attr { padding(left = 20f, right = 20f, top = 10f, bottom = 10f); backgroundColor(YijianColors.surfaceLight); borderRadius(YijianTheme.Radius.md) }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(YijianColors.textPrimary) } } }
                View { attr { padding(left = 20f, right = 20f, top = 10f, bottom = 10f); backgroundLinearGradient(Direction.TO_RIGHT, ColorStop(YijianColors.gradientStart, 0f), ColorStop(YijianColors.gradientEnd, 1f)); borderRadius(YijianTheme.Radius.md); marginLeft(12f) }
                    event { click { onConfirm() } }
                    Text { attr { text("保存"); fontSize(14f); color(YijianColors.textPrimary); fontWeightBold() } } }
            }
        }
    }
}
// 剪辑 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ClipTabContent(ctx: HomePage, mgr: DraftManager) {
    val innerW = ctx.pagerData.pageViewWidth - YijianTheme.Spacing.sm * 2
    val cardWidth = ((innerW - YijianTheme.Spacing.xs * 6) / 3f * 100f).toInt().toFloat() / 100f

    View {
        attr { flex(1f); flexDirectionColumn(); paddingTop(YijianTheme.Spacing.md) }

        // 新建剪辑按钮
        View {
            attr { marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg); height(56f); borderRadius(12f)
                backgroundLinearGradient(Direction.TO_RIGHT, ColorStop(YijianColors.gradientStart, 0f), ColorStop(YijianColors.gradientEnd, 1f))
                flexDirectionRow(); alignItemsCenter(); justifyContentCenter() }
            event { click { if (mgr.isEditing) mgr.exitEditing(); ctx.pickAndEditVideo() } }
            Text { attr { text("🎬"); fontSize(20f); marginRight(8f) } }
            Text { attr { text("新建剪辑"); fontSize(16f); fontWeightBold(); color(YijianColors.textPrimary) } }
        }

        // 草稿箱标题
        View { attr { flexDirectionRow(); alignItemsCenter(); paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.sm) }
            Text { attr { text("草稿箱"); fontSize(14f); fontWeightBold(); color(YijianColors.textPrimary); flex(1f) } }
            Text { attr { text("全部 >"); fontSize(12f); color(YijianColors.textTertiary) } }
        }

        // 草稿列表
        Scroller {
            attr { flex(1f); flexDirectionColumn(); paddingLeft(YijianTheme.Spacing.sm); paddingRight(YijianTheme.Spacing.sm) }
            vif({ mgr.draftList.isEmpty() }) {
                View { attr { padding(YijianTheme.Spacing.xxl); allCenter(); flexDirectionColumn() }
                    Text { attr { text("📁"); fontSize(48f); marginBottom(YijianTheme.Spacing.md) } }
                    Text { attr { text("暂无草稿"); fontSize(16f); color(YijianColors.textSecondary); marginBottom(6f) } }
                    Text { attr { text("点击「新建剪辑」开始创作吧"); fontSize(12f); color(YijianColors.textTertiary) } }
                }
            }
            velse {
                View { attr { width(innerW); flexDirectionRow(); flexWrapWrap() }
                    vfor({ mgr.draftList }) { draft ->
                        DraftCard(ctx, mgr, draft, cardWidth)
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 草稿卡片
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.DraftCard(ctx: HomePage, mgr: DraftManager, video: VideoInfo, cardWidth: Float) {
    var handledByLongPress = false
    View {
        attr { size(cardWidth, cardWidth * 9f / 16f + 52f); flexDirectionColumn(); margin(YijianTheme.Spacing.xs); backgroundColor(YijianColors.surface); borderRadius(YijianTheme.Radius.md); overflow(true) }
        event {
            longPress {
                handledByLongPress = true
                if (!mgr.isEditing) mgr.enterSelection(video.id)
            }
            click {
                if (handledByLongPress) { handledByLongPress = false; return@click }
                if (mgr.isEditing) { mgr.toggleSelection(video.id) } else {
                    if (!fileExists(video.path)) { ctx.showError("视频不存在: ${video.title}"); return@click }
                    val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
                    ctx.jumpPage("EditorPage", params)
                }
            }
        }
        View {
            attr { size(cardWidth, cardWidth * 9f / 16f); backgroundColor(YijianColors.backgroundLight); allCenter() }
            Text { attr { text("🎞"); fontSize(24f) } }
            if (video.duration > 0) {
                View { attr { absolutePosition(bottom = 4f, right = 4f); paddingLeft(4f); paddingRight(4f); paddingTop(1f); paddingBottom(1f); backgroundColor(Color(0xCC000000)); borderRadius(3f) }
                    Text { attr { text(video.formattedDuration); fontSize(9f); color(YijianColors.textPrimary) } } }
            }
            vif({ mgr.isEditing }) {
                val sel = mgr.selectedIds.contains(video.id)
                View { attr { absolutePosition(top = 4f, right = 4f); size(20f, 20f); borderRadius(10f); backgroundColor(if (sel) YijianColors.primary else Color(0x00000000)); border(Border(2f, BorderStyle.SOLID, if (sel) YijianColors.primary else Color(0x88FFFFFF))); allCenter() }
                    vif({ sel }) { Text { attr { text("✓"); fontSize(12f); color(Color(0xFFFFFFFF)); fontWeightBold() } } }
                }
            }
        }
        View { attr { flex(1f); padding(all = 4f); justifyContentCenter() }
            Text { attr { text(video.title); fontSize(11f); color(YijianColors.textPrimary); lines(1) } }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 学习 Tab（空占位）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.LearnTabContent() {
    View { attr { flex(1f); backgroundColor(YijianColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("🎓"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(YijianColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(YijianColors.textSecondary) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 我的 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
    Scroller { attr { flex(1f); backgroundColor(YijianColors.background); flexDirectionColumn(); paddingTop(YijianTheme.Spacing.xxl) }
        View { attr { allCenter(); flexDirectionColumn() }
            View { attr { size(80f, 80f); borderRadius(40f); backgroundColor(YijianColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } } }
        }
        View { attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text { attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(YijianColors.textPrimary) } }
            Text { attr { text(" ✏"); fontSize(14f); color(YijianColors.textTertiary) } }
        }
        View { attr { allCenter(); marginBottom(YijianTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text { attr { text(ctx.userProfile.bio); fontSize(14f); color(YijianColors.textSecondary); textAlignCenter(); lines(3) } }
        }
        View { attr { height(1f); backgroundColor(YijianColors.surfaceLight); marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg) } }
        View { attr { paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg) }
            Text { attr { text("我的统计"); fontSize(14f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(YijianTheme.Spacing.md) } }
            View { attr { flexDirectionRow() }
                listOf(Triple("4", "项目", "📁"), Triple("1", "本月", "📅"), Triple("3m12s", "时长", "⏱")).forEach { (value, label, icon) ->
                    View { attr { flex(1f); backgroundColor(YijianColors.surface); borderRadius(YijianTheme.Radius.md); padding(all = YijianTheme.Spacing.md); allCenter(); flexDirectionColumn(); marginLeft(4f); marginRight(4f) }
                        Text { attr { text(icon); fontSize(20f); marginBottom(4f) } }
                        Text { attr { text(value); fontSize(18f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(2f) } }
                        Text { attr { text(label); fontSize(11f); color(YijianColors.textTertiary) } }
                    }
                }
            }
        }
        View { attr { height(1f); backgroundColor(YijianColors.surfaceLight); marginBottom(YijianTheme.Spacing.sm) } }
        listOf("⚙  设置", "📱  关于一剪").forEach { label ->
            View { attr { height(52f); paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg); flexDirectionRow(); alignItemsCenter(); backgroundColor(YijianColors.background) }
                Text { attr { text(label); fontSize(14f); color(YijianColors.textPrimary); flex(1f) } }
                Text { attr { text(">"); fontSize(14f); color(YijianColors.textTertiary) } }
                View { attr { absolutePosition(bottom = 0f, left = YijianTheme.Spacing.lg, right = 0f); height(1f); backgroundColor(YijianColors.surfaceLight) } }
            }
        }
        View { attr { height(YijianTheme.Spacing.xxxl) } }
    }
}
