package com.fula.mysticchina.data

import com.fula.protocol.ProtocolPageSource
import com.fula.protocol.pb.v1.ProtocolResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal object MockNetwork {
    internal val pages = mapOf(
        "mysticchina_explore" to "explore.json",
        "mysticchina_learn" to "learn.json",
        "mysticchina_about" to "about.json",
    )

    private fun load(pageID: String, readAsset: (String) -> String?): ProtocolResponse {
        val resource = pages[pageID] ?: return ProtocolResponse(code = 404, message = "未知页面 ID: $pageID")
        val json = readAsset("common/protocol_pages/$resource")
            ?: return ProtocolResponse(code = 404, message = "协议资源不存在: $pageID")
        return runCatching { protocolJsonToPb(json) }
            .getOrElse { ProtocolResponse(code = 500, message = "协议转换失败: ${it.message ?: pageID}") }
    }

    fun request(
        pageID: String,
        readAsset: (String) -> String? = ::readTextAsset,
        onResult: (Result<ProtocolResponse>) -> Unit,
    ) {
        CoroutineScope(Dispatchers.Default).launch {
            onResult(runCatching {
                val response = load(pageID, readAsset)
                ProtocolResponse.ADAPTER.decode(ProtocolResponse.ADAPTER.encode(response))
            })
        }
    }

    fun install() {
        ProtocolPageSource.request = { pageID, onResult -> request(pageID, onResult = onResult) }
    }
}

internal expect fun readTextAsset(path: String): String?
