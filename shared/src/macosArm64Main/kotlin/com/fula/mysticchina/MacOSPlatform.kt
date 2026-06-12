package com.fula.mysticchina

/**
 * macOS 平台特定实现占位
 *
 * macOS 与 iOS 共享 appleMain 源集中的代码。
 * macosX64 (Intel Mac) 和 macosArm64 (Apple Silicon Mac)
 * 均可通过此文件或 appleMain 使用 Apple 平台功能。
 */
object MacOSPlatform {
    const val TARGET = "macOS"
}