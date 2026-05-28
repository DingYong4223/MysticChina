package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.player.PlayerController
import com.yijian.player.VideoPlayer
import com.yijian.theme.YijianColors

/**
 * 视频预览页面
 *
 * 功能：
 * - 全屏视频播放
 * - 播放/暂停控制
 * - 进度显示与拖动
 * - 控制栏显隐切换
 * - 返回上一页
 *
 * 参数：
 * - videoPath: 视频文件路径
 * - videoTitle: 视频标题
 */
@Page("PreviewPage", supportInLocal = true)
internal class PreviewPage : BasePager() {

    private val controller = PlayerController()
    private var videoPath by observable("")
    private var videoTitle by observable("")

    override fun created() {
        super.created()
        // 获取传入参数
        videoPath = pagerData.params.optString("videoPath", "")
        videoTitle = pagerData.params.optString("videoTitle", "视频预览")

        // 创建并绑定平台播放器
        setupPlayer()
    }

    private fun setupPlayer() {
        // 根据平台创建对应的播放器实现
        // 在完整实现中，这里需要通过 expect/actual 或依赖注入创建
        // val player = createPlatformPlayer()
        // controller.bind(player)

        // 当前使用模拟实现
        if (videoPath.isNotEmpty()) {
            controller.loadVideo(videoPath)
        }
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(YijianColors.background)
            }

            // 全屏视频播放器
            VideoPlayer {
                attr {
                    videoPath = ctx.videoPath
                    controller = ctx.controller
                }
                event {
                    onBackClick = {
                        ctx.controller.release()
                        ctx.closePage()
                    }
                }
            }
        }
    }

    override fun viewDestroyed() {
        super.viewDestroyed()
        controller.release()
    }
}
