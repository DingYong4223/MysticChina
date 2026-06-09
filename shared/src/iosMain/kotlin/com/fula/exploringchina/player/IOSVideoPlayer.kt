package com.fula.exploringchina.player

import com.fula.exploringchina.player.PlayerState

/**
 * iOS 平台视频播放器实现
 * 使用 AVFoundation 的 AVPlayer
 * 通过 Kotlin/Native 桥接到 Objective-C
 */
class IOSVideoPlayer : IVideoPlayer {

    private var state = PlayerState.IDLE
    private var _currentPosition: Long = 0L
    private var _duration: Long = 0L

    override var onPrepared: (() -> Unit)? = null
    override var onProgress: ((Long, Long) -> Unit)? = null
    override var onCompletion: (() -> Unit)? = null
    override var onError: ((String) -> Unit)? = null
    override var onStateChanged: ((PlayerState) -> Unit)? = null

    override fun loadVideo(path: String) {
        state = PlayerState.LOADING
        onStateChanged?.invoke(state)
        // 实际实现中，创建 AVPlayer 和 AVPlayerLayer
        // 通过 Kotlin/Native interop 调用 Objective-C API
        _duration = 10000L // 10s mock
        state = PlayerState.READY
        onPrepared?.invoke()
        onStateChanged?.invoke(state)
    }

    override fun play() {
        if (state == PlayerState.READY || state == PlayerState.PAUSED || state == PlayerState.COMPLETED) {
            state = PlayerState.PLAYING
            // [avPlayer play]
            onStateChanged?.invoke(state)
        }
    }

    override fun pause() {
        if (state == PlayerState.PLAYING) {
            state = PlayerState.PAUSED
            // [avPlayer pause]
            onStateChanged?.invoke(state)
        }
    }

    override fun seekTo(positionMs: Long) {
        _currentPosition = positionMs.coerceAtLeast(0L)
        // CMTimeMake(positionMs, 1000)
        onProgress?.invoke(_currentPosition, _duration)
    }

    override fun setVolume(volume: Float) {
        // avPlayer.volume = volume
    }

    override fun setLooping(looping: Boolean) {
        // avPlayer.actionAtItemEnd = if (looping) .None else .Pause
    }

    override fun getCurrentPosition(): Long = _currentPosition

    override fun getDuration(): Long = _duration

    override fun getState(): PlayerState = state

    override fun release() {
        state = PlayerState.RELEASED
        onStateChanged?.invoke(state)
        // [avPlayer pause]; [avPlayer replaceCurrentItemWithPlayerItem:nil]
    }
}
