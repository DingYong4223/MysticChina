package com.fula.mysticchina

import com.fula.protocol.PROTOCOL_PAGE_NAME

object App {
    const val START_PAGE = "HomePage"
    const val HANZI_PAGE_ID = "mysticchina_hanzi"

    fun resolvePageName(pageName: String, pageID: String): String =
        if (pageName == PROTOCOL_PAGE_NAME && pageID == HANZI_PAGE_ID) "HanziPage" else pageName
}
