package com.fula.mysticchina.player

import android.view.Surface

/**
 * 跨模块 Surface 注册表 — 由 VideoRenderViewImpl (app层) 注册，
 * 由 AndroidVideoPlayer (shared层) 消费。
 */
object SurfaceRegistry {
    private val map = mutableMapOf<Long, Surface>()
    private var nextId = 1L

    @Synchronized
    fun register(surface: Surface): Long {
        val id = nextId++
        map[id] = surface
        return id
    }

    @Synchronized
    fun get(id: Long): Surface? = map[id]

    @Synchronized
    fun remove(id: Long): Surface? = map.remove(id)
}
