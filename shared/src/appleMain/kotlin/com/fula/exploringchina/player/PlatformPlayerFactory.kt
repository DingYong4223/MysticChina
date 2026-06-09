package com.fula.exploringchina.player

/**
 * Apple 平台共享的播放器工厂 (非 actual — 各目标在自身 source set 中提供实际实现)。
 */
// actual 声明由各平台 source set 提供：iosMain → IOSVideoPlayer, macOS → stub