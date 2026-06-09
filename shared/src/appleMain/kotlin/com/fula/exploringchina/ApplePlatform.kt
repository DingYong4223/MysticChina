package com.fula.exploringchina

/**
 * Apple 平台 (iOS + macOS) 共享工具
 *
 * 此源集被 iosMain 和 macOS 目标共用。
 * 当有 iOS/macOS 共同的平台代码时，应放在这里而非重复。
 */
object ApplePlatform {
    /** iOS or macOS — both use appleMain source set */
    const val IS_APPLE = true
}