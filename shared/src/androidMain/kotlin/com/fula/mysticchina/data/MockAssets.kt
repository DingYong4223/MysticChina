package com.fula.mysticchina.data

import android.content.Context

object MockAssets {
    private lateinit var context: Context

    fun initialize(context: Context) {
        this.context = context.applicationContext
    }

    internal fun readText(path: String): String? = runCatching {
        context.assets.open(path).bufferedReader().use { it.readText() }
    }.getOrNull()
}

internal actual fun readTextAsset(path: String): String? = MockAssets.readText(path)
