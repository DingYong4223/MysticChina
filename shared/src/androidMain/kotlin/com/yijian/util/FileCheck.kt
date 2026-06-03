package com.yijian.util

import java.io.File

actual fun fileExists(path: String): Boolean = File(path).exists()
