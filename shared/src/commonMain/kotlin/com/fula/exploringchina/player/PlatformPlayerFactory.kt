package com.fula.exploringchina.player

/**
 * 平台播放器工厂 — expect 由各平台提供 actual。
 *
 * Android → AndroidVideoPlayer (MediaPlayer)
 * iOS     → IOSVideoPlayer (AVPlayer)
 * macOS   → stub (will use AVPlayer)
 */
expect object PlatformPlayerFactory {
    fun createPlayer(): IVideoPlayer
}