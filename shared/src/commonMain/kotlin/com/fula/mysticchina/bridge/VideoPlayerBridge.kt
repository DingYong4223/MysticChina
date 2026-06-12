package com.fula.mysticchina.bridge

/**
 * 视频播放器桥接接口 — 定义 Kuikly 层与原生播放器之间的通信协议。
 *
 * 通过 Kuikly 的 expand-native-api 机制，platform 模块（Android/iOS）
 * 实现此接口并将实例注册到渲染管线。commonMain 通过此接口间接操控原生播放器。
 */
interface VideoPlayerBridge {

    /** 初始化并加载视频 */
    fun loadVideo(path: String)

    /** 开始播放 */
    fun play()

    /** 暂停播放 */
    fun pause()

    /** 跳转到指定时间点（毫秒） */
    fun seekTo(positionMs: Long)

    /** 设置音量 (0.0 ~ 1.0) */
    fun setVolume(volume: Float)

    /** 设置播放速率 (0.5x / 1.0x / 1.5x / 2.0x) */
    fun setSpeed(speed: Float)

    /** 设置是否循环播放 */
    fun setLooping(looping: Boolean)

    /** 获取当前播放位置（毫秒） */
    fun getCurrentPosition(): Long

    /** 获取视频总时长（毫秒），未加载返回 0 */
    fun getDuration(): Long

    /** 是否正在播放 */
    fun isPlaying(): Boolean

    /** 释放播放器资源 */
    fun release()

    /** 获取视频尺寸 (width, height)，未加载返回 (0, 0) */
    fun getVideoSize(): Pair<Int, Int>

    // ============ 回调注册 ============

    /** 视频准备完成 */
    fun onPrepared(callback: () -> Unit)

    /** 播放进度回调（毫秒） */
    fun onProgressUpdate(callback: (currentMs: Long, durationMs: Long) -> Unit)

    /** 播放完成 */
    fun onCompletion(callback: () -> Unit)

    /** 发生错误 */
    fun onError(callback: (message: String) -> Unit)

    /** 缓冲状态变化 (isBuffering: Boolean) */
    fun onBuffering(callback: (Boolean) -> Unit)
}