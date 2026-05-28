package com.yijian.player

import com.yijian.player.PlayerState

/**
 * Android 平台视频播放器实现
 * 使用 Kotlin/Android 原生 MediaPlayer API
 * 注意：此文件通过 Kotlin Multiplatform expect/actual 或
 * Kuikly expand-native-api 桥接到原生层
 */
class AndroidVideoPlayer : IVideoPlayer {

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
        // 实际实现中，这里创建 Android MediaPlayer/ExoPlayer
        // 并设置 SurfaceView 进行渲染
        // 模拟准备完成
        _duration = 10000L // 10s mock
        state = PlayerState.READY
        onPrepared?.invoke()
        onStateChanged?.invoke(state)
    }

    override fun play() {
        if (state == PlayerState.READY || state == PlayerState.PAUSED || state == PlayerState.COMPLETED) {
            state = PlayerState.PLAYING
            onStateChanged?.invoke(state)
            // 启动进度更新定时器
        }
    }

    override fun pause() {
        if (state == PlayerState.PLAYING) {
            state = PlayerState.PAUSED
            onStateChanged?.invoke(state)
        }
    }

    override fun seekTo(positionMs: Long) {
        _currentPosition = positionMs.coerceAtLeast(0L)
        onProgress?.invoke(_currentPosition, _duration)
    }

    override fun setVolume(volume: Float) {
        // Android MediaPlayer.setVolume
    }

    override fun setLooping(looping: Boolean) {
        // Android MediaPlayer.isLooping
    }

    override fun getCurrentPosition(): Long = _currentPosition

    override fun getDuration(): Long = _duration

    override fun getState(): PlayerState = state

    override fun release() {
        state = PlayerState.RELEASED
        onStateChanged?.invoke(state)
        // 释放 MediaPlayer 实例
    }
}
