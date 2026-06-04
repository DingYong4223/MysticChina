package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.model.UserProfile
import com.yijian.model.VideoInfo
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme
import com.yijian.util.currentTimeMs
import com.yijian.components.DraftActionBar
import com.yijian.components.DraftActionBarConfig
import com.yijian.manager.DraftManager
import com.yijian.util.fileExists

// ─── SP 键名常量 ───
private const val SP_NICKNAME = "yijian_nickname"
private const val SP_BIO = "yijian_bio"
private const val SP_AVATAR = "yijian_avatar"

// ─── Tab 枚举 ───
private enum class HomeTab(val label: String, val icon: String) {
    CLIP("剪辑", "✂"),
    LEARN("学习", "🎓"),
    PROFILE("我的", "👤")
}

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    // ─── 导航状态 ───
    var selectedTab by observable(0)                    // 0=剪辑 1=学习 2=我的

    // ─── 我的 Tab 状态 ───
    var userProfile by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio by observable(false)
    var editingText by observable("")
    var errorMessages by observableList<String>()
    fun showError(msg: String) { errorMessages.add(msg) }
    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }
    val draftMgr by lazy { DraftManager(sp) }
    private val backCallback = object : BackPressCallback() {
        override fun handleOnBackPressed() {
            if (draftMgr.isEditing) {
                draftMgr.exitEditing()
                getBackPressHandler().removeCallback(this)
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
        getBackPressHandler().addCallback(backCallback)
        if (draftMgr.draftList.isEmpty()) {
            draftMgr.add(VideoInfo("1", "午后阳光.mp4", "test1",
                duration = 15200L, createTime = currentTimeMs() - 7200000L))
            draftMgr.add(VideoInfo("2", "城市街景.mp4", "test2",
                duration = 45000L, createTime = currentTimeMs() - 86400000L))
            draftMgr.add(VideoInfo("3", "旅行记录.mp4", "test3",
                duration = 120000L, createTime = currentTimeMs() - 172800000L))
            draftMgr.add(VideoInfo("4", "美食制作.mp4", "test4",
                duration = 32000L, createTime = currentTimeMs() - 3600000L))
        }
    }

    private fun saveNickname() {
        val newVal = editingText.trim()
        if (newVal.isNotEmpty()) {
            userProfile = userProfile.copy(nickname = newVal)
            sp.setString(SP_NICKNAME, newVal)
        }
        showEditNickname = false
        editingText = ""
    }

    private fun saveBio() {
        val newVal = editingText.trim()
        if (newVal.isNotEmpty()) {
            userProfile = userProfile.copy(bio = newVal)
            sp.setString(SP_BIO, newVal)
        }
        showEditBio = false
        editingText = ""
    }


    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }


            // ─── 内容区 ───
            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 0 }) { ClipTabContent(ctx, ctx.draftMgr) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            // ─── 分割线 ───
            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }

            // ─── 底部 Tab Bar ───
            BottomTabBar(ctx)

            // ─── 编辑昵称弹层 ───
            vif({ ctx.showEditNickname }) {
                EditOverlay(
                    ctx = ctx,
                    title = "修改昵称",
                    placeholder = ctx.userProfile.nickname,
                    onConfirm = { ctx.saveNickname() },
                    onDismiss = { ctx.showEditNickname = false; ctx.editingText = "" }
                )
            }

            // ─── 编辑简介弹层 ───
            vif({ ctx.showEditBio }) {
                EditOverlay(
                    ctx = ctx,
                    title = "修改简介",
                    placeholder = ctx.userProfile.bio,
                    onConfirm = { ctx.saveBio() },
                    onDismiss = { ctx.showEditBio = false; ctx.editingText = "" }
                )
            }

            // —— 错误提示栏 ——
            vfor({ ctx.errorMessages }) { msg ->
                View {
                    attr {
                        height(44f)
                        padding(top = 12f, bottom = 12f, left = 16f, right = 16f)
                        backgroundColor(Color(0xE8FF3B30))
                        flexDirectionRow(); alignItemsCenter()
                    }
                    event { click { ctx.errorMessages.clear() } }
                    View { attr { size(20f, 20f); borderRadius(10f); backgroundColor(Color(0xFFFFFFFF)); allCenter(); marginRight(10f) }
                        Text { attr { text("!"); fontSize(13f); color(Color(0xFFFF3B30)); fontWeightBold() } } }
                    Text { attr { text(msg); fontSize(13f); color(Color(0xFFFFFFFF)); flex(1f) } }
                    View { attr { size(24f, 24f); allCenter() }
                        Text { attr { text("✕"); fontSize(14f); color(Color(0xAAFFFFFF)) } } }
                }
            }

            // ─── 选中模式操作栏 ───
            vif({ ctx.draftMgr.isEditing }) {
                DraftActionBar(
                    config = DraftActionBarConfig(
                        isAllSelected = ctx.draftMgr.isAllSelected,
                        selectedCount = ctx.draftMgr.selectedCount,
                        onToggleSelectAll = {
                            if (ctx.draftMgr.isAllSelected) ctx.draftMgr.deselectAll()
                            else ctx.draftMgr.selectAll()
                        },
                        onDelete = { ctx.draftMgr.remove(ctx.draftMgr.selectedIds.toList()) },
                    ),
                    safeAreaBottom = ctx.pagerData.safeAreaInsets.bottom,
                )
            }

        }
    }
}

