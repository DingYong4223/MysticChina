package com.yijian.bridge

/**
 * 文件访问桥接接口 — 让 commonMain 层能间接访问平台文件系统。
 *
 * Android 通过 MediaStore/ContentResolver 实现。
 * iOS 通过 PHAsset/FileManager 实现。
 */
interface FileBridge {

    /** 列出指定目录下的媒体文件路径 */
    fun listMediaFiles(directory: String, filter: MediaFilter = MediaFilter.ALL): List<MediaFile>

    /** 读取文件大小（字节） */
    fun getFileSize(path: String): Long

    /** 文件是否存在 */
    fun fileExists(path: String): Boolean

    /** 获取文件的 MIME 类型 */
    fun getMimeType(path: String): String

    /** 缩略图获取（返回 platform 本地文件路径或 null） */
    fun getThumbnailPath(path: String, maxWidth: Int = 320, maxHeight: Int = 180): String?
}

/** 媒体文件描述 */
data class MediaFile(
    val path: String,
    val name: String,
    val size: Long,
    val mimeType: String,
    val durationMs: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val addedTime: Long = 0L
)

/** 媒体过滤类型 */
enum class MediaFilter {
    ALL,
    VIDEO_ONLY,
    IMAGE_ONLY
}