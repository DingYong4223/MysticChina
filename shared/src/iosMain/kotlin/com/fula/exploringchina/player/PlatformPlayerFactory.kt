package com.fula.exploringchina.player

actual object PlatformPlayerFactory {
    actual fun createPlayer(): IVideoPlayer = IOSVideoPlayer()
}