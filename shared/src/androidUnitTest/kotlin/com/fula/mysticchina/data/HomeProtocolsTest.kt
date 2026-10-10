package com.fula.mysticchina.data

import com.fula.mysticchina.App
import com.fula.mysticchina.pages.HomePage
import com.fula.pages.ProtocolPage
import com.fula.protocol.PROTOCOL_PAGE_NAME
import com.fula.protocol.pb.v1.ProtocolResponse
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HomeProtocolsTest {
    private val assets = listOf(File("src/commonMain/assets"), File("shared/src/commonMain/assets"))
        .first(File::isDirectory)

    @Test
    fun tabsUseDistinctCardProtocolsAndOnlyOpenTheWritingGame() {
        val home = HomePage()
        assertTrue(home is ProtocolPage)
        assertEquals(0, home.selectedTab)
        assertEquals(MockNetwork.pages.keys.toList(), (0..2).map(home::pageIDForTab))
        MockNetwork.pages.values.forEach { resource ->
            val response = protocolJsonToPb(File(assets, "common/protocol_pages/$resource").readText())
            val decoded = ProtocolResponse.ADAPTER.decode(ProtocolResponse.ADAPTER.encode(response))
            val page = requireNotNull(decoded.page)
            assertEquals(false, page.enabled_pullrefresh)
            assertTrue(page.header_.single().fitsSystemWindows)
            assertTrue(page.body.isNotEmpty())
            val entry = page.body.single { it.componentId == "fula_merchant_entry_card" }
            val data = JSONObject(requireNotNull(entry.rawCard).jsonData)
            assertTrue(data.optBoolean("clickable"))
            assertEquals(App.HANZI_PAGE_ID, data.optString("pageID"))
            assertEquals("HanziPage", App.resolvePageName(PROTOCOL_PAGE_NAME, data.optString("pageID")))
        }
        assertEquals("OtherPage", App.resolvePageName("OtherPage", App.HANZI_PAGE_ID))
        assertEquals(PROTOCOL_PAGE_NAME, App.resolvePageName(PROTOCOL_PAGE_NAME, "mysticchina_explore"))
        assertTrue(File(assets, "hanzi/game.html").isFile)
        assertTrue(File(assets, "hanzi/hanzi-writer.min.js").isFile)
        assertTrue(File(assets, "hanzi/data").listFiles().orEmpty().isNotEmpty())
    }

    @Test
    fun requestsParseInBackgroundAndReportMissingOrInvalidResources() {
        fun request(id: String, read: (String) -> String?): ProtocolResponse {
            val done = CountDownLatch(1)
            val result = AtomicReference<Result<ProtocolResponse>>()
            val caller = Thread.currentThread()
            MockNetwork.request(id, readAsset = {
                assertTrue(Thread.currentThread() !== caller)
                read(it)
            }) { result.set(it); done.countDown() }
            assertTrue(done.await(10, TimeUnit.SECONDS))
            return result.get().getOrThrow()
        }
        assertEquals(0, request("mysticchina_explore") { File(assets, it).readText() }.code)
        assertEquals(404, request("unknown") { error("Must not read an unknown page") }.code)
        assertEquals(404, request("mysticchina_explore") { null }.code)
        assertEquals(500, request("mysticchina_explore") { "invalid json" }.code)
    }
}
