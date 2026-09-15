package com.fula.mysticchina.components.protocol

import com.fula.mysticchina.pages.ProtocolPage
import com.fula.mysticchina.protocol.ProtocolAction
import com.fula.mysticchina.protocol.ProtocolComponent
import com.fula.mysticchina.protocol.objects
import com.fula.mysticchina.protocol.protocolAction
import com.fula.mysticchina.sharedcard.BigPicShopCard
import com.fula.mysticchina.sharedcard.ReferenceFlexboxCard
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.attr.AccessibilityRole
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

internal fun ViewContainer<*, *>.ProtocolComponentView(
    component: ProtocolComponent,
    page: ProtocolPage,
    bodyIndex: Int = -1,
) {
    View {
        attr {
            flexDirectionColumn()
            marginTop(component.layout.marginTop)
            marginBottom(component.layout.marginBottom)
            marginLeft(component.layout.marginLeft)
            marginRight(component.layout.marginRight)
            paddingTop(component.layout.paddingTop)
            paddingBottom(component.layout.paddingBottom)
            paddingLeft(component.layout.paddingLeft)
            paddingRight(component.layout.paddingRight)
        }
        when (component.componentId to component.renderType) {
            "mach_pro_sailor_c_channel_list_nav_sub" to "custom", "common_page_guide_bar" to "custom" -> GuideBar(component.jsonData, page)
            "mach_pro_sailor_c_channel_list_header_image_sub" to "custom", "common_page_bg_pic" to "custom",
            "mach_pro_sailor_c_channel_list_header_image_sub" to "Native" -> BackgroundPicture(component.jsonData, page)
            "mach_pro_sailor_c_common_filter_bar_sub" to "sub", "pickup_filter" to "custom" -> FilterBar(component.jsonData, page)
            "mach_pro_sailor_c_feed_common_big_pic_card_v3" to "custom" -> {
                if (bodyIndex in 0..4 && component.jsonData.optJSONArray("pictures") != null)
                    BigPicShopCard(component.jsonData, page.pagerData.pageViewWidth)
                else ContentCard(component.jsonData, page)
            }
            "common_big_pic_shop_card_v3" to "custom" -> ContentCard(component.jsonData, page)
            "kflexbox_sailor_c_navigation_bar" to "Flexbox" -> SampleNavigation(component.jsonData, page)
            "native_sailor_c_sample_header_text" to "Native",
            "native_sailor_c_sample_header_overlay" to "Native",
            "native_sailor_c_sample_navigation_placeholder" to "Native" -> SampleTextBar(component.jsonData)
            "native_sailor_c_sample_tabs" to "Native" -> SampleTextBar(component.jsonData)
            else -> if (component.renderType in listOf("Flexbox", "ADFlexbox") &&
                ReferenceFlexboxCard(component.componentId, component.jsonData, page.pagerData.pageViewWidth)) Unit
            else if (component.renderType == "Flexbox" && component.jsonData.optString("title").isNotEmpty())
                ContentCard(component.jsonData, page)
            else UnsupportedComponent("${component.componentId}/${component.renderType}")
        }
    }
}

internal fun estimatedComponentHeight(
    component: ProtocolComponent,
    pageWidth: Float,
    statusBarHeight: Float,
): Float {
    val contentHeight = when (component.componentCode) {
        "common_page_guide_bar" -> 52f + statusBarHeight
        "common_page_bg_pic" -> pageWidth / parseAspectRatio(component.jsonData.optString("aspectRatio", "16:9"))
        "pickup_filter" -> 58f
        "common_big_pic_shop_card_v3", "common_shop_card_3.0" -> pageWidth * 0.56f + 118f
        else -> 52f
    }
    return component.layout.marginTop + component.layout.paddingTop + contentHeight +
        component.layout.paddingBottom + component.layout.marginBottom
}

