package com.fula.mysticchina.pages

import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.components.protocol.ProtocolComponentView
import com.fula.mysticchina.components.protocol.estimatedComponentHeight
import com.fula.mysticchina.protocol.PARAM_PROTOCOL_JSON
import com.fula.mysticchina.protocol.PARAM_PROTOCOL_URL
import com.fula.mysticchina.protocol.PROTOCOL_PAGE_NAME
import com.fula.mysticchina.protocol.ProtocolAction
import com.fula.mysticchina.protocol.ProtocolComponent
import com.fula.mysticchina.protocol.ProtocolPageData
import com.fula.mysticchina.protocol.parseProtocolResponse
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.directives.velse
import com.tencent.kuikly.core.directives.velseif
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.directives.vforLazy
import com.tencent.kuikly.core.directives.vif
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.module.ModuleConst
import com.tencent.kuikly.core.module.NetworkModule
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.reactive.collection.ObservableList
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.FooterRefresh
import com.tencent.kuikly.core.views.FooterRefreshEndState
import com.tencent.kuikly.core.views.FooterRefreshState
import com.tencent.kuikly.core.views.FooterRefreshView
import com.tencent.kuikly.core.views.List
import com.tencent.kuikly.core.views.Refresh
import com.tencent.kuikly.core.views.RefreshView
import com.tencent.kuikly.core.views.RefreshViewState
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

private const val TAG = "ProtocolPage"

private enum class ProtocolPagePhase { LOADING, CONTENT, EMPTY, ERROR }

@Page(PROTOCOL_PAGE_NAME, supportInLocal = true)
internal class ProtocolPage : BasePager() {

    private var phase by observable(ProtocolPagePhase.LOADING)
    private var errorMessage by observable("")
    internal var selectedFilterId by observable("")
    private var loadingMore by observable(false)
    private var flowComponents: ObservableList<ProtocolComponent> by observableList()
    private var pinnedComponents: ObservableList<ProtocolComponent> by observableList()
    private var floatingComponents: ObservableList<ProtocolComponent> by observableList()

    private var header = emptyList<ProtocolComponent>()
    private var body = emptyList<ProtocolComponent>()
    private var footer = emptyList<ProtocolComponent>()
    private var stickyThresholds = emptyList<Pair<ProtocolComponent, Float>>()
    private var hasMore = false
    private var nextPage = 0
    private var requestVersion = 0
    private var protocolUrl = ""
    private var protocolJson = ""
    private var refreshRef: ViewRef<RefreshView>? = null
    private var footerRefreshRef: ViewRef<FooterRefreshView>? = null

    private val network by lazy { acquireModule<NetworkModule>(ModuleConst.NETWORK) }

    override fun created() {
        super.created()
        protocolUrl = pageData.params.optString(PARAM_PROTOCOL_URL).trim()
        protocolJson = pageData.params.optString(PARAM_PROTOCOL_JSON).trim()
        loadPage(replace = true)
    }

    override fun pageWillDestroy() {
        requestVersion++
        super.pageWillDestroy()
    }

    internal fun dispatch(action: ProtocolAction) {
        when (action) {
            ProtocolAction.Back -> closePage()
            ProtocolAction.Refresh -> loadPage(replace = true)
            is ProtocolAction.Filter -> {
                if (action.id.length > 64) return
                selectedFilterId = action.id
                loadPage(replace = true)
            }
            is ProtocolAction.OpenPage -> {
                if (!SAFE_PAGE_NAME.matches(action.pageName)) {
                    KLog.e(TAG, "Blocked invalid page name: ${action.pageName}")
                    return
                }
                val params = action.params.toString()
                if (params.length <= MAX_ACTION_PARAMS_LENGTH) jumpPage(action.pageName, params)
            }
        }
    }

