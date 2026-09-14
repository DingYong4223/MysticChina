package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/** Native Kuikly rendering of the big-picture Puzzle feed card. No Flexbox runtime. */
fun ViewContainer<*, *>.BigPicShopCard(data: JSONObject, pageWidth: Float) {
    val card = BigPicShopCardData.from(data)
    val imageWidth = (pageWidth - 18f).coerceAtLeast(1f)
    View {
        attr { paddingLeft(9f); paddingRight(9f); paddingBottom(12f); backgroundColor(Color.WHITE); flexDirectionColumn() }
        View {
            attr { width(imageWidth); height(imageWidth / card.imageRatio); borderRadius(12f); overflow(true); backgroundColor(Color(0xFFF3F3F3)) }
            if (card.imageUrl.isNotEmpty()) {
                Image { attr { absolutePositionAllZero(); src(card.imageUrl); resizeCover() } }
            }
            if (card.badgeUrl.isNotEmpty() && card.badgeWidth > 0f && card.badgeHeight > 0f) {
                Image {
                    attr {
                        absolutePosition(top = 8f, left = 8f)
                        size(card.badgeWidth, card.badgeHeight)
                        src(card.badgeUrl)
                        resizeContain()
                    }
                }
            }
            if (card.logoUrl.isNotEmpty()) {
                View {
                    attr { absolutePosition(bottom = 8f, right = 8f); size(36f, 36f); padding(all = 2f); borderRadius(6f); overflow(true); backgroundColor(Color.WHITE) }
                    Image { attr { size(32f, 32f); src(card.logoUrl); resizeCover() } }
                }
            }
            if (card.bottomText.isNotEmpty()) {
                View {
                    attr {
                        absolutePosition(bottom = 0f, left = 0f, right = 0f)
                        height(22f); paddingLeft(8f); flexDirectionRow(); alignItemsCenter()
                        backgroundColor(Color(0xBF000000))
                    }
                    if (card.bottomIconUrl.isNotEmpty()) {
                        Image { attr { size(14f, 14f); marginRight(4f); src(card.bottomIconUrl); resizeContain() } }
                    }
                    Text { attr { text(card.bottomText); fontSize(11f); color(Color.WHITE); lines(1) } }
                }
            }
        }
        Text {
            attr {
                marginTop(9f); text(card.title); fontSize(16f); fontWeightSemiBold()
                color(Color(0xFF222222)); lines(1); textOverFlowTail()
                accessibility(card.title)
            }
        }
        val details = listOf(card.fee, card.deliveryTime, card.distance).filter { it.isNotEmpty() }.joinToString("  ·  ")
        if (card.score.isNotEmpty() || details.isNotEmpty()) {
            View {
                attr { marginTop(5f); flexDirectionRow(); alignItemsCenter() }
                if (card.score.isNotEmpty()) {
                    Text { attr { text("★ ${card.score}"); color(Color(0xFFFFA800)); fontSize(12f); marginRight(8f) } }
                }
                Text { attr { flex(1f); text(details); color(Color(0xFF4D4D4D)); fontSize(12f); lines(1); textOverFlowTail() } }
            }
        }
        if (card.category.isNotEmpty()) {
            Text { attr { marginTop(5f); text(card.category); color(Color(0xFF808080)); fontSize(12f); lines(1); textOverFlowTail() } }
        }
        if (card.tags.isNotEmpty()) {
            View {
                attr { marginTop(7f); flexDirectionRow(); alignItemsCenter() }
                card.tags.take(2).forEach { tag ->
                    View {
                        attr {
                            height(18f); marginRight(4f); paddingLeft(6f); paddingRight(6f)
                            borderRadius(6f); backgroundColor(tag.background.colorOr(Color(0xFFF3F3F3)))
                            allCenter()
                        }
                        Text { attr { text(tag.text); color(tag.color.colorOr(Color(0xFF333333))); fontSize(12f); lines(1) } }
                    }
                }
            }
        }
    }
}

private fun String.colorOr(fallback: Color): Color = try {
    val hex = removePrefix("#")
    if (length != 7 && length != 9) fallback else
        Color(if (hex.length == 6) 0xFF000000L or hex.toLong(16) else hex.toLong(16))
} catch (_: Throwable) {
    fallback
}
