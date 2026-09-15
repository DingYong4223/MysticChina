package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

// newUserHotDish/src/template: show the banner and dishes only when productList exists.
internal fun ViewContainer<*, *>.NewUserHotDish(data: JSONObject, width: Float) {
    val products = data.optJSONObject("productArea")?.optJSONArray("productList") ?: return
    if (products.length() == 0) return
    HotDishes(data, width, products, 155f)
}

// NewUserHotDishV2: the V2 artwork keeps the same sections but uses wider dishes.
internal fun ViewContainer<*, *>.NewUserHotDishV2(data: JSONObject, width: Float) {
    val products = data.optJSONObject("productArea")?.optJSONArray("productList") ?: return
    if (products.length() == 0) return
    HotDishes(data, width, products, (width - 42f) / 2f)
}

private fun ViewContainer<*, *>.HotDishes(data: JSONObject, width: Float, products: JSONArray, itemWidth: Float) {
    View {
        attr { width(width); paddingBottom(12f); flexDirectionColumn(); backgroundColor(Color.WHITE) }
        if (data.optString("bgImage").isNotEmpty()) Image {
            attr { absolutePositionAllZero(); src(data.optString("bgImage")); resizeCover() }
        }
        val title = data.optJSONObject("productArea")?.optJSONObject("titleInfo")
        View {
            attr { height(42f); marginLeft(12f); marginRight(9f); flexDirectionRow(); alignItemsCenter() }
            if (title?.optString("titleImg").orEmpty().isNotEmpty()) Image {
                attr { width((width * 0.55f).coerceAtMost(230f)); height(32f); src(title!!.optString("titleImg")); resizeContain() }
            } else Text { attr { text(title?.optString("titleText").orEmpty()); fontSize(17f); fontWeightBold(); color(Color(0xFF222222)) } }
        }
        Scroller {
            attr { height(itemWidth + 72f); paddingLeft(9f); flexDirectionRow(); showScrollerIndicator(false) }
            for (i in 0 until products.length()) products.optJSONObject(i)?.let { product ->
                View {
                    attr { width(itemWidth); marginRight(8f); flexDirectionColumn(); backgroundColor(Color.WHITE); borderRadius(16f); overflow(true) }
                    val image = product.optJSONArray("pictures")?.optJSONObject(0)?.optString("url").orEmpty().ifEmpty { product.optString("shopMainPicUrl") }
                    View {
                        attr { width(itemWidth); height(itemWidth); borderRadius(16f); overflow(true) }
                        if (image.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(image); resizeCover() } }
                    }
                    Text { attr { marginTop(5f); text(product.optString("title")); fontSize(14f); color(Color(0xFF222222)); lines(2); textOverFlowTail() } }
                    if (product.optString("price").isNotEmpty()) Text { attr { text(product.optString("price")); fontSize(13f); color(Color(0xFFFF8700)) } }
                }
            }
            View { attr { width(16f) } }
        }
    }
}

// orderStatusCard/src/template: rounded status panel, order image, ETA and pickup code.
internal fun ViewContainer<*, *>.OrderStatusCard(data: JSONObject, width: Float) {
    OrderPanel(data, width, false)
}

// orderStatusBottom/src/template: separate compact floating format and version radius.
internal fun ViewContainer<*, *>.OrderStatusBottom(data: JSONObject, width: Float) {
    OrderPanel(data, width, true)
}

private fun ViewContainer<*, *>.OrderPanel(data: JSONObject, width: Float, compact: Boolean) {
    val margin = if (compact) 16f else 9f
    View {
        attr {
            marginLeft(margin); marginRight(margin); marginTop(if (compact) 12f else 20f)
            width((width - 2f * margin).coerceAtLeast(1f)); minHeight(if (compact) 70f else 85f)
            padding(12f); borderRadius(if (data.optInt("styleVersion", 0) == 1) 12f else 16f)
            flexDirectionRow(); alignItemsCenter(); backgroundColor(data.optString("bgColor").utilityColor(Color.WHITE))
        }
        if (data.optString("bgImg").isNotEmpty()) Image {
            attr { absolutePositionAllZero(); src(data.optString("bgImg")); resizeCover() }
        }
        if (data.optString("mainImg").isNotEmpty()) View {
            attr { size(if (compact) 48f else 58f, if (compact) 48f else 58f); borderRadius(10f); overflow(true); marginRight(8f) }
            Image { attr { absolutePositionAllZero(); src(data.optString("mainImg")); resizeCover() } }
        }
        View {
            attr { flex(1f); flexDirectionColumn() }
            Text {
                attr { text(data.optString("title") + data.optString("titleTail")); fontSize(15f); fontWeightSemiBold(); color(Color(0xFF222222)); lines(1); textOverFlowTail() }
            }
            val subtitle = data.optString("subtitle").ifEmpty { data.optString("subTitle") }
            if (subtitle.isNotEmpty()) Text { attr { marginTop(4f); text(subtitle); fontSize(12f); color(Color(0xFF555555)); lines(1) } }
            val eta = data.optString("etaTime")
            val code = data.optString("deliveryCode").ifEmpty { data.optString("pickUpCode") }
            if (eta.isNotEmpty() || code.isNotEmpty()) Text {
                attr { marginTop(4f); text(listOf(eta, code).filter { it.isNotEmpty() }.joinToString(" · ")); fontSize(12f); color(Color(0xFF222222)); lines(1) }
            }
        }
        val close = data.optString("closeImg")
        if (close.isNotEmpty()) Image { attr { absolutePosition(top = 2f, right = 2f); size(16f, 16f); src(close); resizeContain() } }
    }
}

