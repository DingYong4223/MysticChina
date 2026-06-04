package com.yijian.manager

import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.module.SharedPreferencesModule
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.reactive.handler.observableList
import com.yijian.model.VideoInfo

class DraftManager(private val sp: SharedPreferencesModule) {

    companion object {
        private const val TAG = "DraftMgr"
        private const val SP_KEY_DRAFTS = "yijian_drafts"
    }

    var draftList by observableList<VideoInfo>()
    var selectedIds by observableList<String>()
    var isEditing by observable(false)
    var onEditingStateChanged: ((Boolean) -> Unit)? = null

    val selectedCount: Int get() = selectedIds.size
    val isAllSelected: Boolean get() = draftList.isNotEmpty() && selectedIds.size == draftList.size

    fun load() {
        val json = sp.getString(SP_KEY_DRAFTS) ?: return
        draftList.clear()
        draftList.addAll(parseVideoInfoList(json))
    }

    fun add(info: VideoInfo) {
        draftList.add(0, info)
        save()
    }

    fun remove(ids: List<String>) {
        val idSet = ids.toSet()
        draftList.removeAll { it.id in idSet }
        selectedIds.removeAll { it in idSet }
        if (draftList.isEmpty()) exitEditing()
        save()
    }

    fun enterSelection(id: String) {
        KLog.d(TAG, "enterSelection: id=$id")
        isEditing = true
        onEditingStateChanged?.invoke(true)
        if (!selectedIds.contains(id)) selectedIds.add(id)
    }

    fun toggleSelection(id: String) {
        if (selectedIds.contains(id)) selectedIds.remove(id)
        else selectedIds.add(id)
        if (selectedIds.isEmpty()) exitEditing()
    }

    fun selectAll() {
        selectedIds.clear()
        selectedIds.addAll(draftList.map { it.id })
    }

    fun deselectAll() {
        selectedIds.clear()
    }

    fun exitEditing() {
        KLog.d(TAG, "exitEditing")
        isEditing = false
        onEditingStateChanged?.invoke(false)
        selectedIds.clear()
    }

    // ─── 持久化 ───

    private fun save() {
        sp.setString(SP_KEY_DRAFTS, serializeVideoInfoList(draftList))
    }

    private fun serializeVideoInfoList(list: List<VideoInfo>): String {
        val sb = StringBuilder("[")
        for ((i, v) in list.withIndex()) {
            if (i > 0) sb.append(',')
            sb.append("""{"id":"${escape(v.id)}","title":"${escape(v.title)}","path":"${escape(v.path)}",""")
            sb.append("""duration":${v.duration},"fileSize":${v.fileSize},"createTime":${v.createTime}}""")
        }
        sb.append(']')
        return sb.toString()
    }

    private fun parseVideoInfoList(json: String): List<VideoInfo> {
        if (!json.startsWith('[') || !json.endsWith(']')) return emptyList()
        val trimmed = json.substring(1, json.length - 1).trim()
        if (trimmed.isEmpty()) return emptyList()
        val result = mutableListOf<VideoInfo>()
        var i = 0
        while (i < trimmed.length) {
            val objStart = trimmed.indexOf('{', i)
            if (objStart < 0) break
            val objEnd = trimmed.indexOf('}', objStart)
            if (objEnd < 0) break
            val obj = trimmed.substring(objStart, objEnd + 1)
            result.add(parseVideoInfo(obj) ?: continue)
            i = objEnd + 1
        }
        return result
    }

    private fun parseVideoInfo(obj: String): VideoInfo? {
        try {
            val id = extractStr(obj, "id") ?: return null
            val title = extractStr(obj, "title") ?: ""
            val path = extractStr(obj, "path") ?: ""
            val duration = extractLong(obj, "duration")
            val fileSize = extractLong(obj, "fileSize")
            val createTime = extractLong(obj, "createTime")
            return VideoInfo(
                id = id, title = title, path = path,
                duration = duration, fileSize = fileSize,
                createTime = createTime,
            )
        } catch (_: Exception) { return null }
    }

    private fun extractStr(json: String, key: String): String? {
        val idx = json.indexOf("\"$key\"")
        if (idx < 0) return null
        val colon = json.indexOf(':', idx)
        if (colon < 0) return null
        var start = colon + 1
        while (start < json.length && json[start] == ' ') start++
        if (start >= json.length || json[start] != '"') return null
        start++
        val sb = StringBuilder()
        var pos = start
        while (pos < json.length) {
            val c = json[pos]
            if (c == '\\') {
                val next = json.getOrElse(pos + 1) { '?' }
                sb.append(when (next) { 'n' -> '\n'; 'r' -> '\r'; 't' -> '\t'; '"' -> '"'; '\\' -> '\\'; else -> '?' })
                pos += 2
            } else if (c == '"') break
            else { sb.append(c); pos++ }
        }
        return sb.toString()
    }

    private fun extractLong(json: String, key: String): Long {
        val idx = json.indexOf("\"$key\"")
        if (idx < 0) return 0L
        val colon = json.indexOf(':', idx)
        if (colon < 0) return 0L
        var start = colon + 1
        while (start < json.length && !json[start].isDigit() && json[start] != '-') start++
        var end = start
        while (end < json.length && (json[end].isDigit() || json[end] == '-')) end++
        return json.substring(start, end).trim().toLongOrNull() ?: 0L
    }

    private fun escape(s: String): String = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t")
}
