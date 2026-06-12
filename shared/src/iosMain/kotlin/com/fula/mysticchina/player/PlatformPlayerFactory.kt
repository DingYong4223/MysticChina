package com.fula.mysticchina.player

actual object PlatformPlayerFactory {
    actual fun createPlayer(): IVideoPlayer = IOSVideoPlayer()
}