// FeedbackCard/src/template: native rating row followed by configurable option labels.
internal fun ViewContainer<*, *>.FeedbackCard(data: JSONObject, width: Float) {
    val score = data.optJSONObject("scoreCard")
    View {
        attr {
            width((width - 18f).coerceAtLeast(1f)); minHeight(115f); marginLeft(9f); marginTop(16f); marginBottom(16f)
            padding(14f); flexDirectionColumn(); alignItemsCenter(); borderRadius(16f); backgroundColor(Color(0xFFF5F6FA))
        }
        Text { attr { text(score?.optString("feedBackTitle").orEmpty()); fontSize(15f); fontWeightSemiBold(); color(Color.BLACK); lines(2) } }
        View {
            attr { marginTop(12f); flexDirectionRow(); justifyContentCenter() }
            repeat(5) { Text { attr { marginLeft(5f); marginRight(5f); text("☆"); fontSize(26f); color(Color(0xFFFFB800)) } } }
        }
        val options = data.optJSONObject("optionCard")?.optJSONArray("optionList")
        if (options != null && options.length() > 0) View {
            attr { marginTop(8f); flexDirectionRow() }
            for (i in 0 until minOf(options.length(), 3)) options.optJSONObject(i)?.let { option ->
                View {
                    attr { marginRight(6f); paddingLeft(8f); paddingRight(8f); height(26f); borderRadius(13f); backgroundColor(Color.WHITE); allCenter() }
                    Text { attr { text(option.optString("text").ifEmpty { option.optString("title") }); fontSize(11f); color(Color(0xFF222222)) } }
                }
            }
        }
    }
}

// taskBottomBanner/src/template: separate 70px sidebar and bottom progress stripe.
internal fun ViewContainer<*, *>.TaskBottomBanner(data: JSONObject, width: Float) {
    val task = data.optJSONObject("taskDetailDTO") ?: return
    if (task.optInt("notRenderHomePageFloatLayer", 0) == 1) return
    if (data.optInt("floatStyle", 0) == 1) {
        val icon = task.optString("homepageTaskSidebarLogoPictureUrl")
        View {
            attr { width(70f); height(70f) }
            if (icon.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(icon); resizeContain() } }
        }
    } else View {
        attr { width(width); height(72f); paddingLeft(12f); paddingRight(12f); flexDirectionRow(); alignItemsCenter(); backgroundColor(Color(0xFFFFFDE0)) }
        if (data.optString("floatBgImg").isNotEmpty()) Image { attr { absolutePositionAllZero(); src(data.optString("floatBgImg")); resizeCover() } }
        if (task.optString("homePagePictureLogoUtl").isNotEmpty()) Image {
            attr { size(52f, 52f); marginRight(10f); src(task.optString("homePagePictureLogoUtl")); resizeContain() }
        }
        View {
            attr { flex(1f); flexDirectionColumn() }
            Text { attr { text(task.optString("title")); fontSize(14f); fontWeightSemiBold(); color(Color(0xFF222222)); lines(1) } }
            Text { attr { marginTop(4f); text(task.optString("content")); fontSize(12f); color(Color(0xFF555555)); lines(1) } }
        }
        if (data.optString("taskStartButton").isNotEmpty()) Image {
            attr { size(64f, 34f); src(data.optString("taskStartButton")); resizeContain() }
        }
    }
}

// Preheats/src/preheat/index.jsx dispatches by sceneCode to four distinct states.
internal fun ViewContainer<*, *>.Preheats(data: JSONObject, width: Float) {
    val scene = data.optInt("sceneCode", -1)
    val detail = when (scene) {
        201003555 -> data.optJSONObject("nuclearInfo")
        201003221, 201003222 -> data.optJSONObject("guideWord")
        201003216, 201003217 -> data.optJSONObject("guideInfo")
        else -> null
    }
    View {
        attr { width(width); minHeight(180f); padding(20f); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter(); backgroundColor(Color(0xFFF6F7FA)) }
        val picture = detail?.optString("guidePicUrl").orEmpty().ifEmpty { detail?.optString("backgroundPic").orEmpty() }
        if (picture.isNotEmpty()) Image { attr { size(92f, 92f); src(picture); resizeContain() } }
        Text {
            attr { marginTop(12f); text(detail?.optString("title").orEmpty().ifEmpty { data.optString("message") }); fontSize(17f); fontWeightSemiBold(); color(Color(0xFF222222)); textAlignCenter(); lines(2) }
        }
        if (detail?.optString("subTitle").orEmpty().isNotEmpty()) Text {
            attr { marginTop(6f); text(detail!!.optString("subTitle")); fontSize(13f); color(Color(0xFF666666)); textAlignCenter(); lines(2) }
        }
    }
}

// Single dish ad is supplied by the demo protocol rather than sailor_fe_c_card.
internal fun ViewContainer<*, *>.AdSingleDish(data: JSONObject, width: Float) {
    View {
        attr { width(width); padding(9f); flexDirectionRow(); backgroundColor(Color.WHITE) }
        val picture = data.optString("imageUrl")
        if (picture.isNotEmpty()) Image { attr { size(84f, 84f); marginRight(10f); src(picture); resizeCover() } }
        View {
            attr { flex(1f); flexDirectionColumn() }
            Text { attr { text(data.optString("title")); fontSize(16f); fontWeightSemiBold(); color(Color(0xFF222222)); lines(2) } }
            Text { attr { text(data.optString("shopName")); fontSize(12f); color(Color(0xFF555555)); lines(1) } }
            Text { attr { text(data.optString("price")); fontSize(14f); color(Color(0xFFFF8200)); lines(1) } }
        }
    }
}

private fun String.utilityColor(fallback: Color): Color = try {
    val hex = removePrefix("#")
    if (hex.length == 6) Color(0xFF000000L or hex.toLong(16)) else fallback
} catch (_: NumberFormatException) { fallback }
