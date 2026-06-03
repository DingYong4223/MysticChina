package com.yijian.player

import com.tencent.kuikly.core.reactive.handler.observable
import com.yijian.util.FormatUtil

@Suppress("DEPRECATION") // standalone controller: observable delegates require PagerScope

/**
 * 播放器控制器 — 状态管理 + 命令封装
 * 作为 UI 层与 IVideoPlayer 之间的中介
 */
class PlayerController {

    // 响应式播放状态
    var playerState by observable(PlayerState.IDLE)
    var currentPositionMs by observable(0L)
    var durationMs by observable(0L)
    var isControlsVisible by observable(true)
    var errorMessage by observable("")

    // 底层播放器
    private var player: IVideoPlayer? = null

    // 进度更新定时器标志
    private var isProgressUpdating = false

    val currentProgress: Float
        get() = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else 0f

    val currentTimeText: String
        get() = FormatUtil.formatDuration(currentPositionMs)

    val durationText: String
        get() = FormatUtil.formatDuration(durationMs)

    /**
     * 绑定播放器实例
     */
    fun bind(player: IVideoPlayer) {
        this.player?.release()
        this.player = player
        setupCallbacks()
        playerState = PlayerState.IDLE
    }

    /**
     * 加载视频
     */
    fun loadVideo(path: String) {
        playerState = PlayerState.LOADING
        player?.loadVideo(path)
    }

    /**
     * 播放/暂停切换
     */
    fun togglePlayPause() {
        when (playerState) {
            PlayerState.PLAYING -> pause()
            PlayerState.PAUSED, PlayerState.READY, PlayerState.LOADING -> play()
            PlayerState.COMPLETED -> replay()
            PlayerState.ERROR -> { errorMessage = ""; playerState = PlayerState.IDLE }
            else -> { /* IDLE / RELEASED — no-op */ }
        }
    }

    /**
     * 重新播放 (从头开始)
     */
    fun replay() {
        seekTo(0L)
        play()
    }

    /**
     * 播放
     */
    fun play() {
        player?.play()
    }

    /**
     * 暂停
     */
    fun pause() {
        player?.pause()
    }

    /**
     * 跳转
     */
    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
        currentPositionMs = positionMs
    }

    /**
     * 拖动进度条
     */
    fun seekToProgress(progress: Float) {
        val targetMs = (durationMs * progress.coerceIn(0f, 1f)).toLong()
        seekTo(targetMs)
    }

    /** 设置视频渲染表面 ID（从 VideoRenderView 的 surfaceReady 回调获得） */
    fun setSurfaceId(surfaceId: Long) {
        player?.setNativeSurface(surfaceId)
    }
    /**
     * 切换控制栏显隐
     */
    fun toggleControls() {
        isControlsVisible = !isControlsVisible
    }

    /**
     * 释放资源
     */
    fun release() {
        isProgressUpdating = false
        player?.release()
        player = null
        playerState = PlayerState.RELEASED
    }

    private fun setupCallbacks() {
        player?.onPrepared = {
            playerState = PlayerState.READY
            durationMs = player?.getDuration() ?: 0L
        }

        player?.onProgress = { current, duration ->
            currentPositionMs = current
            durationMs = duration
        }

        player?.onCompletion = {
            playerState = PlayerState.COMPLETED
        }

        player?.onError = { msg ->
            errorMessage = msg
            playerState = PlayerState.ERROR
        }

        player?.onStateChanged = { state ->
            playerState = state
        }
    }
}
