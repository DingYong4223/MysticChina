package com.fula.mysticchina.protocol

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ProtocolModelsTest {

    @Test
    fun `bundled demo parses`() {
        val page = parseProtocolResponse(JSONObject(DEMO_PROTOCOL_JSON))

        assertEquals(3, page.header.size)
        assertEquals(2, page.body.size)
        assertEquals(5, page.flow.size)
        assertEquals(0, page.pageNo)
        assertFalse(page.hasMore)
    }

    @Test
    fun `sections pagination layout and action parse`() {
        val page = parseProtocolResponse(JSONObject(fourSectionJson()))

        assertEquals(listOf("h", "b", "f"), page.flow.map { it.dataId })
        assertEquals("x", page.floating.single().dataId)
        assertEquals(
            listOf(ProtocolSection.HEADER, ProtocolSection.BODY, ProtocolSection.FOOTER),
            page.flow.map { it.section },
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

    private fun fourSectionJson() = """
        {
          "code": 0,
          "unknown_root_field": true,
          "data": {
            "module_header": {"component_list":[{
              "data_id":"h", "component_code":"common_page_guide_bar",
              "layout_info":{"padding_top":999,"margin_top":-999},
              "unknown_component_field":"ignored"
            }]},
            "module_body": {
              "component_list":[{
                "data_id":"b", "component_code":"common_big_pic_shop_card_v3",
                "json_data":{"action":{"type":"open_page","page_name":"HanziPage","params":{"source":"demo"}}}
              }],
              "module_data":{"pagination":{"page_no":7,"has_more_page":true}}
            },
            "module_footer": {"component_list":[{"data_id":"f","component_code":"footer"}]},
            "module_float": {"component_list":[{"data_id":"x","component_code":"float"}]}
          }
        }
    """.trimIndent()
}
