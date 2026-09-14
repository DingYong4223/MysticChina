package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.model.UserProfile
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme
import com.fula.mysticchina.theme.ThemeManager

private const val SP_NICKNAME    = "mysticchina_nickname"
private const val SP_BIO         = "mysticchina_bio"
private const val SP_AVATAR      = "mysticchina_avatar"
private const val SP_THEME_INDEX = "theme_index"

private enum class HomeTab(val label: String, val icon: String) {
    EXPLORE("探索", "🧭"),
    LEARN("学习", "📚"),
    ABOUT("关于", "ℹ️")
}

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    var selectedTab       by observable(2)
    var userProfile       by observable(UserProfile())
    var showEditNickname  by observable(false)
    var showEditBio       by observable(false)
    var editingText       by observable("")
    var themeVersion      by observable(0)   // 递增触发 UI 主题刷新

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    override fun created() {
        super.created()
        // 加载用户信息
        userProfile = UserProfile(
            nickname    = sp.getString(SP_NICKNAME) ?: "文化探索者",
            bio         = sp.getString(SP_BIO)      ?: "探索中华文化之美",
            avatarEmoji = sp.getString(SP_AVATAR)   ?: "🧭",
        )
        // 加载已保存的主题
        loadSavedTheme()
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        // 从 ThemePage 返回时，检测主题是否变化并刷新
        val savedIndex = sp.getInt(SP_THEME_INDEX) ?: ThemeManager.defaultThemeIndex
        if (savedIndex != ThemeManager.currentThemeIndex) {
            ThemeManager.applyTheme(savedIndex)
            forceUIUpdate()
        }
    }

    private fun loadSavedTheme() {
        val savedIndex = sp.getInt(SP_THEME_INDEX) ?: ThemeManager.defaultThemeIndex
        ThemeManager.applyTheme(savedIndex)
    }

    /**
     * 强制 UI 主题更新
     *
     * 步骤：
     * 1. themeVersion = -1 → vif({ themeVersion >= 0 }) 为 false，销毁底栏
     * 2. selectedTab 切换 → vif({ selectedTab == X }) 为 false，销毁当前 tab
     * 3. 下一帧：恢复 themeVersion 和 selectedTab → 用新主题重建所有视图
     */
    private fun forceUIUpdate() {
        val currentTab = selectedTab
        themeVersion = -1
        selectedTab = (currentTab + 1) % HomeTab.values().size
        setTimeout(0) {
            themeVersion = 0
            selectedTab = currentTab
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
            attr { backgroundColor(MysticChinaColors.background); flexDirectionColumn() }

            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 2 }) { AboutTabContent(ctx) }
            }

            // 分隔线 + 底部导航栏 — 包裹在 vif 中以便主题切换时重建
            vif({ ctx.themeVersion >= 0 }) {
                View { attr { height(1f); backgroundColor(MysticChinaColors.divider) } }
                BottomTabBar(ctx)
            }

            vif({ ctx.showEditNickname }) {
                EditOverlay(
                    ctx, "修改昵称", ctx.userProfile.nickname,
                    { ctx.saveNickname() },
                    { ctx.showEditNickname = false; ctx.editingText = "" }
                )
            }
            vif({ ctx.showEditBio }) {
                EditOverlay(
                    ctx, "修改简介", ctx.userProfile.bio,
                    { ctx.saveBio() },
                    { ctx.showEditBio = false; ctx.editingText = "" }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 底部 Tab Bar（背景跟随主题主背景色）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.BottomTabBar(ctx: HomePage) {
    View {
        attr {
            height(56f + ctx.pagerData.safeAreaInsets.bottom)
            backgroundColor(MysticChinaColors.background)
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(ctx.pagerData.safeAreaInsets.bottom)
        }
        HomeTab.values().forEachIndexed { index, tab ->
            View {
                attr { flex(1f); height(56f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter() }
                event { click { ctx.selectedTab = index } }
                View {
                    attr {
                        size(4f, 4f); borderRadius(2f)
                        backgroundColor(if (ctx.selectedTab == index) MysticChinaColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.icon); fontSize(22f)
                        color(if (ctx.selectedTab == index) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.label); fontSize(10f)
                        color(if (ctx.selectedTab == index) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                        if (ctx.selectedTab == index) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 关于 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.AboutTabContent(ctx: HomePage) {
    Scroller {
        attr { flex(1f); backgroundColor(MysticChinaColors.background); flexDirectionColumn(); paddingTop(MysticChinaTheme.Spacing.xxl) }
        View { attr { allCenter(); flexDirectionColumn() }
            View {
                attr { size(80f, 80f); borderRadius(40f); backgroundColor(MysticChinaColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } }
            }
        }
        View {
            attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text { attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(MysticChinaColors.textPrimary) } }
            Text { attr { text(" ✏"); fontSize(14f); color(MysticChinaColors.textTertiary) } }
        }
        View {
            attr { allCenter(); marginBottom(MysticChinaTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text { attr { text(ctx.userProfile.bio); fontSize(14f); color(MysticChinaColors.textSecondary); textAlignCenter(); lines(3) } }
        }
        View {
            attr {
                height(1f); backgroundColor(MysticChinaColors.divider)
                marginLeft(MysticChinaTheme.Spacing.lg); marginRight(MysticChinaTheme.Spacing.lg); marginBottom(MysticChinaTheme.Spacing.lg)
            }
        }

        SettingsRow("乐高二级页统一框架", ctx) { ctx.jumpPage("LegoPageModesPage") }
        // ⚙  设置 — 点击跳转主题选择
        SettingsRow("🎨  主题", ctx) { ctx.jumpPage("ThemePage") }
        // 分割线
        View {
            attr {
                height(1f); backgroundColor(MysticChinaColors.divider)
                marginLeft(MysticChinaTheme.Spacing.lg); marginRight(MysticChinaTheme.Spacing.lg)
            }
        }
        SettingsRow("📱  关于神秘中国", ctx)

        View { attr { height(MysticChinaTheme.Spacing.xxxl) } }
    }
}

/** 设置列表行：图标+文字 / 右箭头 */
private fun ViewContainer<*, *>.SettingsRow(label: String, ctx: HomePage, onClick: (() -> Unit)? = null) {
    View {
        attr {
            height(52f); paddingLeft(MysticChinaTheme.Spacing.lg); paddingRight(MysticChinaTheme.Spacing.lg)
            flexDirectionRow(); alignItemsCenter(); backgroundColor(MysticChinaColors.background)
        }
        if (onClick != null) { event { click { onClick() } } }
        Text { attr { text(label); fontSize(14f); color(MysticChinaColors.textPrimary); flex(1f) } }
        Text { attr { text(">"); fontSize(14f); color(MysticChinaColors.textTertiary) } }
        View {
            attr {
                absolutePosition(bottom = 0f, left = MysticChinaTheme.Spacing.lg, right = 0f)
                height(1f); backgroundColor(MysticChinaColors.divider)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 编辑覆盖层（昵称/简介）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.EditOverlay(
    ctx: HomePage,
    title: String,
    placeholder: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    View {
        attr { absolutePositionAllZero(); backgroundColor(Color(0xCC000000)); allCenter() }
        event { click { onDismiss() } }
        View {
            attr {
                width(ctx.pagerData.pageViewWidth - 48f)
                backgroundColor(MysticChinaColors.surface); borderRadius(16f)
                padding(all = MysticChinaTheme.Spacing.lg); flexDirectionColumn()
            }
            event { click {} }
            Text {
                attr { text(title); fontSize(16f); fontWeightBold(); color(MysticChinaColors.textPrimary); marginBottom(MysticChinaTheme.Spacing.md) }
            }
            View {
                attr { flexDirectionRow(); justifyContentFlexEnd(); marginTop(MysticChinaTheme.Spacing.lg) }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundColor(MysticChinaColors.surfaceLight); borderRadius(MysticChinaTheme.Radius.md)
                    }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(MysticChinaColors.textPrimary) } }
                }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(MysticChinaColors.gradientStart, 0f),
                            ColorStop(MysticChinaColors.gradientEnd, 1f),
                        )
                        borderRadius(MysticChinaTheme.Radius.md); marginLeft(12f)
                    }
                    event { click { onConfirm() } }
                    Text { attr { text("保存"); fontSize(14f); color(MysticChinaColors.textPrimary); fontWeightBold() } }
                }
            }
        }
    }
}
