package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.model.UserProfile
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme
import com.fula.exploringchina.util.Constants

private const val SP_NICKNAME = "exploringchina_nickname"
private const val SP_BIO = "exploringchina_bio"
private const val SP_AVATAR = "exploringchina_avatar"

private enum class HomeTab(val label: String, val icon: String) {
    EXPLORE("探索", "🧭"),
    LEARN("学习", "📚"),
    PROFILE("我的", "👤")
}

/** 文化功能卡片数据 */
private data class CultureCard(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val pageName: String?,   // null = 即将上线，禁用点击
)

private val CULTURE_CARDS = listOf(
    CultureCard("🖊", "汉字练习", "写好每一笔", Constants.PAGE_HANZI),
    CultureCard("🔜", "更多功能", "即将上线", null),
)

@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {

    var selectedTab by observable(0)
    var userProfile by observable(UserProfile())
    var showEditNickname by observable(false)
    var showEditBio by observable(false)
    var editingText by observable("")

    private val sp by lazy {
        acquireModule<SharedPreferencesModule>(SharedPreferencesModule.MODULE_NAME)
    }

    override fun created() {
        super.created()
        userProfile = UserProfile(
            nickname = sp.getString(SP_NICKNAME) ?: "文化探索者",
            bio = sp.getString(SP_BIO) ?: "探索中华文化之美",
            avatarEmoji = sp.getString(SP_AVATAR) ?: "🧭"
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
            attr { backgroundColor(YijianColors.background); flexDirectionColumn() }

            View {
                attr { flex(1f); flexDirectionColumn() }
                vif({ ctx.selectedTab == 0 }) { ExploreTabContent(ctx) }
                vif({ ctx.selectedTab == 1 }) { LearnTabContent() }
                vif({ ctx.selectedTab == 2 }) { ProfileTabContent(ctx) }
            }

            View { attr { height(1f); backgroundColor(YijianColors.surfaceLight) } }

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
            backgroundColor(YijianColors.background)
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
                        backgroundColor(if (selected) YijianColors.primary else Color(0x00000000))
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.icon); fontSize(22f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        marginBottom(2f)
                    }
                }
                Text {
                    attr {
                        text(tab.label); fontSize(10f)
                        color(if (selected) YijianColors.primary else YijianColors.textTertiary)
                        if (selected) fontWeightSemiBold() else fontWeightNormal()
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 探索 Tab — 文化功能卡片网格
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ExploreTabContent(ctx: HomePage) {
    View {
        attr { flex(1f); flexDirectionColumn() }

        // 顶部标题栏
        View {
            attr {
                height(YijianTheme.BarHeight.topBar + ctx.pagerData.statusBarHeight)
                backgroundColor(YijianColors.background)
                flexDirectionRow()
                alignItemsCenter()
                paddingTop(ctx.pagerData.statusBarHeight)
                paddingLeft(YijianTheme.Spacing.lg)
                paddingRight(YijianTheme.Spacing.lg)
            }
            Text {
                attr {
                    text("探索中国文化")
                    fontSize(YijianTheme.FontSize.title)
                    color(YijianColors.textPrimary)
                    fontWeightBold()
                    flex(1f)
                }
            }
        }

        // 卡片网格
        val cardMargin = YijianTheme.Spacing.sm
        val cardSize = (ctx.pagerData.pageViewWidth - cardMargin * 3) / 2f

        Scroller {
            attr { flex(1f); flexDirectionColumn(); paddingTop(YijianTheme.Spacing.md) }
            View {
                attr {
                    flexDirectionRow()
                    flexWrapWrap()
                    paddingLeft(cardMargin)
                    paddingRight(cardMargin)
                }
                CULTURE_CARDS.forEach { card ->
                    CultureCardView(ctx, card, cardSize, cardMargin)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 文化功能卡片
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.CultureCardView(
    ctx: HomePage,
    card: CultureCard,
    cardSize: Float,
    margin: Float
) {
    val enabled = card.pageName != null
    View {
        attr {
            size(cardSize, cardSize)
            margin(margin / 2)
            borderRadius(YijianTheme.Radius.xl)
            overflow(true)
            flexDirectionColumn()
            allCenter()
            if (enabled) {
                backgroundLinearGradient(
                    Direction.TO_BOTTOM_RIGHT,
                    ColorStop(YijianColors.gradientStart, 0f),
                    ColorStop(YijianColors.gradientEnd, 1f)
                )
            } else {
                backgroundColor(YijianColors.surface)
            }
        }
        if (enabled) {
            event { click { ctx.jumpPage(card.pageName!!) } }
        }
        Text {
            attr {
                text(card.emoji)
                fontSize(48f)
                marginBottom(YijianTheme.Spacing.md)
            }
        }
        Text {
            attr {
                text(card.title)
                fontSize(YijianTheme.FontSize.subtitle)
                fontWeightBold()
                color(if (enabled) YijianColors.textPrimary else YijianColors.textTertiary)
                marginBottom(YijianTheme.Spacing.xs)
            }
        }
        Text {
            attr {
                text(card.subtitle)
                fontSize(YijianTheme.FontSize.small)
                color(if (enabled) YijianColors.textSecondary else YijianColors.textDisabled)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// 学习 Tab（占位）
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.LearnTabContent() {
    View {
        attr { flex(1f); backgroundColor(YijianColors.background); allCenter(); flexDirectionColumn() }
        Text { attr { text("📚"); fontSize(48f); marginBottom(16f) } }
        Text { attr { text("即将上线"); fontSize(16f); color(YijianColors.textPrimary); marginBottom(8f) } }
        Text { attr { text("学习内容正在精心准备中..."); fontSize(12f); color(YijianColors.textSecondary) } }
    }
}

// ═══════════════════════════════════════════════════════════
// 我的 Tab
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.ProfileTabContent(ctx: HomePage) {
    Scroller {
        attr { flex(1f); backgroundColor(YijianColors.background); flexDirectionColumn(); paddingTop(YijianTheme.Spacing.xxl) }
        View { attr { allCenter(); flexDirectionColumn() }
            View {
                attr { size(80f, 80f); borderRadius(40f); backgroundColor(YijianColors.surface); allCenter() }
                Text { attr { text(ctx.userProfile.avatarEmoji); fontSize(36f) } }
            }
        }
        View {
            attr { allCenter(); marginBottom(8f); flexDirectionRow(); justifyContentCenter(); alignItemsCenter() }
            event { click { ctx.editingText = ctx.userProfile.nickname; ctx.showEditNickname = true } }
            Text { attr { text(ctx.userProfile.nickname); fontSize(20f); fontWeightBold(); color(YijianColors.textPrimary) } }
            Text { attr { text(" ✏"); fontSize(14f); color(YijianColors.textTertiary) } }
        }
        View {
            attr { allCenter(); marginBottom(YijianTheme.Spacing.xl); paddingLeft(32f); paddingRight(32f) }
            event { click { ctx.editingText = ctx.userProfile.bio; ctx.showEditBio = true } }
            Text { attr { text(ctx.userProfile.bio); fontSize(14f); color(YijianColors.textSecondary); textAlignCenter(); lines(3) } }
        }
        View {
            attr {
                height(1f); backgroundColor(YijianColors.surfaceLight)
                marginLeft(YijianTheme.Spacing.lg); marginRight(YijianTheme.Spacing.lg); marginBottom(YijianTheme.Spacing.lg)
            }
        }
        listOf("⚙  设置", "📱  关于探索中国").forEach { label ->
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

// ═══════════════════════════════════════════════════════════
// 编辑覆盖层
// ═══════════════════════════════════════════════════════════
private fun ViewContainer<*, *>.EditOverlay(
    ctx: HomePage,
    title: String,
    placeholder: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    View {
        attr { absolutePositionAllZero(); backgroundColor(Color(0xCC000000)); allCenter() }
        event { click { onDismiss() } }
        View {
            attr {
                width(ctx.pagerData.pageViewWidth - 48f)
                backgroundColor(YijianColors.surface); borderRadius(16f)
                padding(all = YijianTheme.Spacing.lg); flexDirectionColumn()
            }
            event { click {} }
            Text {
                attr { text(title); fontSize(16f); fontWeightBold(); color(YijianColors.textPrimary); marginBottom(YijianTheme.Spacing.md) }
            }
            View {
                attr { flexDirectionRow(); justifyContentFlexEnd(); marginTop(YijianTheme.Spacing.lg) }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundColor(YijianColors.surfaceLight); borderRadius(YijianTheme.Radius.md)
                    }
                    event { click { onDismiss() } }
                    Text { attr { text("取消"); fontSize(14f); color(YijianColors.textPrimary) } }
                }
                View {
                    attr {
                        padding(left = 20f, right = 20f, top = 10f, bottom = 10f)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        borderRadius(YijianTheme.Radius.md); marginLeft(12f)
                    }
                    event { click { onConfirm() } }
                    Text { attr { text("保存"); fontSize(14f); color(YijianColors.textPrimary); fontWeightBold() } }
                }
            }
        }
    }
}
