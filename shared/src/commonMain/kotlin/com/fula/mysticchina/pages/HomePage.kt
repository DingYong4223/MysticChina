package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.components.ExploreTabContent
import com.fula.mysticchina.model.UserProfile
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

private const val SP_NICKNAME = "mysticchina_nickname"
private const val SP_BIO      = "mysticchina_bio"
private const val SP_AVATAR   = "mysticchina_avatar"

private enum class HomeTab(val label: String, val icon: String) {
    EXPLORE("探索", "🧭"),
    LEARN("学习", "📚"),
    PROFILE("我的", "👤")
}

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    var selectedTab    by observable(0)
    var userProfile    by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio      by observable(false)
    var editingText      by observable("")

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    override fun created() {
        super.created()
        userProfile = UserProfile(
            nickname    = sp.getString(SP_NICKNAME) ?: "文化探索者",
            bio         = sp.getString(SP_BIO)      ?: "探索中华文化之美",
            avatarEmoji = sp.getString(SP_AVATAR)   ?: "🧭",
        )
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
                vif({ ctx.selectedTab == 0 }) { ExploreTabContent(ctx) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            View { attr { height(1f); backgroundColor(MysticChinaColors.divider) } }

            BottomTabBar(ctx)

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
// 底部 Tab Bar
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.BottomTabBar(ctx: HomePage) {
    View {
        attr {
            height(56f + ctx.pagerData.safeAreaInsets.bottom)
            backgroundColor(MysticChinaColors.backgroundLight)
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(ctx.pagerData.safeAreaInsets.bottom)
        }
        HomeTab.values().forEachIndexed { index, tab ->
            val selected = ctx.selectedTab == index
            View {
                attr { flex(1f); height(56f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter() }
                event { click { ctx.selectedTab = index } }
                View {
                    attr {
                        size(4f, 4f); borderRadius(2f)
                        backgroundColor(if (selected) MysticChinaColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.icon); fontSize(22f)
                        color(if (selected) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.label); fontSize(10f)
                        color(if (selected) MysticChinaColors.primary else MysticChinaColors.textTertiary)
                        if (selected) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 学习 Tab（占位）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.LearnTabContent() {
    View {
        attr { flex(1f); backgroundColor(MysticChinaColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("📚"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(MysticChinaColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(MysticChinaColors.textSecondary) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 我的 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
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
        listOf("⚙  设置", "📱  关于神秘中国").forEach { label ->
            View {
                attr {
                    height(52f); paddingLeft(MysticChinaTheme.Spacing.lg); paddingRight(MysticChinaTheme.Spacing.lg)
                    flexDirectionRow(); alignItemsCenter(); backgroundColor(MysticChinaColors.background)
                }
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
        View { attr { height(MysticChinaTheme.Spacing.xxxl) } }
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
