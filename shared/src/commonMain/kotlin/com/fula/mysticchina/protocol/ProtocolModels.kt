package com.fula.mysticchina.protocol

import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

internal const val PROTOCOL_PAGE_NAME = "ProtocolPage"
internal const val PARAM_PROTOCOL_URL = "protocolUrl"
internal const val PARAM_PROTOCOL_JSON = "protocolJson"

internal enum class ProtocolSection { HEADER, BODY, FOOTER, FLOAT }

internal data class ProtocolLayout(
    val marginTop: Float = 0f,
    val marginBottom: Float = 0f,
    val marginLeft: Float = 0f,
    val marginRight: Float = 0f,
    val paddingTop: Float = 0f,
    val paddingBottom: Float = 0f,
    val paddingLeft: Float = 0f,
    val paddingRight: Float = 0f,
)

internal data class ProtocolComponent(
    val dataId: String,
    val componentCode: String,
    val jsonData: JSONObject,
    val layout: ProtocolLayout,
    val section: ProtocolSection,
    val sticky: Boolean,
)

internal data class ProtocolPageData(
    val header: List<ProtocolComponent>,
    val body: List<ProtocolComponent>,
    val footer: List<ProtocolComponent>,
    val floating: List<ProtocolComponent>,
    val pageNo: Int,
    val hasMore: Boolean,
) {
    val flow: List<ProtocolComponent> get() = header + body + footer
    val isEmpty: Boolean get() = flow.isEmpty() && floating.isEmpty()
}

internal sealed class ProtocolAction {
    data object Back : ProtocolAction()
    data object Refresh : ProtocolAction()
    data class Filter(val id: String) : ProtocolAction()
    data class OpenPage(val pageName: String, val params: JSONObject) : ProtocolAction()
}

internal fun JSONObject.protocolAction(): ProtocolAction? {
    val action = optJSONObject("action") ?: return null
    return when (action.optString("type")) {
        "back" -> ProtocolAction.Back
        "refresh" -> ProtocolAction.Refresh
        "open_page" -> ProtocolAction.OpenPage(
            pageName = action.optString("page_name"),
            params = action.optJSONObject("params") ?: JSONObject(),
        )
        else -> null
    }
}

internal fun parseProtocolResponse(root: JSONObject): ProtocolPageData {
    val code = root.optInt("code", Int.MIN_VALUE)
    require(code != Int.MIN_VALUE) { "Protocol response is missing code" }
    require(code == 0) { root.optString("message", root.optString("msg", "Protocol request failed: $code")) }

    val data = root.optJSONObject("data") ?: return ProtocolPageData(
        emptyList(), emptyList(), emptyList(), emptyList(), 0, false,
    )
    val seenIds = mutableSetOf<String>()
    val header = parseSection(data.optJSONObject("module_header"), ProtocolSection.HEADER, seenIds)
    val bodyModule = data.optJSONObject("module_body")
    val body = parseSection(bodyModule, ProtocolSection.BODY, seenIds)
    val footer = parseSection(data.optJSONObject("module_footer"), ProtocolSection.FOOTER, seenIds)
    val floating = parseSection(data.optJSONObject("module_float"), ProtocolSection.FLOAT, seenIds)
    val pagination = bodyModule?.optJSONObject("module_data")?.optJSONObject("pagination")
    return ProtocolPageData(
        header = header,
        body = body,
        footer = footer,
        floating = floating,
        pageNo = pagination?.optInt("page_no", 0) ?: 0,
        hasMore = pagination?.optBoolean("has_more_page", false) ?: false,
    )
}

private fun parseSection(
    module: JSONObject?,
    section: ProtocolSection,
    seenIds: MutableSet<String>,
): List<ProtocolComponent> {
    val items = module?.optJSONArray("component_list") ?: return emptyList()
    return buildList {
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            val dataId = item.optString("data_id").trim()
            val componentCode = item.optString("component_code").trim()
            require(dataId.isNotEmpty()) { "$section component[$index] is missing data_id" }
            require(seenIds.add(dataId)) { "Duplicate protocol data_id: $dataId" }
            require(componentCode.isNotEmpty()) { "$section component[$index] is missing component_code" }
            val properties = item.optJSONObject("component_properties")
            add(
                ProtocolComponent(
                    dataId = dataId,
                    componentCode = componentCode,
                    jsonData = item.optJSONObject("json_data") ?: JSONObject(),
                    layout = parseLayout(item.optJSONObject("layout_info")),
                    section = section,
                    sticky = properties?.optInt("stick_type", 0) == 1,
                )
            )
        }
    }
}

private fun parseLayout(json: JSONObject?): ProtocolLayout {
    fun margin(name: String) = (json?.optInt(name, 0) ?: 0).coerceIn(-64, 64).toFloat()
    fun padding(name: String) = (json?.optInt(name, 0) ?: 0).coerceIn(0, 64).toFloat()
    return ProtocolLayout(
        marginTop = margin("margin_top"),
        marginBottom = margin("margin_bottom"),
        marginLeft = margin("margin_left"),
        marginRight = margin("margin_right"),
        paddingTop = padding("padding_top"),
        paddingBottom = padding("padding_bottom"),
        paddingLeft = padding("padding_left"),
        paddingRight = padding("padding_right"),
    )
}

internal fun protocolPageParams(protocolJson: String): String = JSONObject().apply {
    put(PARAM_PROTOCOL_JSON, protocolJson)
}.toString()

internal fun JSONArray.objects(): List<JSONObject> = buildList {
    for (index in 0 until length()) optJSONObject(index)?.let(::add)
}

/** Small bundled contract used by the home entry and parser acceptance tests. */
internal val DEMO_PROTOCOL_JSON = """
    {
      "code": 0,
      "message": "success",
      "data": {
        "module_header": {
          "component_list": [
            {
              "data_id": "demo-guide",
              "component_code": "common_page_guide_bar",
              "component_properties": { "stick_type": 1 },
              "json_data": { "title": "中华文化专题", "titleColor": "#222222", "stickBgColor": "#ffffff" }
            },
            {
              "data_id": "demo-picture",
              "component_code": "common_page_bg_pic",
              "json_data": { "aspectRatio": "16:7" }
            },
            {
              "data_id": "demo-filter",
              "component_code": "pickup_filter",
              "component_properties": { "stick_type": 1 },
              "json_data": {
                "filterOptions": [
                  { "filterOptionText": "推荐", "filterOptionId": "recommend" },
                  { "filterOptionText": "文字", "filterOptionId": "writing" },
                  { "filterOptionText": "传统文化", "filterOptionId": "culture" }
                ]
              }
            }
          ]
        },
        "module_body": {
          "component_list": [
            {
              "data_id": "demo-hanzi",
              "component_code": "common_big_pic_shop_card_v3",
              "json_data": {
                "title": "汉字书写挑战",
                "subtitle": "从一笔一画开始感受汉字之美",
                "action": { "type": "open_page", "page_name": "HanziPage", "params": {} }
              }
            },
            {
              "data_id": "demo-festival",
              "component_code": "common_big_pic_shop_card_v3",
              "json_data": { "title": "传统节日", "subtitle": "了解节日背后的历史与礼俗" }
            }
          ],
          "module_data": { "pagination": { "page_no": 0, "has_more_page": false } }
        }
      }
    }
""".trimIndent()
