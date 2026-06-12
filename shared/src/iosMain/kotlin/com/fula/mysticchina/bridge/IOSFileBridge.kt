package com.fula.mysticchina.bridge

/**
 * iOS 平台文件访问桥接实现 (一期暂不实现，接口保留)。
 *
 * 使用 PHAsset + FileManager 访问相册和文件系统。
 */
class IOSFileBridge : FileBridge {

    override fun listMediaFiles(directory: String, filter: MediaFilter): List<MediaFile> {
        // TODO: iOS — PHAsset.fetchAssets(with: .video, options: sortOptions)
        return emptyList()
    }

    override fun getFileSize(path: String): Long = 0L

    override fun fileExists(path: String): Boolean = false

    override fun getMimeType(path: String): String = "video/mp4"

    override fun getThumbnailPath(path: String, maxWidth: Int, maxHeight: Int): String? = null
}