package com.fula.mysticchina.data

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSBundle
import platform.Foundation.NSFileManager
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal actual fun readTextAsset(path: String): String? {
    val resourcePath = NSBundle.mainBundle.resourcePath ?: return null
    val data = NSFileManager.defaultManager.contentsAtPath("$resourcePath/$path") ?: return null
    val bytes = ByteArray(data.length.toInt())
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), data.bytes, data.length) }
    return runCatching { bytes.decodeToString(throwOnInvalidSequence = true) }.getOrNull()
}
