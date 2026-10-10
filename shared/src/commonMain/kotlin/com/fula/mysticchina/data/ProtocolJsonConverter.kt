package com.fula.mysticchina.data

import com.fula.protocol.pb.v1.FeedCardPayload
import com.fula.protocol.pb.v1.HeaderImagePayload
import com.fula.protocol.pb.v1.HeaderScrollMode
import com.fula.protocol.pb.v1.NavigationBarPayload
import com.fula.protocol.pb.v1.OverlayRole
import com.fula.protocol.pb.v1.OverlayImagePayload
import com.fula.protocol.pb.v1.TextBarPayload
import com.fula.protocol.pb.v1.RawCardPayload
import com.fula.protocol.pb.v1.ProtocolComponent as PbProtocolComponent
import com.fula.protocol.pb.v1.ProtocolLayout as PbProtocolLayout
import com.fula.protocol.pb.v1.ProtocolPage as PbProtocolPage
import com.fula.protocol.pb.v1.ProtocolResponse as PbProtocolResponse
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

internal fun protocolJsonToPb(json: String): PbProtocolResponse {
    val root = JSONObject(json)
    val code = root.optInt("code", Int.MIN_VALUE)
    require(code != Int.MIN_VALUE) { "Protocol is missing code" }
    val data = root.optJSONObject("data")
    if (data == null) return PbProtocolResponse(code = code, message = root.optString("message"))

    val bodyModule = data.optJSONObject("module_body")
    val pagination = bodyModule?.optJSONObject("module_data")?.optJSONObject("pagination")
    val pageNo = pagination?.optInt("page_no", 0) ?: 0
    val headerModule = data.optJSONObject("module_header")
    val extraData = data.optJSONObject("extra_data")
    val background = extraData?.optJSONObject("background")
    val enabledPullrefresh = if (extraData?.has("enabled_pullrefresh") == true) {
        val value = extraData.opt("enabled_pullrefresh")
        require(value is Boolean) { "enabled_pullrefresh must be a boolean" }
        value
    } else null
    val scrollMode = when (headerModule?.optJSONObject("extra_data")?.optString("scroll_mode")) {
        "fixed" -> HeaderScrollMode.HEADER_SCROLL_MODE_FIXED
        "linked" -> HeaderScrollMode.HEADER_SCROLL_MODE_LINKED
        else -> HeaderScrollMode.HEADER_SCROLL_MODE_UNSPECIFIED
    }
    return PbProtocolResponse(
        code = code,
        message = root.optString("message", root.optString("msg")),
        page = PbProtocolPage(
            header_ = convertSection(headerModule, "header", pageNo),
            body = convertSection(bodyModule, "body", pageNo),
            footer = convertSection(data.optJSONObject("module_footer"), "footer", pageNo),
            floating = convertSection(data.optJSONObject("module_float"), "float", pageNo),
            headerScrollMode = scrollMode,
            headerMinContentHeight = (headerModule?.optJSONObject("extra_data")
                ?.optInt("min_content_height_dp", 0) ?: 0).toFloat(),
            enabled_pullrefresh = enabledPullrefresh,
            pageNo = pageNo,
            hasMore = pagination?.optBoolean("has_more_page", false) ?: false,
            backgroundColor = if (background?.optString("type") == "color") background.optString("color") else "",
            backgroundImageUrl = if (background?.optString("type") == "image") background.optString("image_url") else "",
        ),
    )
}

private fun convertSection(module: JSONObject?, section: String, pageNo: Int): List<PbProtocolComponent> {
    val items = module?.optJSONArray("component_list") ?: return emptyList()
    return buildList {
        for (index in 0 until items.length()) {
            val item = requireNotNull(items.optJSONObject(index)) { "$section component[$index] must be an object" }
            val componentId = item.optString("component_id").trim()
            require(componentId.isNotEmpty()) { "$section component[$index] is missing component_id" }
            val properties = item.optJSONObject("component_properties")
            val payload = item.optJSONObject("json_data") ?: JSONObject()
            val layout = item.optJSONObject("layout_info")
            val common = PbProtocolComponent(
                instanceId = item.optString("data_id").trim().ifEmpty { "$section:$pageNo:$index:$componentId" },
                componentId = componentId,
                componentCode = item.optString("component_code"),
                sticky = properties?.optInt("stick_type", 0) == 1,
                fitsSystemWindows = properties?.optBoolean("fitsSystemWindows", false) == true,
                stretch2SystemWindows = properties?.optBoolean("stretch2SystemWindows", false) == true,
                overlayRole = when (properties?.optString("overlay_role")) {
                    "TOP_STICKY" -> OverlayRole.OVERLAY_ROLE_TOP_STICKY
                    else -> OverlayRole.OVERLAY_ROLE_UNSPECIFIED
                },
                layout = layout?.let {
                    PbProtocolLayout(
                        overlay = it.optString("layout_mode") == "overlay",
                        marginTop = it.optInt("margin_top", 0).coerceIn(-64, 64).toFloat(),
                        marginRight = it.optInt("margin_right", 0).coerceIn(-64, 64).toFloat(),
                        marginBottom = it.optInt("margin_bottom", 0).coerceIn(-64, 64).toFloat(),
                        marginLeft = it.optInt("margin_left", 0).coerceIn(-64, 64).toFloat(),
                    )
                },
            )
            add(when (componentId) {
                "mach_pro_sailor_c_channel_list_header_image_sub" -> {
                    common.copy(
                        headerImage = HeaderImagePayload(
                            picUrl = payload.requiredString("picUrl", section, index),
                            aspectRatio = payload.optString("aspectRatio", "16:9"),
                        )
                    )
                }
                "fula_home_page_feeds_card_golden" -> {
                    require(section == "body") { "$section component[$index] feed card is in the wrong section" }
                    common.copy(
                        feedCard = FeedCardPayload(
                            title = payload.requiredString("title", section, index),
                            shopMainPicUrl = payload.requiredString("shopMainPicUrl", section, index),
                            logo = payload.optString("logo"),
                        )
                    )
                }
                "fula_navigation_bar" -> {
                    if (payload.optBoolean("center_title", false)) common.copy(rawCard = RawCardPayload(jsonData = payload.toString())) else common.copy(
                        navigationBar = NavigationBarPayload(
                            title = payload.optString("title"),
                        )
                    )
                }
                "native_sailor_c_sample_header_text", "native_sailor_c_sample_tabs" -> {
                    val tabs = componentId == "native_sailor_c_sample_tabs"
                    val labels = payload.optJSONArray(if (tabs) "tabs" else "texts")
                    common.copy(textBar = TextBarPayload(
                        labels = (0 until (labels?.length() ?: 0)).map { labels!!.optString(it).orEmpty() },
                        tabs = tabs,
                    ))
                }
                "native_sailor_c_sample_header_overlay" -> common.copy(
                    overlayImage = OverlayImagePayload(asset = "zgj_swing.gif")
                )
                else -> if (componentId.startsWith("fula_") ||
                    componentId == "big_pic_shop_card" ||
                    componentId == "native_sailor_c_home_page_header_kingkong_new_sub") {
                    common.copy(rawCard = RawCardPayload(jsonData = payload.toString()))
                } else error("$section component[$index] unsupported component_id=$componentId")
            })
        }
    }
}

private fun JSONObject.requiredString(name: String, section: String, index: Int): String =
    optString(name).trim().also { require(it.isNotEmpty()) { "$section component[$index] is missing $name" } }
