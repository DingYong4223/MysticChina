package com.yijian.util

import platform.Foundation.NSDate

actual fun currentTimeMs(): Long = (NSDate.date().timeIntervalSince1970() * 1000).toLong()
