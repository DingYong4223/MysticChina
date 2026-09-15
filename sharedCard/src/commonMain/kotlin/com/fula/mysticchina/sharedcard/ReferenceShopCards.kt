package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// FeedsShopCard/src/template: 111px square, 16px corners, 9px side insets.
internal fun ViewContainer<*, *>.FeedsShopCard(data: JSONObject, width: Float) {
    ShopFeedRow(data, width, 111f)
}

// FeedsGroceryCard/src/template: same 111px media, with grocery products below.
internal fun ViewContainer<*, *>.FeedsGroceryCard(data: JSONObject, width: Float) {
    ShopFeedRow(data, width, 111f)
    data.optJSONArray("subCards")?.let { GroceryProducts(it, width) }
}

// FeedsShopWithDishesCard/src/template: feed card followed by dish cards.
internal fun ViewContainer<*, *>.FeedsShopWithDishesCard(data: JSONObject, width: Float) {
    ShopFeedRow(data, width, 111f)
    data.optJSONArray("subCards")?.let { GroceryProducts(it, width) }
}

// SearchShopCard/src/template: 82px square and search-specific title/price row.
internal fun ViewContainer<*, *>.SearchShopCard(data: JSONObject, width: Float) {
    val sectionTitle = data.optString("shopTopText")
    if (sectionTitle.isNotEmpty()) Text {
        attr { marginLeft(9f); marginTop(8f); text(sectionTitle); fontSize(16f); fontWeightBold(); color(Color(0xFF222222)) }
    }
    ShopFeedRow(data, width, 82f)
    data.optJSONArray("subCards")?.let { GroceryProducts(it, width) }
}

private fun ViewContainer<*, *>.ShopFeedRow(data: JSONObject, width: Float, pictureSize: Float) {
    View {
        attr { width(width); paddingLeft(9f); paddingRight(9f); paddingBottom(9f); backgroundColor(Color.WHITE) }
        View {
            attr { flexDirectionRow(); backgroundColor(Color.WHITE); borderRadius(12f); overflow(true) }
            View {
                attr { size(pictureSize, pictureSize); borderRadius(16f); overflow(true); backgroundColor(Color(0xFFF2F2F2)) }
                val image = data.optString("shopMainPicUrl").ifEmpty { data.optJSONArray("pictures")?.optJSONObject(0)?.optString("url").orEmpty() }
                if (image.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(image); resizeCover() } }
                if (data.optString("logo").isNotEmpty()) Image {
                    attr { absolutePosition(top = 5f, left = 6f); size(28f, 28f); src(data.optString("logo")); resizeContain() }
                }
                val bottom = data.optJSONObject("imgBottomContent")
                if (bottom?.optString("promotionText").orEmpty().isNotEmpty()) View {
                    attr { absolutePosition(bottom = 0f, left = 0f, right = 0f); height(19f); paddingLeft(4f); backgroundColor(Color(0xB0000000)) }
                    Text { attr { text(bottom!!.optString("promotionText")); fontSize(10f); color(Color.WHITE); lines(1) } }
                }
            }
            View {
                attr { flex(1f); flexDirectionColumn(); marginLeft(8f); paddingRight(6f) }
                val name = data.field("shopName")?.optString("name").orEmpty().ifEmpty { data.optString("title") }
                Text {
                    attr { text(name); fontSize(16f); fontWeightSemiBold(); color(Color(0xFF222222)); lines(2); textOverFlowTail() }
                }
                val score = data.field("shopScore")?.optJSONObject("shopScoreInfo")?.optString("score").orEmpty()
                val delivery = data.field("deliveryTime")?.optString("deliveryTimeDesc").orEmpty()
                val distance = data.field("distance")?.optJSONObject("distance")?.optString("desc").orEmpty()
                View {
                    attr { marginTop(5f); flexDirectionRow(); alignItemsCenter() }
                    if (score.isNotEmpty()) Text { attr { marginRight(6f); text("★ $score"); fontSize(12f); color(Color(0xFFFFA000)) } }
                    Text {
                        attr { flex(1f); text(listOf(delivery, distance).filter { it.isNotEmpty() }.joinToString(" · ")); fontSize(12f); color(Color(0xFF555555)); lines(1) }
                    }
                }
                val category = data.field("shopCategory")?.optString("shopCategoryName").orEmpty()
                if (category.isNotEmpty()) Text { attr { marginTop(5f); text(category); fontSize(12f); color(Color(0xFF808080)); lines(1) } }
                val tags = data.field("dynamicTags")?.optJSONObject("promotionTags")?.optJSONObject("discountTag")?.optJSONArray("tags")
                if (tags != null && tags.length() > 0) View {
                    attr { marginTop(6f); flexDirectionRow(); alignItemsCenter() }
                    for (i in 0 until minOf(tags.length(), 2)) tags.optJSONObject(i)?.let { tag ->
                        val tagText = tag.optJSONArray("subTags")?.optJSONObject(0)?.optString("text").orEmpty()
                        if (tagText.isNotEmpty()) Text {
                            attr { marginRight(4f); marginLeft(4f); text(tagText); fontSize(11f); color(Color(0xFFDE4A2F)); lines(1) }
                        }
                    }
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.GroceryProducts(items: JSONArray, width: Float) {
    if (items.length() == 0) return
    val size = (width - 38f) / 3f
    Scroller {
        attr { height(size + 50f); flexDirectionRow(); paddingLeft(9f); backgroundColor(Color.WHITE) }
        for (i in 0 until items.length()) items.optJSONObject(i)?.let { item ->
            View {
                attr { width(size); marginRight(10f); flexDirectionColumn() }
                View {
                    attr { width(size); height(size); borderRadius(10f); overflow(true) }
                    val image = item.optString("imageUrl").ifEmpty { item.optJSONArray("pictures")?.optJSONObject(0)?.optString("url").orEmpty() }
                    if (image.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(image); resizeCover() } }
                }
                Text { attr { text(item.optString("title")); fontSize(12f); color(Color(0xFF333333)); lines(1); textOverFlowTail() } }
                if (item.optString("price").isNotEmpty()) Text { attr { text(item.optString("price")); fontSize(12f); color(Color(0xFFFF8200)) } }
            }
        }
        View { attr { width(16f) } }
    }
}

private fun JSONObject.field(type: String): JSONObject? {
    val content = optJSONArray("content") ?: return null
    for (i in 0 until content.length()) {
        val row = content.optJSONArray(i) ?: continue
        for (j in 0 until row.length()) {
            val field = row.optJSONObject(j) ?: continue
            if (field.optString("type") == type) return field.optJSONObject("data")
        }
    }
    return null
}
