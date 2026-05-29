package com.yijian.player

import android.media.MediaPlayer
import java.io.File
import java.util.Timer
import kotlin.concurrent.timer

/**
 * Android 平台视频播放器 — 基于系统 MediaPlayer 实现 IVideoPlayer 接口。
 *
 * 生命周期管理：
 *   IDLE → loadVideo() → LOADING → onPrepared → READY
 *   READY → play() → PLAYING → pause() → PAUSED → play() → PLAYING
 *   PLAYING → onCompletion → COMPLETED → play() → PLAYING
 *   COMPLETED → seekTo(0)+play() → PLAYING (loop)
 */
class AndroidVideoPlayer : IVideoPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var state = PlayerState.IDLE
    private var progressTimer: Timer? = null
    private var _looping = false

    override var onPrepared: (() -> Unit)? = null
    override var onProgress: ((Long, Long) -> Unit)? = null
    override var onCompletion: (() -> Unit)? = null
    override var onError: ((String) -> Unit)? = null
    override var onStateChanged: ((PlayerState) -> Unit)? = null

    override fun loadVideo(path: String) {
        releaseInternal()
        if (!File(path).exists()) {
            onError?.invoke("文件不存在: $path")
            return
        }
        setState(PlayerState.LOADING)
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                setOnPreparedListener { mp ->
                    setState(PlayerState.READY)
                    onPrepared?.invoke()
                }
                setOnCompletionListener {
                    if (_looping) {
                        seekTo(0)
                        start()
                    } else {
                        setState(PlayerState.COMPLETED)
                        onCompletion?.invoke()
                    }
                }
                setOnErrorListener { _, what, extra ->
                    setState(PlayerState.ERROR)
                    onError?.invoke("播放错误: what=$what extra=$extra")
                    true
                }
                setOnVideoSizeChangedListener { _, width, height ->
                    // 视频尺寸变化（可在此处理渲染区域适配）
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            setState(PlayerState.ERROR)
            onError?.invoke("加载失败: ${e.message}")
        }
    }

    override fun play() {
        mediaPlayer?.takeIf { !it.isPlaying }?.let {
            it.start()
            setState(PlayerState.PLAYING)
            startProgressTimer()
        }
    }

    override fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.let {
            it.pause()
            setState(PlayerState.PAUSED)
            stopProgressTimer()
        }
    }

    override fun seekTo(positionMs: Long) {
        mediaPlayer?.seekTo(positionMs.toInt())
    }

    override fun setVolume(volume: Float) {
        val vol = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(vol, vol)
    }

    override fun setLooping(looping: Boolean) {
        _looping = looping
    }

    override fun getCurrentPosition(): Long =
        try { mediaPlayer?.currentPosition?.toLong() ?: 0L } catch (e: Exception) { 0L }

    override fun getDuration(): Long =
        try { mediaPlayer?.duration?.toLong() ?: 0L } catch (e: Exception) { 0L }

    override fun getState(): PlayerState = state

    override fun release() {
        releaseInternal()
        setState(PlayerState.RELEASED)
    }

    private fun setState(newState: PlayerState) {
        state = newState
        onStateChanged?.invoke(newState)
    }

    private fun startProgressTimer() {
        stopProgressTimer()
        progressTimer = timer("player-progress", false, 0, 250) {
            val pos = getCurrentPosition()
            val dur = getDuration()
            onProgress?.invoke(pos, dur)
        }
    }

    private fun stopProgressTimer() {
        progressTimer?.cancel()
        progressTimer = null
    }

    private fun releaseInternal() {
        stopProgressTimer()
        mediaPlayer?.apply {
            if (isPlaying) stop()
            reset()
            release()
        }
        mediaPlayer = null
    }
}