    private fun loadPage(replace: Boolean) {
        if (loadingMore || (!replace && !hasMore)) return
        val requestPage = if (replace) 0 else nextPage
        val version = ++requestVersion
        if (replace && flowComponents.isEmpty()) phase = ProtocolPagePhase.LOADING else loadingMore = !replace

        if (protocolJson.isNotEmpty()) {
            setTimeout(0) { applyRawResponse(protocolJson, replace, version) }
            return
        }
        if (!isAllowedProtocolUrl(protocolUrl)) {
            fail("缺少有效的协议地址", replace, version)
            return
        }
        val params = JSONObject().apply {
            put("page_no", requestPage)
            if (selectedFilterId.isNotEmpty()) put("filter_id", selectedFilterId)
        }
        network.requestGet(protocolUrl, params) { data, success, error, _ ->
            setTimeout(0) {
                if (success) applyResponse(data, replace, version)
                else fail(error.ifEmpty { "协议加载失败" }, replace, version)
            }
        }
    }

    private fun applyRawResponse(raw: String, replace: Boolean, version: Int) {
        val root = try {
            JSONObject(raw)
        } catch (_: Throwable) {
            fail("协议 JSON 格式错误", replace, version)
            return
        }
        applyResponse(root, replace, version)
    }

    private fun applyResponse(root: JSONObject, replace: Boolean, version: Int) {
        if (version != requestVersion) return
        val result = try {
            parseProtocolResponse(root)
        } catch (error: Throwable) {
            fail(error.message ?: "协议解析失败", replace, version)
            return
        }
        try {
            if (replace) replaceContent(result) else appendContent(result)
        } catch (error: Throwable) {
            fail(error.message ?: "协议内容更新失败", replace, version)
            return
        }
        hasMore = result.hasMore
        nextPage = result.pageNo + 1
        loadingMore = false
        phase = if (flowComponents.isEmpty() && floatingComponents.isEmpty()) {
            ProtocolPagePhase.EMPTY
        } else {
            ProtocolPagePhase.CONTENT
        }
        refreshRef?.view?.endRefresh()
        footerRefreshRef?.view?.resetRefreshState(
            if (hasMore) FooterRefreshState.IDLE else FooterRefreshState.NONE_MORE_DATA
        )
    }

    private fun replaceContent(result: ProtocolPageData) {
        header = result.header
        body = result.body
        footer = result.footer
        floatingComponents.clear()
        floatingComponents.addAll(result.floating)
        syncFlow()
    }

    private fun appendContent(result: ProtocolPageData) {
        val existingIds = (header + body + footer).mapTo(mutableSetOf()) { it.dataId }
        require(result.body.none { it.dataId in existingIds }) { "Load-more response contains duplicate data_id" }
        body = body + result.body
        if (result.footer.isNotEmpty()) footer = result.footer
        syncFlow()
    }

    private fun syncFlow() {
        val updated = header + body + footer
        flowComponents.clear()
        flowComponents.addAll(updated)
        rebuildStickyThresholds(updated)
    }

    private fun rebuildStickyThresholds(components: List<ProtocolComponent>) {
        var offset = 0f
        stickyThresholds = buildList {
            components.forEach { component ->
                if (component.sticky) add(component to offset)
                offset += estimatedComponentHeight(
                    component,
                    pagerData.pageViewWidth,
                    pagerData.statusBarHeight,
                )
            }
        }
        updatePinned(0f)
    }

    private fun updatePinned(offsetY: Float) {
        // ponytail: renderer heights are deterministic today; switch to measured frames when arbitrary components arrive.
        val visible = stickyThresholds.filter { (_, threshold) -> offsetY >= threshold }.map { it.first }
        if (visible.map { it.dataId } == pinnedComponents.map { it.dataId }) return
        pinnedComponents.clear()
        pinnedComponents.addAll(visible)
    }

