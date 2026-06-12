package com.fula.mysticchina.player

/**
 * macOS 平台播放器工厂 — 一期暂用 stub，后续接入 AVPlayer。
 */
actual object PlatformPlayerFactory {
    actual fun createPlayer(): IVideoPlayer {
        return object : IVideoPlayer {
            private var state = PlayerState.IDLE
            override var onPrepared: (() -> Unit)? = null
            override var onProgress: ((Long, Long) -> Unit)? = null
            override var onCompletion: (() -> Unit)? = null
            override var onError: ((String) -> Unit)? = null
            override var onStateChanged: ((PlayerState) -> Unit)? = null
            override fun loadVideo(path: String) {}
            override fun play() {}
            override fun pause() {}
            override fun seekTo(positionMs: Long) {}
            override fun setVolume(volume: Float) {}
            override fun setLooping(looping: Boolean) {}
            override fun getCurrentPosition(): Long = 0L
            override fun getDuration(): Long = 0L
            override fun getState(): PlayerState = state
            override fun release() {}
        }
    }
}