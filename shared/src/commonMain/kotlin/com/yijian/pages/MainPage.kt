package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.*
import com.tencent.kuikly.core.views.compose.Button
import com.yijian.base.BasePager
import com.yijian.model.VideoInfo
import com.yijian.module.GalleryModule
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme
import com.yijian.util.fileExists

private const val TAG = "MainPage"

@Page("MainPage", supportInLocal = true)
internal class MainPage : BasePager() {

    private var videoList by observableList<VideoInfo>()
    private var errorMessages by observableList<String>()
    private var _nextId = 0L
    private val gallery: GalleryModule by lazy { acquireModule(GalleryModule.MODULE_NAME) }

    private fun nextId(): String = (++_nextId).toString()

    override fun created() {
        super.created()
        addTestVideos()
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
    }

    private fun addTestVideos() {
        videoList.addAll(listOf(
            VideoInfo("real1", "test.mp4", "/data/data/com.yijian.android/files/screen_test.mp4", duration = 3000L),
            VideoInfo("fake1", "不存在的视频.mp4", "nonexistent_path", duration = 15000L),
        ))
    }

    private fun onImportClick() {
        gallery.pickVideo { result ->
            val res = result ?: return@pickVideo
            val path = res.optString("path", "")
            if (res.optBoolean("cancelled", false) || path.isEmpty()) return@pickVideo
            videoList.add(VideoInfo(
                id = nextId(),
                title = res.optString("name", "未知.mp4"),
                path = path,
                fileSize = res.optLong("size", 0L)
            ))
        }
    }

    private fun onVideoClick(video: VideoInfo) {
        if (!fileExists(video.path)) {
            KLog.e(TAG, "视频不存在: ${video.path}")
            errorMessages.add("视频不存在: ${video.title}")
            return
        }
        val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
        jumpPage("EditorPage", params)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background) }

            // 顶部导航
            View {
                attr {
                    height(YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(YijianColors.background); flexDirectionRow(); alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    padding(left = YijianTheme.Spacing.lg, right = YijianTheme.Spacing.lg)
                }
                Text { attr { text("一剪"); fontSize(YijianTheme.FontSize.title); color(YijianColors.textPrimary); fontWeightBold() } }
            }

            // 错误提示栏（顶部导航和视频列表之间）
            vfor({ ctx.errorMessages }) { msg ->
                View {
                    attr {
                        height(44f)
                        padding(top = 12f, bottom = 12f, left = 16f, right = 16f)
                        backgroundColor(Color(0xE8FF3B30)); flexDirectionRow(); alignItemsCenter()
                    }
                    event { click { ctx.errorMessages.clear() } }
                    View { attr { size(20f, 20f); borderRadius(10f); backgroundColor(Color(0xFFFFFFFF)); allCenter(); marginRight(10f) }
                        Text { attr { text("!"); fontSize(13f); color(Color(0xFFFF3B30)); fontWeightBold() } } }
                    Text { attr { text(msg); fontSize(13f); color(Color(0xFFFFFFFF)); flex(1f) } }
                    View { attr { size(24f, 24f); allCenter() }
                        Text { attr { text("✕"); fontSize(14f); color(Color(0xAAFFFFFF)) } } }
                }
            }

            // 视频列表
            List {
                attr { flex(1f) }
                View {
                    attr { flexDirectionRow(); flexWrapWrap(); padding(all = YijianTheme.Spacing.sm) }
                    vfor({ ctx.videoList }) { video ->
                        val cw = ctx.pagerData.pageViewWidth / 2 - YijianTheme.Spacing.md
                        View {
                            attr {
                                size(cw, cw * 9f / 16f + 40f); flexDirectionColumn(); margin(YijianTheme.Spacing.xs)
                                backgroundColor(YijianColors.surface); borderRadius(YijianTheme.Radius.md); overflow(true)
                            }
                            event { click { ctx.onVideoClick(video) } }
                            View {
                                attr { size(cw, cw * 9f / 16f); backgroundColor(YijianColors.backgroundLight); allCenter() }
                                View { attr { size(44f, 44f); borderRadius(22f); backgroundColor(Color(0x80000000)); allCenter() }
                                    Text { attr { text("▶"); fontSize(20f); color(YijianColors.textPrimary) } } }
                                if (video.duration > 0) {
                                    View { attr { absolutePosition(bottom = 6f, right = 6f); padding(left = 6f, right = 6f, top = 2f, bottom = 2f); backgroundColor(Color(0xCC000000)); borderRadius(4f) }
                                        Text { attr { text(video.formattedDuration); fontSize(11f); color(YijianColors.textPrimary) } } }
                                }
                            }
                            View { attr { flex(1f); padding(all = YijianTheme.Spacing.sm); justifyContentCenter() }
                                Text { attr { text(video.title); fontSize(YijianTheme.FontSize.small); color(YijianColors.textPrimary) } } }
                        }
                    }
                }
            }

            // 底部按钮
            View { attr { height(70f); backgroundColor(YijianColors.surfaceLight); allCenter() }
                Button {
                    attr {
                        size(width = 220f, height = 44f); borderRadius(YijianTheme.Radius.round)
                        backgroundLinearGradient(Direction.TO_RIGHT, ColorStop(YijianColors.gradientStart, 0f), ColorStop(YijianColors.gradientEnd, 1f))
                        titleAttr { text("🎬 添加视频"); fontSize(16f); color(YijianColors.textPrimary); fontWeightBold() }
                    }
                    event { click { ctx.onImportClick() } }
                }
            }
        }
    }
}
