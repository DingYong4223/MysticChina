package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// HeaderBrandListCard/src/template dispatches by extraData.brandStyle.
internal fun ViewContainer<*, *>.HeaderBrandListCard(data: JSONObject, width: Float) {
    val style = data.optJSONObject("extraData")?.optInt("brandStyle", 0) ?: 0
    HeaderCard(data, width) {
        if (style == 2 || style == 3) BannerGrid(data.optJSONArray("shopList"), width, 100f, style)
        else BannerGrid(data.optJSONArray("shopList"), width, 92f, 2)
    }
}

// HeaderBrandListCardV2 is a separate v4 template with a compact logo row.
internal fun ViewContainer<*, *>.HeaderBrandListCardV2(data: JSONObject, width: Float) {
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("shopList"), width, 78f, 2) }
}

// ShopBanner/src/template: title/image background and horizontally scrolling shops.
internal fun ViewContainer<*, *>.ShopBanner(data: JSONObject, width: Float) {
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("shopList"), width, 142f, 1) }
}

// ShopBannerV2 uses a wider shop card and shorter poster than its predecessor.
internal fun ViewContainer<*, *>.ShopBannerV2(data: JSONObject, width: Float) {
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("shopList"), width, 170f, 1) }
}

// DishEntrance/src/template: product rather than shop content, over banner image.
internal fun ViewContainer<*, *>.DishEntrance(data: JSONObject, width: Float) {
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("productList"), width, 128f, 1) }
}

// DishEntranceV2/src/template: two larger dishes visible per viewport.
internal fun ViewContainer<*, *>.DishEntranceV2(data: JSONObject, width: Float) {
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("productList"), width, (width - 42f) / 2f, 1) }
}

// RecentOrder/src/template and RecentOrderV2/src/template use the same source
// component with different row counts; the second version passes rows=1.
internal fun ViewContainer<*, *>.RecentOrder(data: JSONObject, width: Float) {
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("shopList"), width, (width - 50f) / 4f, 2) }
}

internal fun ViewContainer<*, *>.RecentOrderV2(data: JSONObject, width: Float) {
    val large = data.optJSONObject("extraData")?.optInt("brandStyle", 0) == 2
    HeaderCard(data, width) { BannerGrid(data.optJSONArray("shopList"), width, (width - if (large) 60f else 50f) / if (large) 5f else 4f, 1) }
}

// HeaderGatheringCard/src/template combines title, category tiles and zones.
internal fun ViewContainer<*, *>.HeaderGatheringCard(data: JSONObject, width: Float) {
    HeaderCard(data, width) {
        val tiles = data.optJSONArray("tileList")
        if (tiles != null && tiles.length() > 0) Scroller {
            attr { height(100f); flexDirectionRow(); paddingLeft(9f) }
            for (i in 0 until tiles.length()) tiles.optJSONObject(i)?.let { tile ->
                View {
                    attr { marginRight(8f); width(88f); height(88f); borderRadius(10f); overflow(true) }
                    val img = tile.optString("cardImg")
                    if (img.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(img); resizeCover() } }
                }
            }
            View { attr { width(16f) } }
        }
        data.optJSONArray("cardZoneList")?.let { zones ->
            for (i in 0 until zones.length()) zones.optJSONObject(i)?.let { zone ->
                HeaderCard(zone, width) {
                    BannerGrid(zone.optJSONArray("shopList") ?: zone.optJSONArray("productList"), width, 142f, 1)
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.HeaderCard(data: JSONObject, width: Float, children: ViewContainer<*, *>.() -> Unit) {
    View {
        attr { width(width); paddingBottom(if (data.optString("bgImg").isNotEmpty()) 12f else 0f); flexDirectionColumn(); backgroundColor(Color.WHITE) }
        if (data.optString("bgImg").isNotEmpty()) Image {
            attr { absolutePositionAllZero(); src(data.optString("bgImg")); resizeCover() }
        }
        val title = data.optJSONObject("cardTitle")
        if (title != null) View {
            attr { height(48f); paddingLeft(9f); paddingRight(12f); flexDirectionRow(); alignItemsCenter() }
            if (title.optString("titleImg").isNotEmpty()) Image {
                attr { width((width * 0.6f).coerceAtMost(250f)); height(35f); src(title.optString("titleImg")); resizeContain() }
            } else Text {
                attr {
                    flex(1f); text(title.optString("titleText")); fontWeightSemiBold(); fontSize(17f)
                    color(title.optString("titleColor").headerColor()); lines(1)
                }
            }
            if (title.optString("arrowIcon").isNotEmpty()) Image {
                attr { size(16f, 16f); src(title.optString("arrowIcon")); resizeContain() }
            }
        }
        children()
    }
}

private fun ViewContainer<*, *>.BannerGrid(cards: JSONArray?, width: Float, itemWidth: Float, rows: Int) {
    if (cards == null || cards.length() == 0) return
    val cellHeight = itemWidth + 49f
    Scroller {
        attr { height(cellHeight * rows); paddingLeft(9f); flexDirectionRow(); showScrollerIndicator(false) }
        for (column in 0 until (cards.length() + rows - 1) / rows) View {
            attr { width(itemWidth); height(cellHeight * rows); marginRight(9f); flexDirectionColumn() }
            for (row in 0 until rows) cards.optJSONObject(column * rows + row)?.let { card ->
                View {
                    attr { width(itemWidth); height(cellHeight); flexDirectionColumn(); backgroundColor(Color.WHITE) }
                    val image = card.optJSONArray("pictures")?.optJSONObject(0)?.optString("url").orEmpty()
                        .ifEmpty { card.optString("shopMainPicUrl") }.ifEmpty { card.optString("logo") }
                    View {
                        attr { width(itemWidth); height(itemWidth); borderRadius(12f); overflow(true); backgroundColor(Color(0xFFF3F3F3)) }
                        if (image.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(image); resizeCover() } }
                    }
                    val label = card.optString("title").ifEmpty { card.optString("shopName") }
                    if (label.isNotEmpty()) Text {
                        attr { text(label); fontSize(12f); color(Color(0xFF222222)); lines(2); textOverFlowTail() }
                    }
                }
            }
        }
        View { attr { width(16f) } }
    }
}

private fun String.headerColor(): Color = try {
    val hex = removePrefix("#")
    if (hex.length == 6) Color(0xFF000000L or hex.toLong(16)) else Color(0xFF222222)
} catch (_: NumberFormatException) { Color(0xFF222222) }
