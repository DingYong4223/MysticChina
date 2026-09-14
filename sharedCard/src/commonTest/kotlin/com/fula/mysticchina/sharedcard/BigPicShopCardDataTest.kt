package com.fula.mysticchina.sharedcard

import com.tencent.kuikly.core.nvi.serialization.json.JSONObject
import kotlin.test.Test
import kotlin.test.assertEquals

class BigPicShopCardDataTest {
    @Test
    fun readsBigPictureAndNestedContent() {
        val data = BigPicShopCardData.from(JSONObject("""{
            "pictures":[{"url":"https://example.com/shop.jpg"}],
            "pictureRatio":{"x":16,"y":9},
            "content":[
              [{"type":"shopName","data":{"name":"Dough Bros"}}],
              [{"type":"shopScore","data":{"shopScoreInfo":{"score":"4.6"}}},
               {"type":"deliveryFee","data":{"finalDeliveryFeeInfo":{"price":{"displayText":"Free"}}}}],
              [{"type":"dynamicTags","data":{"promotionTags":{"discountTag":{"tags":[
                {"backgroundColor":"#FFEE6B","subTags":[{"text":"25% off","textColor":"#662200"}]}
              ]}}}}]
            ]
        }"""))
        assertEquals("Dough Bros", data.title)
        assertEquals("https://example.com/shop.jpg", data.imageUrl)
        assertEquals(16f / 9f, data.imageRatio)
        assertEquals("4.6", data.score)
        assertEquals("Free", data.fee)
        assertEquals(ShopTag("25% off", "#FFEE6B", "#662200"), data.tags.single())
        val invalid = BigPicShopCardData.from(JSONObject("""{"pictures":[{"url":"file:///private/secret"}],"pictureRatio":{"x":1,"y":999}}"""))
        assertEquals("", invalid.imageUrl)
        assertEquals(16f / 9f, invalid.imageRatio)
        val upgraded = BigPicShopCardData.from(JSONObject("""{"pictures":[{"url":"http://img-ap-hongkong.mykeeta.net/a.jpg"}]}"""))
        assertEquals("https://img-ap-hongkong.mykeeta.net/a.jpg", upgraded.imageUrl)
    }
}
