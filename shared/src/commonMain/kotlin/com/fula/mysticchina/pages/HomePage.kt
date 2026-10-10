package com.fula.mysticchina.pages

import com.fula.mysticchina.App
import com.fula.mysticchina.data.MockNetwork
import com.fula.pages.ProtocolPage
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.ImageUri
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

private data class HomeTab(val title: String, val icon: String, val pageID: String)

private val homeTabs = listOf(
    HomeTab("探索", "worker_home", "mysticchina_explore"),
    HomeTab("学习", "worker_find", "mysticchina_learn"),
    HomeTab("关于", "worker_me", "mysticchina_about"),
)

@Page(App.START_PAGE, supportInLocal = true)
internal class HomePage : ProtocolPage() {
    var selectedTab by observable(0)
        private set

    override fun created() {
        MockNetwork.install()
        super.created()
    }

    override fun debugUIInspector(): Boolean = false
    override fun initialPageID(): String = pageIDForTab(0)

    internal fun pageIDForTab(index: Int): String = homeTabs[index].pageID

    internal fun showTab(index: Int) {
        if (index !in homeTabs.indices || selectedTab == index) return
        selectedTab = index
        showProtocolPage(pageIDForTab(index))
    }

    override fun body(): ViewBuilder {
        val ctx = this
        val protocolBody = super.body()
        return {
            attr { backgroundColor(Color(0xFFE8F0F8)); flexDirectionColumn() }
            View {
                attr { flex(1f) }
                protocolBody()
            }
            View { attr { height(1f); backgroundColor(Color(0x26264D7C)) } }
            BottomTabs(ctx)
        }
    }
}

private fun ViewContainer<*, *>.BottomTabs(ctx: HomePage) {
    View {
        attr {
            height(56f + ctx.pagerData.safeAreaInsets.bottom)
            paddingBottom(ctx.pagerData.safeAreaInsets.bottom)
            backgroundColor(Color.WHITE)
            flexDirectionRow()
            alignItemsCenter()
        }
        homeTabs.forEachIndexed { index, tab ->
            View {
                attr {
                    flex(1f); height(56f); flexDirectionColumn()
                    alignItemsCenter(); justifyContentCenter(); accessibility(tab.title)
                }
                event { click { ctx.showTab(index) } }
                Image { attr {
                    size(26f, 26f)
                    src(ImageUri.commonAssets("tabs/${tab.icon}_${if (ctx.selectedTab == index) "on" else "off"}.png"))
                    resizeContain()
                } }
                Text { attr {
                    text(tab.title); fontSize(10f)
                    color(if (ctx.selectedTab == index) Color(0xFFFF852B) else Color(0xCC1A1A2E))
                } }
            }
        }
    }
}
