package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject

internal data class ShopTag(val text: String, val background: String, val color: String)

internal data class BigPicShopCardData(
    val title: String,
    val imageUrl: String,
    val imageRatio: Float,
    val logoUrl: String,
    val badgeUrl: String,
    val badgeWidth: Float,
    val badgeHeight: Float,
    val bottomIconUrl: String,
    val bottomText: String,
    val score: String,
    val fee: String,
    val deliveryTime: String,
    val distance: String,
    val category: String,
    val tags: List<ShopTag>,
) {
    companion object {
        fun from(data: JSONObject): BigPicShopCardData {
            val badge = data.optJSONObject("imgTopContentV2") ?: data.optJSONObject("imgTopContent")
            val tags = data.content("dynamicTags")?.optJSONObject("promotionTags")
                ?.optJSONObject("discountTag")?.optJSONArray("tags")
            val ratio = data.optJSONObject("pictureRatio")
            val ratioX = ratio?.optInt("x", 16) ?: 16
            val ratioY = ratio?.optInt("y", 9) ?: 9
            return BigPicShopCardData(
                title = data.content("shopName")?.optString("name").orEmpty().ifEmpty { data.optString("title") },
                imageUrl = data.optJSONArray("pictures")?.optJSONObject(0)?.optString("url")
                    .orEmpty().ifEmpty { data.optString("shopMainPicUrl") }.httpImage(),
                imageRatio = (if (ratioX > 0 && ratioY > 0) ratioX.toFloat() / ratioY else 16f / 9f)
                    .takeIf { it in 0.5f..3f } ?: 16f / 9f,
                logoUrl = data.optString("logo").httpImage(),
                badgeUrl = badge?.optString("picUrl").orEmpty().httpImage(),
                badgeWidth = badge?.optInt("width", 0)?.coerceIn(0, 150)?.toFloat() ?: 0f,
                badgeHeight = badge?.optInt("height", 0)?.coerceIn(0, 80)?.toFloat() ?: 0f,
                bottomIconUrl = data.optJSONObject("imgBottomContent")?.optString("promotionIconUrl").orEmpty().httpImage(),
                bottomText = data.optJSONObject("imgBottomContent")?.optString("promotionText").orEmpty(),
                score = data.content("shopScore")?.optJSONObject("shopScoreInfo")?.optString("score").orEmpty(),
                fee = data.content("deliveryFee")?.optJSONObject("finalDeliveryFeeInfo")
                    ?.optJSONObject("price")?.optString("displayText").orEmpty(),
                deliveryTime = data.content("deliveryTime")?.optString("deliveryTimeDesc").orEmpty(),
                distance = data.content("distance")?.optJSONObject("distance")?.optString("desc").orEmpty(),
                category = data.content("shopCategory")?.optString("shopCategoryName").orEmpty(),
                tags = buildList {
                    if (tags != null) for (index in 0 until tags.length()) {
                        val tag = tags.optJSONObject(index) ?: continue
                        val subTags = tag.optJSONArray("subTags") ?: continue
                        for (part in 0 until subTags.length()) {
                            val subTag = subTags.optJSONObject(part) ?: continue
                            val text = subTag.optString("text").trim()
                            if (text.isNotEmpty()) add(ShopTag(text, tag.optString("backgroundColor"), subTag.optString("textColor")))
                        }
                    }
                },
            )
        }
    }
}

private fun String.httpImage(): String = when {
    startsWith("https://") -> this
    startsWith("http://img-ap-hongkong.mykeeta.net/") -> replaceFirst("http://", "https://")
    else -> ""
}

private fun JSONObject.content(type: String): JSONObject? {
    val rows = optJSONArray("content") ?: return null
    for (row in 0 until rows.length()) {
        val items = rows.optJSONArray(row) ?: continue
        for (index in 0 until items.length()) {
            val item = items.optJSONObject(index) ?: continue
            if (item.optString("type") == type) return item.optJSONObject("data")
        }
    }
    return null
}
