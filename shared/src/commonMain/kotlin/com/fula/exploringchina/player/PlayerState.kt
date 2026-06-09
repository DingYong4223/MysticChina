package com.fula.exploringchina.player

/**
 * 播放器状态枚举
 */
enum class PlayerState {
    /** 初始状态 */
    IDLE,
    /** 加载中 */
    LOADING,
    /** 准备就绪 */
    READY,
    /** 播放中 */
    PLAYING,
    /** 暂停中 */
    PAUSED,
    /** 播放完成 */
    COMPLETED,
    /** 错误状态 */
    ERROR,
    /** 已释放 */
    RELEASED
}