private fun ViewContainer<*, *>.GuideBar(data: JSONObject, page: ProtocolPage) {
    View {
        attr {
            height(52f + page.pagerData.statusBarHeight)
            paddingTop(page.pagerData.statusBarHeight)
            paddingLeft(8f)
            paddingRight(16f)
            flexDirectionRow()
            alignItemsCenter()
                backgroundColor(parseColor(
                    data.optString(if (page.scrollOffset > 0f) "stickBgColor" else "unStickBgColor"),
                    Color.WHITE,
                ))
        }
        View {
            attr {
                size(44f, 44f)
                allCenter()
                accessibility("返回")
                accessibilityRole(AccessibilityRole.BUTTON)
            }
            event { click { page.dispatch(ProtocolAction.Back) } }
            val backImage = data.optString("backImageUrl")
            if (backImage.isNotEmpty()) {
                Image { attr { size(24f, 24f); src(backImage); resizeContain() } }
            } else {
                Text { attr { text("‹"); fontSize(34f); color(Color(0xFF222222)) } }
            }
        }
        Text {
            attr {
                flex(1f)
                text(data.optString("title", "专题"))
                color(parseColor(data.optString("titleColor"), Color(0xFF222222)))
                fontSize(18f)
                fontWeightSemiBold()
                lines(1)
                textOverFlowTail()
            }
        }
    }
}

private fun ViewContainer<*, *>.BackgroundPicture(data: JSONObject, page: ProtocolPage) {
    val height = page.pagerData.pageViewWidth / parseAspectRatio(data.optString("aspectRatio", "16:9"))
    val url = data.optString("picUrl")
    View {
        attr {
            height(height)
            backgroundColor(Color(0xFFF2E6D5))
            overflow(true)
        }
        if (url.isNotEmpty()) {
            Image { attr { absolutePosition(0f, 0f, 0f, 0f); src(url); resizeCover() } }
        } else {
            View {
                attr { flex(1f); allCenter(); flexDirectionColumn() }
                Text { attr { text("🏮"); fontSize(42f); marginBottom(8f) } }
                Text { attr { text("探索中华文化之美"); fontSize(16f); color(Color(0xFF6B4930)) } }
            }
        }
    }
}

private fun ViewContainer<*, *>.FilterBar(data: JSONObject, page: ProtocolPage) {
    val options = data.optJSONArray("filterOptions")?.objects().orEmpty()
    Scroller {
        attr {
            height(58f)
            flexDirectionRow()
            alignItemsCenter()
            paddingLeft(16f)
            backgroundColor(Color.WHITE)
        }
        options.forEach { option ->
            val id = option.optString("filterOptionId", option.optString("filterOptionText"))
            val label = option.optString("filterOptionText", "筛选")
            View {
                attr {
                    height(34f)
                    paddingLeft(14f)
                    paddingRight(14f)
                    marginRight(8f)
                    borderRadius(17f)
                    allCenter()
                    accessibility(label)
                    accessibilityRole(AccessibilityRole.BUTTON)
                    backgroundColor(
                        if (page.selectedFilterId == id) Color(0xFFCC1111) else Color(0xFFF3F3F3)
                    )
                }
                event { click { page.dispatch(ProtocolAction.Filter(id)) } }
                Text {
                    attr {
                        text(label)
                        fontSize(13f)
                        color(if (page.selectedFilterId == id) Color.WHITE else Color(0xFF333333))
                    }
                }
            }
        }
        View { attr { width(8f) } }
    }
}

