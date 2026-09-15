package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.base.Border
import com.tencent.kuikly.core.base.BorderStyle
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.nvi.serialization.json.JSONArray
import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import com.tencent.kuikly.core.views.Image
import com.tencent.kuikly.core.views.Scroller
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

/** One native component per reference template. JSX/CSS under sailor_fe_c_card is read-only. */
fun ViewContainer<*, *>.ReferenceFlexboxCard(id: String, data: JSONObject, pageWidth: Float): Boolean {
    when (id) {
        "kflexbox_sailor_mkt_resources_vertical_banner_v2" -> VerticalBannerV2(data, pageWidth)
        "kflexbox_sailor_mkt_resources_new_vertical_banner" -> VerticalBanner(data, pageWidth)
        "kflexbox_sailor_mkt_resources_banner" -> HorizontalBanner(data, pageWidth)
        "kflexbox_sailor_c_image_banner" -> ImageBanner(data, pageWidth)
        "kflexbox_sailor_c_home_page_promotion_banner_sub" -> PromotionBanner(data, pageWidth)
        "kflexbox_sailor_c_search_no_results_card" -> SearchNoResultsCard(data, pageWidth)
        "kflexbox_sailor_c_notification_bar" -> NotificationBar(data, pageWidth)
        "kflexbox_sailor_c_second_search_bar" -> SecondSearchBar(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_countdown_bar" -> CountdownBar(data, pageWidth)
        "kflexbox_sailor_c_old_user_sticky" -> OldUserSticky(data, pageWidth)
        "kflexbox_sailor_c_new_user_sticky" -> NewUserSticky(data, pageWidth)
        "kflexbox_sailor_c_search_clue_card" -> SearchClueCard(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_tiles_area" -> HeaderTilesArea(data, pageWidth)
        "kflexbox_sailor_c_home_page_feeds_card_golden" -> FeedsShopCard(data, pageWidth)
        "kflexbox_sailor_c_home_page_feeds_grocery_card" -> FeedsGroceryCard(data, pageWidth)
        "kflexbox_sailor_c_home_page_feeds_card_with_dishes_golden" -> FeedsShopWithDishesCard(data, pageWidth)
        "kflexbox_sailor_c_search_shop_card" -> SearchShopCard(data, pageWidth)
        "kflexbox_sailor_c_home_page_feeds_theme_card" -> FeedsThemeCard(data, pageWidth)
        "kflexbox_sailor_c_search_similar_dishes" -> SearchSimilarDishes(data, pageWidth)
        "kflexbox_sailor_c_bottom_floating_layer" -> BottomFloatingLayer(data, pageWidth)
        "kflexbox_sailor_c_brand_logo_card_v3" -> HeaderBrandListCard(data, pageWidth)
        "kflexbox_sailor_c_brand_logo_card_v4" -> HeaderBrandListCardV2(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_shop_banner_v3" -> ShopBanner(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_shop_banner_v4" -> ShopBannerV2(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_shop_dish_entrance_v3" -> DishEntrance(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_shop_dish_entrance_v4" -> DishEntranceV2(data, pageWidth)
        "kflexbox_sailor_c_recent_order_entrance" -> RecentOrder(data, pageWidth)
        "kflexbox_sailor_c_recent_order_entrance_v2" -> RecentOrderV2(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_gathering_card_v3" -> HeaderGatheringCard(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_new_user_hot_dish" -> NewUserHotDish(data, pageWidth)
        "kflexbox_sailor_c_home_page_header_new_user_hot_dish_v2" -> NewUserHotDishV2(data, pageWidth)
        "kflexbox_sailor_c_order_status_card_new" -> OrderStatusCard(data, pageWidth)
        "kflexbox_sailor_c_order_status_bottom" -> OrderStatusBottom(data, pageWidth)
        "kflexbox_sailor_c_feedback_card" -> FeedbackCard(data, pageWidth)
        "kflexbox_sailor_c_home_page_task_bottom_banner" -> TaskBottomBanner(data, pageWidth)
        "kflexbox_sailor_c_area_preheat" -> Preheats(data, pageWidth)
        "kflexbox_sailor_ad_cpm_card_with_single_dish" -> AdSingleDish(data, pageWidth)
        else -> return false
    }
    return true
}

// VerticalBannerV2/src/template/index.jsx: two visible cards, 340:240 image ratio,
// 9px outer inset, 8px gap, 16px radius; no banner for fewer than three items.
private fun ViewContainer<*, *>.VerticalBannerV2(data: JSONObject, width: Float) {
    val cards = data.optJSONArray("launchTaskList") ?: return
    if (cards.length() <= 2) return
    val params = data.optJSONObject("inputParams")
    val itemWidth = ((width - 18f - 16f) / 2f).coerceAtLeast(1f)
    val height = itemWidth * (params?.optInt("imageHeight", 240) ?: 240).coerceAtLeast(1) /
        (params?.optInt("imageWidth", 340) ?: 340).coerceAtLeast(1)
    LaunchTaskStrip(cards, itemWidth, height, 8f)
}

// VerticalBanner/src/template/index.jsx: 3 columns for 4+ cards, otherwise 2;
// source aspect ratio defaults to 212:267.
private fun ViewContainer<*, *>.VerticalBanner(data: JSONObject, width: Float) {
    val cards = data.optJSONArray("launchTaskList") ?: return
    if (cards.length() == 0) return
    val columns = if (cards.length() > 3) 3 else 2
    val itemWidth = ((width - 18f - 8f * columns) / columns).coerceAtLeast(1f)
    val params = data.optJSONObject("inputParams")
    val height = itemWidth * (params?.optInt("imageHeight", 267) ?: 267).coerceAtLeast(1) /
        (params?.optInt("imageWidth", 212) ?: 212).coerceAtLeast(1)
    LaunchTaskStrip(cards, itemWidth, height, 8f)
}

private fun ViewContainer<*, *>.HorizontalBanner(data: JSONObject, width: Float) {
    val cards = data.optJSONArray("launchTaskList") ?: return
    if (cards.length() == 0) return
    val params = data.optJSONObject("inputParams")
    val itemWidth = (width - 18f).coerceAtLeast(1f)
    val height = itemWidth * (params?.optInt("imageHeight", 120) ?: 120).coerceAtLeast(1) /
        (params?.optInt("imageWidth", 750) ?: 750).coerceAtLeast(1)
    LaunchTaskStrip(cards, itemWidth, height, 8f)
}

private fun ViewContainer<*, *>.LaunchTaskStrip(cards: JSONArray, itemWidth: Float, height: Float, gap: Float) {
    Scroller {
        attr { height(height); flexDirectionRow(); paddingLeft(9f); showScrollerIndicator(false) }
        for (i in 0 until cards.length()) cards.optJSONObject(i)?.let { card ->
            View {
                attr { width(itemWidth); height(height); marginRight(gap); borderRadius(16f); overflow(true) }
                RefImage(card.optString("imgUrl"), itemWidth, height)
            }
        }
        View { attr { width(9f) } }
    }
}

// ImageBanner/src/template: the protocol's bannerLayout is the sizing source,
// including its intentionally spelled `horiziontalPadding` field.
private fun ViewContainer<*, *>.ImageBanner(data: JSONObject, width: Float) {
    val banner = data.optJSONArray("searchBannerInfoList")?.optJSONObject(0) ?: return
    if (banner.optString("bannerPicUrl").isEmpty()) return
    val layout = banner.optJSONObject("bannerLayout")
    val inset = (layout?.optInt("horiziontalPadding", 12) ?: 12).coerceIn(0, 48).toFloat()
    val imageWidth = (width - 2f * inset).coerceAtLeast(1f)
    val ratio = (layout?.optInt("height", 0) ?: 0).takeIf { it > 0 }?.toFloat()?.div(
        (layout?.optInt("width", 0) ?: 0).coerceAtLeast(1)
    )?.takeIf { it in 0.1f..2f } ?: 0.3f
    View {
        attr { paddingLeft(inset); paddingRight(inset); alignItemsCenter() }
        View {
            attr { width(imageWidth); height(imageWidth * ratio); borderRadius(12f); overflow(true) }
            RefImage(banner.optString("bannerPicUrl"), imageWidth, imageWidth * ratio)
        }
    }
}

// PromotionBanner/src/template: full-width illustration, proportional height,
// and copy placed over the left two thirds (Lottie is rendered as its image fallback).
private fun ViewContainer<*, *>.PromotionBanner(data: JSONObject, width: Float) {
    if (data.optString("bannerImageUrl").isEmpty() && data.optString("bannerLottieUrl").isEmpty()) return
    val ratio = data.optString("fitRatio").toFloatOrNull()?.takeIf { it > 0f }
    val height = if (ratio != null) width / ratio else width * 320f / 750f
    View {
        attr { width(width); height(height) }
        RefImage(data.optString("bannerImageUrl"), width, height)
        View {
            attr {
                positionAbsolute(); top(0f); left(0f); height(height); width(width * 2f / 3f)
                paddingLeft(20f); paddingRight(20f); justifyContentCenter()
            }
            val title = data.optString("bannerTitle")
            if (title.isNotEmpty()) Text {
                attr { text(title); color(Color(0xFF0C223E)); fontSize(17f); fontWeightBold(); lines(if (ratio != null && ratio >= 3.75f) 1 else 2) }
            }
            val subtitle = data.optString("bannerSubTitle")
            if (subtitle.isNotEmpty()) Text {
                attr { marginTop(6f); text(subtitle); color(Color(0xFF263E5D)); fontSize(12f); lines(if (ratio != null && ratio >= 3.75f) 3 else 5) }
            }
        }
    }
}

// SearchNoResultsCard/src/template: centered 100px image and two-line copy.
private fun ViewContainer<*, *>.SearchNoResultsCard(data: JSONObject, width: Float) {
    View {
        attr { width(width); flexDirectionColumn(); alignItemsCenter(); justifyContentCenter() }
        RefImage(data.optString("image"), 100f, 100f)
        Text {
            attr {
                marginTop(12f); marginBottom(4f); text(data.optString("title"))
                fontSize(16f); fontWeightSemiBold(); color(Color(0xFF222222))
                textAlignCenter(); lines(2)
            }
        }
        Text { attr { text(data.optString("subTitle")); fontSize(14f); color(Color(0xFF595959)); textAlignCenter(); lines(2) } }
    }
}

// NotificationBar/src/template: template 1 shows both image and title,
// template 2 image only, template 3 title only.
private fun ViewContainer<*, *>.NotificationBar(data: JSONObject, width: Float) {
    val template = data.optInt("noticeTemplate", 3)
    View {
        attr { width(width); paddingLeft(9f); paddingRight(9f); paddingBottom(12f) }
        View {
            attr {
                width((width - 18f).coerceAtLeast(1f)); minHeight(80f); borderRadius(16f)
                border(Border(1f, BorderStyle.SOLID, Color(0x14222222)))
                flexDirectionRow(); alignItemsCenter(); backgroundColor(Color.WHITE)
            }
            if (template == 1 || template == 2) {
                View { attr { marginLeft(8f) }; RefImage(data.optString("mainImg"), 58f, 58f) }
            }
            View {
                attr {
                    flex(1f); minHeight(58f); marginLeft(if (template == 3) 16f else 8f)
                    marginTop(10f); marginBottom(10f); marginRight(26f); justifyContentCenter()
                }
                if (template == 1 || template == 3) Text {
                    attr { text(data.optString("title")); fontSize(16f); fontWeightBold(); color(Color(0xFF222222)); lines(1); marginBottom(4f) }
                }
                Text { attr { text(data.optString("noticeBody")); fontSize(14f); fontWeightSemiBold(); color(Color(0xFF222222)); lines(3) } }
            }
            if (data.optString("closeButtonUrl").isNotEmpty()) Image {
                attr {
                    absolutePosition(top = 8f, right = 8f); size(12f, 12f)
                    src(data.optString("closeButtonUrl")); resizeContain()
                }
            }
        }
    }
}

// SecondSearchBar/src/template/index.css: 80x96 slots and 60x60 thumbnails.
// The selected image is outlined rather than masquerading as a chip.
private fun ViewContainer<*, *>.SecondSearchBar(data: JSONObject, width: Float) {
    val items = data.optJSONArray("items") ?: return
    if (items.length() == 0) return
    Scroller {
        attr { width(width); height(96f); flexDirectionRow(); backgroundColor(Color.WHITE); showScrollerIndicator(false) }
        for (i in 0 until items.length()) items.optJSONObject(i)?.let { item ->
            View {
                attr { width(80f); height(96f); paddingTop(7f); paddingBottom(11f); alignItemsCenter(); flexDirectionColumn() }
                View {
                    attr { width(60f); height(60f); marginBottom(4f); borderRadius(16f); overflow(true) }
                    RefImage(item.optString("imageUrl"), 60f, 60f)
                }
                Text { attr { width(80f); text(item.optString("text")); fontSize(12f); color(Color.BLACK); textAlignCenter(); lines(1) } }
            }
        }
        View { attr { width(16f) } }
    }
}

// countdownBar/src/template: left icon/text and right countdown text on its image background.
private fun ViewContainer<*, *>.CountdownBar(data: JSONObject, width: Float) {
    val info = data.optJSONObject("infoArea") ?: return
    val time = data.optJSONObject("timeArea")
    View {
        attr { width(width); height(44f); flexDirectionRow(); alignItemsCenter(); paddingLeft(12f); paddingRight(12f); backgroundColor(Color(0xFFFFF5E7)) }
        if (data.optString("backgroundPic").isNotEmpty()) Image {
            attr { absolutePositionAllZero(); src(data.optString("backgroundPic")); resizeCover() }
        }
        if (info.optString("icon").isNotEmpty()) Image {
            attr { size(16f, 16f); marginRight(4f); src(info.optString("icon")); resizeContain() }
        }
        Text {
            attr { flex(1f); text(info.optString("text")); fontSize(14f); color(info.optString("textColor").referenceColor(Color(0xFF222222))); lines(1) }
        }
        if (time != null) Text {
            attr {
                text(listOf(time.optString("leftText"), time.optString("rightText")).filter { it.isNotEmpty() }.joinToString(" "))
                fontSize(13f); color(time.optString("textColor").referenceColor(Color(0xFF222222))); lines(1)
            }
        }
    }
}

private fun ViewContainer<*, *>.OldUserSticky(data: JSONObject, width: Float) = StickyBenefits(data, width)

private fun ViewContainer<*, *>.NewUserSticky(data: JSONObject, width: Float) = StickyBenefits(data, width)

// oldUserSticky/newUserSticky share the same 36px inner bar in their original CSS.
private fun ViewContainer<*, *>.StickyBenefits(data: JSONObject, width: Float) {
    val benefits = data.optJSONArray("benefitAreas") ?: return
    View {
        attr { width(width); height(46f); paddingTop(5f); paddingBottom(5f); paddingLeft(9f); paddingRight(9f); backgroundColor(Color.WHITE) }
        View {
            attr {
                height(36f); flexDirectionRow(); alignItemsCenter(); paddingLeft(12f); paddingRight(12f)
                borderRadius(10f); backgroundColor(data.optString("backgroundColor").referenceColor(Color(0xFFFFF5F2)))
            }
            for (i in 0 until benefits.length()) benefits.optJSONObject(i)?.let { item ->
                if (i > 0) View { attr { width(1f); height(15f); marginLeft(7f); marginRight(7f); backgroundColor(Color(0x1A000000)) } }
                View {
                    attr { flexDirectionRow(); alignItemsCenter(); if (benefits.length() == 1) flex(1f) }
                    if (item.optString("iconUrl").isNotEmpty()) Image {
                        attr { size(18f, 18f); marginRight(5f); src(item.optString("iconUrl")); resizeContain() }
                    }
                    Text {
                        attr { text(item.optString("text")); color(item.optString("textFontColor").referenceColor(Color(0xFF222222))); fontSize(12f); lines(1) }
                    }
                    if (item.optString("supplementText").isNotEmpty()) Text {
                        attr {
                            marginLeft(4f); text("· ${item.optString("supplementText")}")
                            color(item.optString("supplementColor").referenceColor(Color(0xFF222222))); fontSize(12f); lines(1)
                        }
                    }
                    if (item.optString("linkIcon").isNotEmpty()) Image {
                        attr { size(14f, 14f); marginLeft(5f); src(item.optString("linkIcon")); resizeContain() }
                    }
                }
                if (benefits.length() == 1 && item.optString("subtitle").isNotEmpty()) Text {
                    attr {
                        text(item.optString("subtitle")); fontSize(12f); lines(1)
                        color(item.optString("subtitleColor").referenceColor(Color(0xFF333333)))
                    }
                }
            }
        }
    }
}

// SearchClueCard/src/template: 82px image, text column, and a 30px request pill.
private fun ViewContainer<*, *>.SearchClueCard(data: JSONObject, width: Float) {
    View {
        attr { width(width); paddingTop(16f); paddingBottom(20f); paddingLeft(9f); paddingRight(9f); flexDirectionColumn(); backgroundColor(Color.WHITE) }
        View {
            attr { flexDirectionRow() }
            RefImage(data.optString("mainImg"), 82f, 82f)
            View {
                attr { flex(1f); marginLeft(8f); marginTop(4f); flexDirectionColumn() }
                Text { attr { text(data.optString("title")); fontWeightSemiBold(); fontSize(16f); color(Color.BLACK); lines(1); marginBottom(4f) } }
                Text { attr { text(data.optString("subTitle")); fontSize(12f); color(Color(0xFF555555)); lines(1); marginBottom(5f) } }
                View {
                    attr { height(30f); paddingLeft(10f); paddingRight(12f); borderRadius(10f); flexDirectionRow(); alignItemsCenter(); backgroundColor(Color(0xFFF0F1F5)) }
                    Text {
                        attr { text(if (data.optInt("buttonStatus", 0) == 0) "Request this shop" else "Requested"); fontSize(12f); color(Color.BLACK); lines(1) }
                    }
                }
            }
        }
        View { attr { marginTop(16f); height(8f); backgroundColor(Color(0xFFF0F1F5)) } }
    }
}

// HeaderTilesArea/src/template: title row above a horizontally scrolling tile strip.
private fun ViewContainer<*, *>.HeaderTilesArea(data: JSONObject, width: Float) {
    val tiles = data.optJSONArray("tileList") ?: return
    val title = data.optJSONObject("cardTitle")
    View {
        attr { width(width); flexDirectionColumn(); backgroundColor(Color.WHITE) }
        View {
            attr { height(48f); paddingLeft(12f); flexDirectionRow(); alignItemsCenter() }
            if (title?.optString("titleImg").orEmpty().isNotEmpty()) RefImage(title!!.optString("titleImg"), width * 0.5f, 32f)
            else Text { attr { text(title?.optString("titleText").orEmpty()); fontSize(18f); fontWeightBold(); color(Color(0xFF222222)) } }
        }
        val size = data.optString("width").toFloatOrNull()?.takeIf { it in 40f..250f } ?: 128f
        val height = data.optString("height").toFloatOrNull()?.takeIf { it in 40f..250f } ?: size
        Scroller {
            attr { height(height); paddingLeft(9f); flexDirectionRow(); showScrollerIndicator(false) }
            for (i in 0 until tiles.length()) tiles.optJSONObject(i)?.let { tile ->
                View {
                    attr { marginRight(8f); width(size); height(height); borderRadius(12f); overflow(true) }
                    RefImage(tile.optString("cardImg"), size, height)
                }
            }
            View { attr { width(16f) } }
        }
    }
}

private fun ViewContainer<*, *>.RefImage(url: String, width: Float, height: Float) {
    if (url.isNotEmpty()) Image { attr { size(width, height); src(url); resizeContain() } }
    else View { attr { size(width, height); backgroundColor(Color(0xFFF2F2F2)) } }
}

// FeedsThemeCard/src/template: background skin, 12px heading inset, then
// horizontal square shop cards. The protocol supplies themeShopPageData as an array.
private fun ViewContainer<*, *>.FeedsThemeCard(data: JSONObject, width: Float) {
    val style = data.optJSONObject("cardStyleInfo")
    val title = data.optJSONObject("cardTitleInfo")
    val shops = data.optJSONArray("themeShopPageData")
    View {
        attr {
            width(width); paddingBottom(12f)
            backgroundColor(style?.optString("bgColor").orEmpty().referenceColor(Color(0xFFEEEFF4)))
        }
        if (style?.optString("bgImg").orEmpty().isNotEmpty()) Image {
            attr { absolutePositionAllZero(); src(style!!.optString("bgImg")); resizeCover() }
        }
        View {
            attr { height(48f); paddingLeft(12f); flexDirectionRow(); alignItemsCenter() }
            if (title?.optString("titleImg").orEmpty().isNotEmpty()) {
                val ratio = title?.optString("ratio")?.toFloatOrNull()?.takeIf { it > 0f } ?: 6f
                RefImage(title!!.optString("titleImg"), (width - 45f).coerceAtLeast(1f), (width - 45f) / ratio)
            } else Text {
                attr { flex(1f); text(title?.optString("title").orEmpty()); fontSize(16f); fontWeightSemiBold(); color(Color.BLACK); lines(1) }
            }
            if (title?.optString("jumpIcon").orEmpty().isNotEmpty()) RefImage(title!!.optString("jumpIcon"), 16f, 16f)
        }
        if (shops != null && shops.length() > 0) {
            val itemWidth = if (data.optInt("type", 0) == 2) (width - 52f) / 2f else 230f
            Scroller {
                attr { height(itemWidth + 82f); paddingLeft(9f); flexDirectionRow(); showScrollerIndicator(false) }
                for (i in 0 until shops.length()) shops.optJSONObject(i)?.let { shop ->
                    View {
                        attr { width(itemWidth); marginRight(10f); borderRadius(16f); overflow(true); backgroundColor(Color.WHITE) }
                        View {
                            attr { width(itemWidth); height(itemWidth); borderRadius(16f); overflow(true) }
                            val picture = shop.optJSONArray("pictures")?.optJSONObject(0)?.optString("url").orEmpty()
                                .ifEmpty { shop.optString("shopMainPicUrl") }
                            if (picture.isNotEmpty()) Image { attr { absolutePositionAllZero(); src(picture); resizeCover() } }
                            if (shop.optString("logo").isNotEmpty()) Image {
                                attr { absolutePosition(bottom = 6f, right = 6f); size(28f, 28f); src(shop.optString("logo")); resizeContain() }
                            }
                        }
                        val name = shop.optString("title")
                        if (name.isNotEmpty()) Text {
                            attr { marginTop(5f); marginLeft(6f); text(name); fontSize(14f); color(Color(0xFF222222)); lines(2); textOverFlowTail() }
                        }
                    }
                }
                View { attr { width(16f) } }
            }
        }
    }
}

// SearchSimilarDishes/src/template: section divider, heading and dishes.
private fun ViewContainer<*, *>.SearchSimilarDishes(data: JSONObject, width: Float) {
    val title = data.optJSONObject("cardTitleInfo")
    if (data.optInt("showIndex", 0) > 0) View { attr { width(width); height(8f); backgroundColor(Color(0xFFF0F1F5)) } }
    View {
        attr { width(width); flexDirectionColumn(); backgroundColor(Color.WHITE) }
        if (title?.optString("title").orEmpty().isNotEmpty()) Text {
            attr { marginLeft(12f); marginTop(14f); marginBottom(10f); text(title!!.optString("title")); fontSize(16f); fontWeightSemiBold(); color(Color(0xFF222222)) }
        }
        data.optJSONArray("themeShopPageData")?.let { shops ->
            val itemWidth = (width - 38f) / 3f
            Scroller {
                attr { height(itemWidth + 50f); paddingLeft(9f); flexDirectionRow() }
                for (i in 0 until shops.length()) shops.optJSONObject(i)?.let { dish ->
                    View {
                        attr { width(itemWidth); marginRight(10f) }
                        val image = dish.optJSONArray("pictures")?.optJSONObject(0)?.optString("url").orEmpty()
                        RefImage(image, itemWidth, itemWidth)
                        Text { attr { text(dish.optString("title")); fontSize(12f); color(Color(0xFF333333)); lines(1) } }
                    }
                }
                View { attr { width(16f) } }
            }
        }
    }
}

// bottomFloatingLayer/src/template/index.css: 72px logo floats above 44px bar.
private fun ViewContainer<*, *>.BottomFloatingLayer(data: JSONObject, width: Float) {
    if (data.optString("text").isEmpty()) return
    View {
        attr { width(width); height(72f); paddingLeft(7.5f); paddingRight(7.5f); justifyContentFlexEnd() }
        View {
            attr { height(44f); borderRadius(10f); flexDirectionRow(); alignItemsCenter(); backgroundColor(Color(0xFF62390B)) }
            if (data.optString("backgroundImageUrl").isNotEmpty()) Image {
                attr { absolutePositionAllZero(); src(data.optString("backgroundImageUrl")); resizeCover() }
            }
            if (data.optString("imageUrl").isNotEmpty()) Image {
                attr { width(72f); height(72f); marginRight(8f); alignSelfFlexEnd(); src(data.optString("imageUrl")); resizeContain() }
            }
            Text {
                attr {
                    flex(1f); text(data.optString("text")); fontSize(16f); fontWeightBold()
                    color(data.optString("textColor").referenceColor(Color.WHITE)); lines(1); textOverFlowTail()
                }
            }
            if (data.optString("buttonIcon").isNotEmpty()) Image {
                attr {
                    width(data.optInt("buttonIconWidth", 16).coerceIn(8, 90).toFloat())
                    height(data.optInt("buttonIconHeight", 16).coerceIn(8, 70).toFloat())
                    marginRight(21f); src(data.optString("buttonIcon")); resizeContain()
                }
            }
        }
    }
}

private fun String.referenceColor(fallback: Color): Color = try {
    val hex = removePrefix("#")
    if (hex.length == 6) Color(0xFF000000L or hex.toLong(16)) else fallback
} catch (_: NumberFormatException) { fallback }
