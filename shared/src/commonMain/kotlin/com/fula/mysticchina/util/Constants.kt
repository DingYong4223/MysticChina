package com.fula.mysticchina.util

/**
 * 工具常量
 */
object Constants {
    const val APP_NAME = "神秘中国"
    const val PAGE_MAIN = "MainPage"
    const val PAGE_PREVIEW = "PreviewPage"
    const val PAGE_SPLASH = "SplashPage"
    const val PAGE_HANZI = "HanziPage"
    const val PAGE_FOOD = "FoodPage"
    const val PAGE_CULTURE = "CulturePage"
    const val PAGE_SCENERY = "SceneryPage"
    const val PAGE_HUMANITY = "HumanityPage"
    const val PAGE_HISTORY = "HistoryPage"

    // Mock视频数据路径前缀
    const val MOCK_VIDEO_PREFIX = "file:///storage/emulated/0/Movies/"
}

/**
 * 时间格式化工具
 */
object FormatUtil {

    fun formatDuration(millis: Long): String {
        if (millis <= 0) return "00:00"
        val totalSeconds = millis / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return buildString {
            if (minutes < 10) append('0')
            append(minutes)
            append(':')
            if (seconds < 10) append('0')
            append(seconds)
        }
    }

    fun formatDurationLong(millis: Long): String {
        if (millis <= 0) return "00:00:00"
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return buildString {
            if (hours < 10) append('0')
            append(hours)
            append(':')
            if (minutes < 10) append('0')
            append(minutes)
            append(':')
            if (seconds < 10) append('0')
            append(seconds)
        }
    }
}