private fun ViewContainer<*, *>.ContentCard(data: JSONObject, page: ProtocolPage) {
    val picture = data.optString("imageUrl", data.optString("shopMainPicUrl"))
    val title = data.optString("title").ifEmpty {
        data.findContentData("shopName")?.optString("name").orEmpty()
    }.ifEmpty { "文化内容" }
    val subtitle = data.optString("subtitle").ifEmpty {
        listOfNotNull(
            data.findContentData("shopScore")?.optJSONObject("shopScoreInfo")?.optString("score")
                ?.takeIf(String::isNotEmpty)?.let { "★ $it" },
            data.findContentData("deliveryTime")?.optString("deliveryTimeDesc")?.takeIf(String::isNotEmpty),
            data.findContentData("distance")?.optJSONObject("distance")?.optString("desc")
                ?.takeIf(String::isNotEmpty),
        ).joinToString("  ·  ")
    }
    View {
        attr {
            marginLeft(16f)
            marginRight(16f)
            marginTop(12f)
            backgroundColor(Color.WHITE)
            borderRadius(12f)
            overflow(true)
            flexDirectionColumn()
            accessibility(title)
            data.protocolAction()?.let { accessibilityRole(AccessibilityRole.BUTTON) }
        }
        data.protocolAction()?.let { action -> event { click { page.dispatch(action) } } }
        View {
            attr {
                height(page.pagerData.pageViewWidth * 0.56f)
                backgroundColor(Color(0xFFF2E6D5))
                overflow(true)
            }
            if (picture.isNotEmpty()) {
                Image { attr { absolutePosition(0f, 0f, 0f, 0f); src(picture); resizeCover() } }
            } else {
                Text { attr { text("🏯"); fontSize(48f); textAlignCenter(); marginTop(44f) } }
            }
        }
        View {
            attr { padding(all = 14f); flexDirectionColumn() }
            Text {
                attr {
                    text(title)
                    fontSize(17f)
                    fontWeightSemiBold()
                    color(Color(0xFF222222))
                    lines(2)
                    textOverFlowTail()
                }
            }
            if (subtitle.isNotEmpty()) {
                Text {
                    attr {
                        marginTop(8f)
                        text(subtitle)
                        fontSize(13f)
                        color(Color(0xFF666666))
                        lines(2)
                        textOverFlowTail()
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.SampleNavigation(data: JSONObject, page: ProtocolPage) {
    View {
        attr {
            height(52f + page.pagerData.statusBarHeight)
            paddingTop(page.pagerData.statusBarHeight)
            paddingLeft(12f)
            backgroundColor(Color.WHITE)
            flexDirectionRow()
            alignItemsCenter()
        }
        View {
            attr { size(40f, 40f); allCenter(); accessibility("返回") }
            event { click { page.dispatch(ProtocolAction.Back) } }
            Text { attr { text("‹"); fontSize(30f); color(Color.BLACK) } }
        }
        Text { attr { text(data.optString("title", "LEGO 二级页")); fontSize(17f); color(Color.BLACK); fontWeightSemiBold(); flex(1f) } }
    }
}

private fun ViewContainer<*, *>.SampleTextBar(data: JSONObject) {
    val labels = data.optJSONArray("texts") ?: data.optJSONArray("tabs")
    View {
        attr { height(52f); backgroundColor(Color.WHITE); flexDirectionRow(); alignItemsCenter(); paddingLeft(12f) }
        if (labels != null) {
            for (i in 0 until labels.length()) {
                Text { attr { text(labels.optString(i).orEmpty()); fontSize(14f); color(Color.BLACK); marginRight(18f) } }
            }
        } else {
            Text { attr { text(data.optString("title", "专题")); fontSize(14f); color(Color.BLACK) } }
        }
    }
}

private fun ViewContainer<*, *>.SampleBanner(data: JSONObject) {
    val url = data.optString("imageUrl")
    View {
        attr { height(120f); marginLeft(16f); marginRight(16f); backgroundColor(Color(0xFFF2F2F2)) }
        if (url.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(url); resizeCover() } }
    }
}

private fun ViewContainer<*, *>.UnsupportedComponent(code: String) {
    View {
        attr {
            height(52f)
            margin(all = 12f)
            padding(all = 12f)
            borderRadius(8f)
            backgroundColor(Color(0xFFFFF3E0))
        }
        Text { attr { text("暂不支持组件：$code"); fontSize(12f); color(Color(0xFF9A5B00)) } }
    }
}

private fun JSONObject.findContentData(type: String): JSONObject? {
    val rows = optJSONArray("content") ?: return null
    for (rowIndex in 0 until rows.length()) {
        val row = rows.optJSONArray(rowIndex) ?: continue
        for (itemIndex in 0 until row.length()) {
            val item = row.optJSONObject(itemIndex) ?: continue
            if (item.optString("type") == type) return item.optJSONObject("data")
        }
    }
    return null
}

private fun parseAspectRatio(value: String): Float {
    val parts = value.split(':')
    val width = parts.getOrNull(0)?.toFloatOrNull() ?: 16f
    val height = parts.getOrNull(1)?.toFloatOrNull() ?: 9f
    return if (width > 0f && height > 0f) width / height else 16f / 9f
}

private fun parseColor(value: String, fallback: Color): Color {
    if (value == "transparent") return Color.TRANSPARENT
    return try {
        val hex = value.removePrefix("#")
        val argb = when (hex.length) {
            6 -> 0xFF000000L or hex.toLong(16)
            8 -> hex.toLong(16)
            else -> return fallback
        }
        Color(argb)
    } catch (_: Throwable) {
        fallback
    }
}
