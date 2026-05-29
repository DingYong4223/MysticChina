package com.yijian.bridge

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import java.io.File

/**
 * Android 平台文件访问桥接实现。
 *
 * 通过 MediaStore 访问共享媒体库，通过 ContentResolver 获取文件信息。
 */
class AndroidFileBridge(private val context: Context) : FileBridge {

    override fun listMediaFiles(directory: String, filter: MediaFilter): List<MediaFile> {
        val result = mutableListOf<MediaFile>()
        val cr: ContentResolver = context.contentResolver

        val uri = when (filter) {
            MediaFilter.VIDEO_ONLY -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            MediaFilter.IMAGE_ONLY -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            MediaFilter.ALL -> MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DISPLAY_NAME,
            MediaStore.Files.FileColumns.SIZE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED
        )

        val selection = when (filter) {
            MediaFilter.ALL -> "${MediaStore.Files.FileColumns.MIME_TYPE} LIKE 'video/%'"
            else -> null
        }

        cr.query(uri, projection, selection, null, null)?.use { cursor ->
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE)
            val addedCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataCol)
                val name = cursor.getString(nameCol)
                val size = cursor.getLong(sizeCol)
                val mime = cursor.getString(mimeCol) ?: "unknown"
                val added = cursor.getLong(addedCol)

                result.add(MediaFile(
                    path = path,
                    name = name,
                    size = size,
                    mimeType = mime,
                    addedTime = added * 1000L
                ))
            }
        }

        return result
    }

    override fun getFileSize(path: String): Long = File(path).takeIf { it.exists() }?.length() ?: 0L

    override fun fileExists(path: String): Boolean = File(path).exists()

    override fun getMimeType(path: String): String {
        val ext = MimeTypeMap.getFileExtensionFromUrl(path)
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) ?: "video/*"
    }

    override fun getThumbnailPath(path: String, maxWidth: Int, maxHeight: Int): String? {
        // 通过 MediaStore 获取缩略图
        return try {
            val cr = context.contentResolver
            val uri = Uri.parse("content://media/external/video/media")
            val projection = arrayOf(MediaStore.Video.Thumbnails.DATA)
            val selection = "${MediaStore.Video.Thumbnails.VIDEO_ID} = (SELECT _id FROM video WHERE _data = ?)"
            cr.query(uri, projection, selection, arrayOf(path), null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getString(0)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}