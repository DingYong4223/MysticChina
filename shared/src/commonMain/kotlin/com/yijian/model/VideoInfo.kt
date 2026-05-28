package com.yijian.model

/**
 * 视频信息数据模型
 */
data class VideoInfo(
    val id: String,
    val title: String,
    val path: String,
    val thumbnailUrl: String = "",
    val duration: Long = 0L,          // 毫秒
    val fileSize: Long = 0L,          // 字节
    val width: Int = 0,
    val height: Int = 0,
    val createTime: Long = 0L
) {
    val formattedDuration: String
        get() {
            val totalSeconds = duration / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format("%02d:%02d", minutes, seconds)
        }
}

/**
 * 媒体条目 — 用于列表展示
 */
data class MediaItem(
    val video: VideoInfo,
    val isSelected: Boolean = false
)