// ──────────────────────────────────────────────────────────
// 底部 Tab Bar
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.BottomTabBar(ctx: HomePage) {
    View {
        attr {
            height(56f + ctx.pagerData.safeAreaInsets.bottom)
            backgroundColor(YijianColors.background)
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(ctx.pagerData.safeAreaInsets.bottom)
        }

        HomeTab.values().forEachIndexed { index, tab ->
            val selected = ctx.selectedTab == index
            View {
                attr {
                    flex(1f)
                    height(56f)
                    flexDirectionColumn()
                    alignItemsCenter()
                    justifyContentCenter()
                }
                event { click { ctx.selectedTab = index } }

                // 选中指示点（占位保持对齐）
                View {
                    attr {
                        size(4f, 4f)
                        borderRadius(2f)
                        backgroundColor(if (selected) YijianColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }

                // 图标
                Text {
                    attr {
                        text(tab.icon)
                        fontSize(22f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        marginBottom(2f)
                    }
                }

                // 标签
                Text {
                    attr {
                        text(tab.label)
                        fontSize(10f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        if (selected) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// 编辑覆盖层（昵称 / 简介）
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.EditOverlay(
    ctx: HomePage,
    title: String,
    placeholder: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    View {
        attr {
            absolutePositionAllZero()
            backgroundColor(Color(0xCC000000))
            allCenter()
        }
        event { click { onDismiss() } }

        View {
            attr {
                width(ctx.pagerData.pageViewWidth - 48f)
                backgroundColor(YijianColors.surface)
                borderRadius(16f)
                padding(all = YijianTheme.Spacing.lg)
                flexDirectionColumn()
            }
            event { click { /* 阻止点击穿透 */ } }

            // 标题
            Text {
                attr {
                    text(title)
                    fontSize(16f)
                    fontWeightBold()
                    color(YijianColors.textPrimary)
                    marginBottom(YijianTheme.Spacing.md)
                }
            }

            // 输入框（Input.textDidChange 来源：input.md）
            Input {
                attr {
                    width(ctx.pagerData.pageViewWidth - 48f - YijianTheme.Spacing.lg * 2)
                    height(44f)
                    backgroundColor(YijianColors.backgroundLight)
                    borderRadius(8f)
                    fontSize(14f)
                    color(YijianColors.textPrimary)
                    placeholder(placeholder)
                    text(ctx.editingText)
                }
                event {
                    textDidChange { ctx.editingText = it.text }
                }
            }

            // 按钮行
            View {
                attr {
                    flexDirectionRow()
                    justifyContentFlexEnd()
                    marginTop(YijianTheme.Spacing.md)
                }

                // 取消
                View {
                    attr {
                        height(36f); paddingLeft(16f); paddingRight(16f)
                        borderRadius(18f)
                        backgroundColor(YijianColors.surfaceLight)
                        allCenter(); marginRight(8f)
                    }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(YijianColors.textSecondary) } }
                }

                // 确认
                View {
                    attr {
                        height(36f); paddingLeft(16f); paddingRight(16f)
                        borderRadius(18f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        allCenter()
                    }
                    event { click { onConfirm() } }
                    Text { attr { text("确认"); fontSize(14f); color(YijianColors.textPrimary); fontWeightBold() } }
                }
            }
        }
    }
}

// ──────────────────────────────────────────────────────────
// Tab 1：剪辑
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.ClipTabContent(ctx: HomePage, mgr: DraftManager) {
    View {
        attr { flex(1f); flexDirectionColumn(); backgroundColor(YijianColors.background) }

        // TopBar
        View {
            attr {
                height(YijianTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
                paddingTop(ctx.pagerData.statusBarHeight)
                paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg)
                flexDirectionRow(); alignItemsCenter()
            }
            Text {
                attr {
                    text("一剪"); fontSize(YijianTheme.FontSize.title)
                    fontWeightBold(); color(YijianColors.textPrimary); flex(1f)
                }
            }
        }

        // 新建剪辑按钮
        View {
            attr {
                marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg)
                marginBottom(YijianTheme.Spacing.lg)
                height(56f); borderRadius(12f)
                backgroundLinearGradient(
                    Direction.TO_RIGHT,
                    ColorStop(YijianColors.gradientStart, 0f),
                    ColorStop(YijianColors.gradientEnd, 1f)
                )
                flexDirectionRow(); alignItemsCenter(); justifyContentCenter()
            }
            event { click {
                if (mgr.isEditing) mgr.exitEditing()
                ctx.jumpPage("MainPage")
            } }

            Text { attr { text("🎬"); fontSize(20f); marginRight(8f) } }
            Text {
                attr {
                    text("新建剪辑"); fontSize(16f)
                    fontWeightBold(); color(YijianColors.textPrimary)
                }
            }
        }

        // 草稿箱标题行
        View {
            attr {
                flexDirectionRow(); alignItemsCenter()
                paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg)
                marginBottom(YijianTheme.Spacing.sm)
            }
            Text {
                attr { text("草稿箱"); fontSize(14f); fontWeightBold(); color(YijianColors.textPrimary); flex(1f) }
            }
            Text { attr { text("全部 >"); fontSize(12f); color(YijianColors.textTertiary) } }
        }

        // 草稿列表 / 空态
        vif({ mgr.draftList.isEmpty() }) {
            View {
                attr { flex(1f); allCenter(); flexDirectionColumn() }
                Text { attr { text("📂"); fontSize(48f); marginBottom(12f) } }
                Text { attr { text("暂无草稿"); fontSize(16f); color(YijianColors.textSecondary); marginBottom(6f) } }
                Text { attr { text("点击「新建剪辑」开始创作吧"); fontSize(12f); color(YijianColors.textTertiary) } }
            }
        }
        velse {
            Scroller {
                attr { flex(1f); flexDirectionColumn() }
                View {
                    attr {
                        flexDirectionRow(); flexWrapWrap()
                        paddingLeft(YijianTheme.Spacing.sm); paddingRight(YijianTheme.Spacing.sm)
                    }
                    val gap = YijianTheme.Spacing.xs
                    val cardWidth = (ctx.pagerData.pageViewWidth - YijianTheme.Spacing.sm * 2 - gap * 2) / 3f
                    for (draft in mgr.draftList) {
                        DraftCard(ctx, mgr, draft, cardWidth)
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.DraftCard(
    ctx: HomePage, mgr: DraftManager,
    video: VideoInfo, cardWidth: Float,
) {
    var touchStartMs = 0L
    val isSelected = mgr.selectedIds.contains(video.id)
    val isEditing = mgr.isEditing

    View {
        attr {
            size(cardWidth, cardWidth * 9f / 16f + 52f)
            flexDirectionColumn()
            margin(YijianTheme.Spacing.xs)
            backgroundColor(YijianColors.surface)
            borderRadius(YijianTheme.Radius.md)
            overflow(true)
            if (isSelected && isEditing) {
                border(Border(2f, BorderStyle.SOLID, YijianColors.primary))
            }
        }
        event {
            touchDown { touchStartMs = currentTimeMs() }
            touchUp {
                val elapsed = currentTimeMs() - touchStartMs
                if (elapsed >= 500L) {
                    // 长按
                    if (!isEditing) mgr.enterSelection(video.id)
                } else {
                    // 单击
                    if (isEditing) {
                        mgr.toggleSelection(video.id)
                    } else {
                        if (!fileExists(video.path)) {
                            ctx.showError("视频不存在: ${video.title}")
                            return@touchUp
                        }
                        val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
                        ctx.jumpPage("EditorPage", params)
                    }
                }
            }
        }

        // 缩略图
        View {
            attr {
                size(cardWidth, cardWidth * 9f / 16f)
                backgroundColor(YijianColors.backgroundLight); allCenter()
            }
            Text { attr { text("🎞"); fontSize(24f) } }

            if (video.duration > 0) {
                View {
                    attr {
                        absolutePosition(bottom = 4f, right = 4f)
                        paddingLeft(4f); paddingRight(4f); paddingTop(1f); paddingBottom(1f)
                        backgroundColor(Color(0xCC000000)); borderRadius(3f)
                    }
                    Text { attr { text(video.formattedDuration); fontSize(9f); color(YijianColors.textPrimary) } }
                }
            }

            // 选中模式 → 右上角 checkbox
            if (isEditing) {
                View {
                    attr {
                        absolutePosition(top = 4f, right = 4f)
                        size(20f, 20f); borderRadius(10f)
                        backgroundColor(if (isSelected) YijianColors.primary else Color(0x00000000))
                        border(Border(2f, BorderStyle.SOLID, if (isSelected) YijianColors.primary else Color(0x88FFFFFF)))
                        allCenter()
                    }
                    if (isSelected) {
                        Text { attr { text("✓"); fontSize(12f); color(Color(0xFFFFFFFF)); fontWeightBold() } }
                    }
                }
            }
        }

        // 标题
        View {
            attr { flex(1f); padding(all = 4f); justifyContentCenter() }
            Text {
                attr {
                    text(video.title); fontSize(11f); color(YijianColors.textPrimary)
                    lines(1)
                }
            }
        }
    }
}

private fun formatRelativeTime(createTime: Long): String {
    if (createTime == 0L) return "刚刚"
    val diff = currentTimeMs() - createTime
    return when {
        diff < 60_000L -> "刚刚"
        diff < 3_600_000L -> "${diff / 60_000L}分钟前"
        diff < 86_400_000L -> "${diff / 3_600_000L}小时前"
        else -> "${diff / 86_400_000L}天前"
    }
}

// ──────────────────────────────────────────────────────────
// Tab 2：学习（空占位）
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.LearnTabContent() {
    View {
        attr { flex(1f); backgroundColor(YijianColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("🎓"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(YijianColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(YijianColors.textSecondary) } }
    }
}

// ──────────────────────────────────────────────────────────
// Tab 3：我的
// ──────────────────────────────────────────────────────────
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
    Scroller {
        attr { flex(1f); flexDirectionColumn(); backgroundColor(YijianColors.background) }

        // 顶部安全区
        View { attr { height(ctx.pagerData.statusBarHeight + YijianTheme.Spacing.xl) } }

        // 头像
        View {
            attr { allCenter(); marginBottom(12f) }
            View {
                attr { size(80f, 80f); borderRadius(40f); backgroundColor(YijianColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } }
            }
        }

        // 昵称（可编辑）
        View {
            attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text {
                attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(YijianColors.textPrimary) }
            }
            Text { attr { text(" ✏"); fontSize(14f); color(YijianColors.textTertiary) } }
        }

        // 简介（可编辑）
        View {
            attr { allCenter(); marginBottom(YijianTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text {
                attr { text(ctx.userProfile.bio); fontSize(14f); color(YijianColors.textSecondary); textAlignCenter(); lines(3) }
            }
        }

        // 分割线
        View {
            attr {
                height(1f); backgroundColor(YijianColors.surfaceLight)
                marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg)
            }
        }

        // 统计
        View {
            attr { paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg) }
            Text { attr { text("我的统计"); fontSize(14f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(YijianTheme.Spacing.md) } }
            View {
                attr { flexDirectionRow() }
                listOf(Triple("4", "项目", "📁"), Triple("1", "本月", "📅"), Triple("3m12s", "时长", "⏱")).forEach { (value, label, icon) ->
                    View {
                        attr {
                            flex(1f); backgroundColor(YijianColors.surface); borderRadius(YijianTheme.Radius.md)
                            padding(all = YijianTheme.Spacing.md); allCenter(); flexDirectionColumn()
                            marginLeft(4f); marginRight(4f)
                        }
                        Text { attr { text(icon); fontSize(20f); marginBottom(4f) } }
                        Text { attr { text(value); fontSize(18f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(2f) } }
                        Text { attr { text(label); fontSize(11f); color(YijianColors.textTertiary) } }
                    }
                }
            }
        }

        // 分割线
        View { attr { height(1f); backgroundColor(YijianColors.surfaceLight); marginBottom(YijianTheme.Spacing.sm) } }

        // 设置列表项
        listOf("⚙  设置", "📱  关于一剪").forEach { label ->
            View {
                attr {
                    height(52f); paddingLeft(YijianTheme.Spacing.lg); paddingRight(YijianTheme.Spacing.lg)
                    flexDirectionRow(); alignItemsCenter(); backgroundColor(YijianColors.background)
                }
                Text { attr { text(label); fontSize(14f); color(YijianColors.textPrimary); flex(1f) } }
                Text { attr { text(">"); fontSize(14f); color(YijianColors.textTertiary) } }
                View {
                    attr {
                        absolutePosition(bottom = 0f, left = YijianTheme.Spacing.lg, right = 0f)
                        height(1f); backgroundColor(YijianColors.surfaceLight)
                    }
                }
            }
        }

        View { attr { height(YijianTheme.Spacing.xxxl) } }
    }
}


// ── 错误 Toast ——
private fun ViewContainer<*, *>.ErrorToast(message: String, onDismiss: () -> Unit) {
    View {
        attr {
            absolutePosition(top = 80f, left = 20f, right = 20f)
            zIndex(100)
            padding(top = 12f, bottom = 12f, left = 16f, right = 16f)
            backgroundColor(Color(0xE8FF3B30))
            borderRadius(10f)
            flexDirectionRow()
            alignItemsCenter()
        }
        event { click { onDismiss.invoke() } }

        View { attr { size(20f, 20f); borderRadius(10f); backgroundColor(Color(0xFFFFFFFF)); allCenter(); marginRight(10f) }
            Text { attr { text("!"); fontSize(13f); color(Color(0xFFFF3B30)); fontWeightBold() } } }

        Text { attr { text(message); fontSize(13f); color(Color(0xFFFFFFFF)); flex(1f) } }

        View { attr { size(24f, 24f); allCenter() }
            Text { attr { text("✕"); fontSize(14f); color(Color(0xAAFFFFFF)) } } }
    }
}