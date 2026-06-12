package com.fula.mysticchina.util

import platform.Foundation.NSFileManager

actual fun fileExists(path: String): Boolean =
    NSFileManager.defaultManager.fileExistsAtPath(path)
