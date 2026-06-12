package com.fula.mysticchina

import com.fula.mysticchina.pages.EXPLORE_CATEGORIES
import com.fula.mysticchina.pages.FEATURED_CARDS
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExploreDataTest {

    @Test
    fun `FEATURED_CARDS is not empty`() {
        assertTrue(FEATURED_CARDS.isNotEmpty())
    }

    @Test
    fun `EXPLORE_CATEGORIES has exactly 5 categories`() {
        assertEquals(5, EXPLORE_CATEGORIES.size)
    }

    @Test
    fun `each category has at least one item`() {
        EXPLORE_CATEGORIES.forEach { category ->
            assertTrue(
                category.items.isNotEmpty(),
                "Category '${category.name}' should have at least one item"
            )
        }
    }

    @Test
    fun `first item in 文字书法 points to HanziPage`() {
        val writingCategory = EXPLORE_CATEGORIES.first()
        assertEquals("文字书法", writingCategory.name)
        assertEquals("HanziPage", writingCategory.items.first().pageName)
    }

    @Test
    fun `FEATURED_CARDS first card has non-null pageName`() {
        assertNotNull(FEATURED_CARDS.first().pageName)
    }

    @Test
    fun `coming soon items have null pageName`() {
        // Assertion: items without a live page use null (never blank string)
        // Deliberately checks non-blank rather than a specific page name so new
        // features can be launched without updating this test.
        val allItems = EXPLORE_CATEGORIES.flatMap { it.items }
        val availableItems = allItems.filter { it.pageName != null }
        assertTrue(availableItems.isNotEmpty(), "At least one item should be available")
        availableItems.forEach { item ->
            assertTrue(item.pageName!!.isNotBlank(), "pageName must not be blank for ${item.name}")
        }
    }
}
