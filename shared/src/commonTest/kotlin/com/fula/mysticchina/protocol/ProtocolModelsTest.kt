package com.fula.mysticchina.protocol

import com.fula.mysticchina.pages.firstStickyBodyOffset
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProtocolModelsTest {

    @Test
    fun `reference modes open distinct protocol layouts`() {
        assertEquals(11, LEGO_SAMPLE_MODES.size)
        val pages = LEGO_SAMPLE_MODES.map { parseProtocolResponse(JSONObject(it.protocolJson)) }
        assertEquals(listOf(19, 37, 19, 19, 19, 19, 19, 20, 19, 21, 19), pages.map { it.body.size })
        assertEquals("fixed", pages[2].headerScrollMode)
        assertEquals("linked", pages[3].headerScrollMode)
        assertTrue(pages[5].header.any { it.layout.mode == "overlay" })
        assertTrue(pages[6].header.any { it.sticky })
        assertTrue(pages[7].header.isEmpty())
        assertEquals("image", pages[8].background?.optString("type"))
        assertTrue(pages[9].body.any { it.sticky })
        val stickyOffset = firstStickyBodyOffset(pages[9].body, 360f, 24f)
        assertTrue(stickyOffset != null && stickyOffset > 48f)
        assertEquals(null, firstStickyBodyOffset(pages[8].body, 360f, 24f))
        assertTrue(pages[10].footer.isNotEmpty())
        assertTrue(pages.all { it.body.isNotEmpty() })
    }

    @Test
    fun `bundled demo parses`() {
        val page = parseProtocolResponse(JSONObject(DEMO_PROTOCOL_JSON))

        assertEquals(2, page.header.size)
        assertEquals(2, page.body.size)
        assertEquals(1, page.floating.size)
        assertEquals("linked", page.headerScrollMode)
        assertEquals("overlay", page.floating.single().layout.mode)
        assertEquals("TOP_STICKY", page.floating.single().overlayRole)
        assertEquals(0, page.pageNo)
        assertFalse(page.hasMore)
    }

    @Test
    fun `sections pagination layout and action parse`() {
        val page = parseProtocolResponse(JSONObject(fourSectionJson()))

        assertEquals(listOf("h", "b", "f"), (page.header + page.body + page.footer).map { it.dataId })
        assertEquals("x", page.floating.single().dataId)
        assertEquals(
            listOf(ProtocolSection.HEADER, ProtocolSection.BODY, ProtocolSection.FOOTER),
            (page.header + page.body + page.footer).map { it.section },
        )
        assertEquals(7, page.pageNo)
        assertTrue(page.hasMore)
        assertEquals(64f, page.header.single().layout.paddingTop)
        assertEquals(-64f, page.header.single().layout.marginTop)
        val action = assertIs<ProtocolAction.OpenPage>(page.body.single().jsonData.protocolAction())
        assertEquals("HanziPage", action.pageName)
        assertEquals("demo", action.params.optString("source"))
    }

    @Test
    fun `duplicate data id is rejected across sections`() {
        val raw = fourSectionJson().replace("\"data_id\":\"b\"", "\"data_id\":\"h\"")
        assertFailsWith<IllegalArgumentException> { parseProtocolResponse(JSONObject(raw)) }
    }

    @Test
    fun `nonzero response code is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            parseProtocolResponse(JSONObject("""{"code":500,"message":"failed"}"""))
        }
    }

    @Test
    fun `target contract rejects unpartitioned header and body overlay`() {
        assertFailsWith<IllegalArgumentException> {
            parseProtocolResponse(JSONObject(fourSectionJson().replace("\"scroll_mode\":\"linked\"", "\"scroll_mode\":\"natural\"")))
        }
        val page = parseProtocolResponse(JSONObject(fourSectionJson().replace(
            "\"data_id\":\"b\"", "\"data_id\":\"b\",\"layout_info\":{\"layout_mode\":\"overlay\"}",
        )))
        assertTrue(page.body.isEmpty())
    }

    private fun fourSectionJson() = """
        {
          "code": 0,
          "unknown_root_field": true,
          "data": {
            "module_header": {"extra_data":{"scroll_mode":"linked"},"component_list":[{
              "data_id":"h", "component_id":"mach_pro_sailor_c_channel_list_header_image_sub", "component_code":"common_page_bg_pic",
              "layout_info":{"padding_top":999,"margin_top":-999},
              "unknown_component_field":"ignored"
            }]},
            "module_body": {
              "component_list":[{
                "data_id":"b", "component_id":"mach_pro_sailor_c_feed_common_big_pic_card_v3", "component_code":"common_big_pic_shop_card_v3",
                "json_data":{"action":{"type":"open_page","page_name":"HanziPage","params":{"source":"demo"}}}
              }],
              "module_data":{"pagination":{"page_no":7,"has_more_page":true}}
            },
            "module_footer": {"component_list":[{"data_id":"f","component_id":"footer"}]},
            "module_float": {"component_list":[{"data_id":"x","component_id":"float"}]}
          }
        }
    """.trimIndent()
}
