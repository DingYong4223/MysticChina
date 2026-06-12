package com.fula.mysticchina

/**
 * JS / Web 平台入口占位
 *
 * Kuikly JS 运行依赖于 core-render-web 渲染器。
 * 当前使用已发布库 (v2.4.0-1.9.22) 暂不含 JS 产物；
 * 需切换到 KuiklyUI 源码编译以获得完整 JS 支持。
 *
 * 启用方式：
 *   1. settings.gradle.kts 中添加 includeBuild("../KuiklyUI")
 *   2. 将 commonMain dependencies 改为 project(":core"), project(":compose")
 *   3. jsMain dependencies 中添加 core-render-web
 */
object WebPlatform {
    const val RENDERER = "core-render-web"
}