    private fun fail(message: String, replace: Boolean, version: Int) {
        if (version != requestVersion) return
        loadingMore = false
        refreshRef?.view?.endRefresh()
        footerRefreshRef?.view?.endRefresh(FooterRefreshEndState.FAILURE)
        if (replace && flowComponents.isEmpty()) {
            errorMessage = message
            phase = ProtocolPagePhase.ERROR
        } else {
            KLog.e(TAG, message)
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(Color(0xFFF7F3EE)); flexDirectionColumn() }

            vif({ ctx.phase == ProtocolPagePhase.LOADING }) {
                PageMessage("正在加载…")
            }
            velseif({ ctx.phase == ProtocolPagePhase.ERROR }) {
                View {
                    attr { flex(1f); allCenter(); flexDirectionColumn(); padding(all = 24f) }
                    Text { attr { text(ctx.errorMessage); fontSize(14f); color(Color(0xFF666666)); textAlignCenter() } }
                    View {
                        attr { marginTop(16f); height(40f); paddingLeft(24f); paddingRight(24f); borderRadius(20f); backgroundColor(Color(0xFFCC1111)); allCenter() }
                        event { click { ctx.loadPage(replace = true) } }
                        Text { attr { text("重试"); fontSize(14f); color(Color.WHITE) } }
                    }
                }
            }
            velseif({ ctx.phase == ProtocolPagePhase.EMPTY }) {
                PageMessage("暂无内容")
            }
            velse {
                View {
                    attr { flex(1f) }
                    List {
                        attr { flex(1f); backgroundColor(Color(0xFFF7F3EE)); showScrollerIndicator(false) }
                        event { scroll { ctx.updatePinned(it.offsetY.toFloat()) } }
                        Refresh {
                            ref { ctx.refreshRef = it }
                            attr { height(48f); allCenter() }
                            event {
                                refreshStateDidChange {
                                    if (it == RefreshViewState.REFRESHING) ctx.loadPage(replace = true)
                                }
                            }
                            Text { attr { text("下拉刷新"); fontSize(12f); color(Color(0xFF888888)) } }
                        }
                        vforLazy({ ctx.flowComponents }, maxLoadItem = 24) { component, _, _ ->
                            ProtocolComponentView(component, ctx)
                        }
                        FooterRefresh {
                            ref { ctx.footerRefreshRef = it }
                            attr { height(52f); allCenter(); preloadDistance(120f) }
                            event {
                                refreshStateDidChange {
                                    if (it == FooterRefreshState.REFRESHING) ctx.loadPage(replace = false)
                                }
                            }
                            Text {
                                attr {
                                    text(if (ctx.loadingMore) "加载更多…" else if (ctx.hasMore) "上拉加载更多" else "没有更多了")
                                    fontSize(12f)
                                    color(Color(0xFF999999))
                                }
                            }
                        }
                    }
                    View {
                        attr { absolutePosition(top = 0f, left = 0f, right = 0f); flexDirectionColumn() }
                        vfor({ ctx.pinnedComponents }) { component ->
                            ProtocolComponentView(component, ctx)
                        }
                    }
                    View {
                        attr { absolutePosition(left = 0f, right = 0f, bottom = 0f); flexDirectionColumn() }
                        vfor({ ctx.floatingComponents }) { component ->
                            ProtocolComponentView(component, ctx)
                        }
                    }
                }
            }
        }
    }

    private companion object {
        val SAFE_PAGE_NAME = Regex("^[A-Za-z][A-Za-z0-9_]{0,63}$")
        const val MAX_ACTION_PARAMS_LENGTH = 8192

        fun isAllowedProtocolUrl(url: String): Boolean = url.startsWith("https://") ||
            url.startsWith("http://localhost/") || url.startsWith("http://127.0.0.1/")
    }
}

private fun com.tencent.kuikly.core.base.ViewContainer<*, *>.PageMessage(message: String) {
    View {
        attr { flex(1f); allCenter() }
        Text { attr { text(message); fontSize(14f); color(Color(0xFF777777)) } }
    }
}
