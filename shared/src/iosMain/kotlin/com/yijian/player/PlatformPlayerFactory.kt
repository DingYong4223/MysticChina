package com.yijian.player

actual object PlatformPlayerFactory {
    actual fun createPlayer(): IVideoPlayer = IOSVideoPlayer()
}