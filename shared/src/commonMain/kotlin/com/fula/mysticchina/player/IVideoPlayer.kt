package com.fula.mysticchina.player

/**
 * 平台无关的视频播放器接口
 * Android 实现使用 MediaPlayer/ExoPlayer
 * iOS 实现使用 AVPlayer
 */
interface IVideoPlayer {

    /** 加载视频 */
    fun loadVideo(path: String)

    /** 开始/继续播放 */
    fun play()

    /** 暂停播放 */
    fun pause()

    /** 跳转到指定位置（毫秒） */
    fun seekTo(positionMs: Long)

    /** 设置音量 0.0 ~ 1.0 */
    fun setVolume(volume: Float)

    /** 设置是否循环播放 */
    fun setLooping(looping: Boolean)

    /** 获取当前播放位置（毫秒） */
    fun getCurrentPosition(): Long

    /** 获取视频总时长（毫秒） */
    fun getDuration(): Long

    /** 获取当前播放状态 */
    fun getState(): PlayerState

    /** 释放播放器资源 */
    fun release()

    /**
     * 设置原生视频渲染表面（平台桥接）。
     * @param surfaceId 由 VideoRenderView 的 surfaceReady 事件提供的表面 ID，
     *   各平台 actual 实现负责解析为对应原生 Surface。
     */
    fun setNativeSurface(surfaceId: Long) {}

    // ============ 回调 ============

    /** 视频准备完成回调 */
    var onPrepared: (() -> Unit)?

    /** 播放进度回调（毫秒） */
    var onProgress: ((currentMs: Long, durationMs: Long) -> Unit)?

    /** 播放完成回调 */
    var onCompletion: (() -> Unit)?

    /** 错误回调 */
    var onError: ((message: String) -> Unit)?

    /** 状态变化回调 */
    var onStateChanged: ((PlayerState) -> Unit)?
}
