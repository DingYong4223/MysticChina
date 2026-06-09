package com.fula.exploringchina

import com.fula.exploringchina.util.Constants

/**
 * 应用入口 — 路由配置
 *
 * 集中管理所有页面的路由常量、跳转参数 keys 和启动配置。
 * 所有页面通过 @Page 注解由 KSP 自动注册。
 */
object App {

    /** 启动页面（默认首页） */
    const val START_PAGE = Constants.PAGE_SPLASH

    /** 页面间跳转参数 keys */
    object Param {
        /** 视频文件路径 */
        const val VIDEO_PATH = "videoPath"
        /** 视频标题 */
        const val VIDEO_TITLE = "videoTitle"
        /** 视频 ID */
        const val VIDEO_ID = "videoId"
        /** 媒体过滤类型 (all/video/image) */
        const val MEDIA_TYPE = "mediaType"
        /** 搜索关键词 */
        const val SEARCH_KEYWORD = "searchKeyword"
    }

    /** 全局配置 */
    object Config {
        /** 自动隐藏控制栏超时（毫秒） */
        const val CONTROL_AUTO_HIDE_MS = 3000L
        /** 启动页停留时间（毫秒） */
        const val SPLASH_DURATION_MS = 1500L
        /** 缩略图网格列数 */
        const val GRID_COLUMNS = 2
    }
}