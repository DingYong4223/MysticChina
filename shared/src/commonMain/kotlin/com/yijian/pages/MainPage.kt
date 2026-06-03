package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.tencent.kuikly.core.views.compose.Button
import com.yijian.base.BasePager
import com.yijian.model.VideoInfo
import com.yijian.module.GalleryModule
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme
import com.yijian.util.fileExists

private const val TAG = "MainPage"

/**
 * 主页 — 视频媒体库
 */
@Page("MainPage", supportInLocal = true)
internal class MainPage : BasePager() {

    private var errorMessage by observable("")


    private var videoList by observableList<VideoInfo>()
    private var _nextId = 0L
    private val gallery: GalleryModule by lazy { acquireModule(GalleryModule.MODULE_NAME) }

    private fun nextId(): String = (++_nextId).toString()

    override fun created() {
        super.created()
        KLog.i(TAG, "MainPage created — 加载测试数据")
        addTestVideos()
        KLog.i(TAG, "视频列表数量: ${videoList.size}")
    }

    override fun pageDidAppear() {
        super.pageDidAppear()
        KLog.i(TAG, "MainPage 已显示 (${videoList.size} 个视频)")
    }

    private fun addTestVideos() {
        KLog.d(TAG, "添加 4 条测试视频")
        videoList.addAll(listOf(
            VideoInfo("1", "午后阳光.mp4", "test1", duration = 15200L),
            VideoInfo("2", "城市街景.mp4", "test2", duration = 45000L),
            VideoInfo("3", "旅行记录.mp4", "test3", duration = 120000L),
            VideoInfo("4", "美食制作.mp4", "test4", duration = 32000L),
        ))
    }

    private fun onImportClick() {
        KLog.i(TAG, "点击导入按钮 → 打开系统文件选择器")
        gallery.pickVideo { result ->
            val res = result ?: run { KLog.d(TAG, "导入取消 (result=null)"); return@pickVideo }
            val path = res.optString("path", "")
            if (res.optBoolean("cancelled", false) || path.isEmpty()) {
                KLog.d(TAG, "导入取消")
                return@pickVideo
            }
            val name = res.optString("name", "未知.mp4")
            KLog.i(TAG, "导入成功: $name ($path)")
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
            errorMessage = "视频不存在: ${video.title}"
            return
        }
        KLog.i(TAG, "点击视频: ${video.title} → 跳转 EditorPage")
        val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
        jumpPage("EditorPage", params)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr { backgroundColor(YijianColors.background) }

            // —— 错误 Toast ——
            if (ctx.errorMessage.isNotEmpty()) {
                View {
                    attr {
                        absolutePosition(top = 80f, left = 20f, right = 20f); zIndex(100)
                        padding(top = 12f, bottom = 12f, left = 16f, right = 16f)
                        backgroundColor(Color(0xE8FF3B30)); borderRadius(10f)
                        flexDirectionRow(); alignItemsCenter()
                    }
                    event { click { ctx.errorMessage = "" } }
                    View { attr { size(20f, 20f); borderRadius(10f); backgroundColor(Color(0xFFFFFFFF)); allCenter(); marginRight(10f) }
                        Text { attr { text("!"); fontSize(13f); color(Color(0xFFFF3B30)); fontWeightBold() } } }
                    Text { attr { text(ctx.errorMessage); fontSize(13f); color(Color(0xFFFFFFFF)); flex(1f) } }
                    View { attr { size(24f, 24f); allCenter() }
                        Text { attr { text("✕"); fontSize(14f); color(Color(0xAAFFFFFF)) } } }
                }
            }
            // 顶部导航
            View {
                attr {
                    height(YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(YijianColors.background)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                    padding(left = YijianTheme.Spacing.lg, right = YijianTheme.Spacing.lg)
                }
                Text {
                    attr {
                        text("一剪")
                        fontSize(YijianTheme.FontSize.title)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                    }
                }
            }

            // 视频列表
            List {
                attr {
                    flex(1f)
                }
                View {
                    attr {
                        flexDirectionRow()
                        flexWrapWrap()
                        padding(all = YijianTheme.Spacing.sm)
                    }
                    vfor({ ctx.videoList }) { video ->
                        val cardWidth = ctx.pagerData.pageViewWidth / 2 - YijianTheme.Spacing.md
                        View {
                            attr {
                                size(cardWidth, cardWidth * 9f / 16f + 40f)
                                flexDirectionColumn()
                                margin(YijianTheme.Spacing.xs)
                                backgroundColor(YijianColors.surface)
                                borderRadius(YijianTheme.Radius.md)
                                overflow(true)
                            }
                            event { click { ctx.onVideoClick(video) } }

                            // 缩略图区
                            View {
                                attr {
                                    size(cardWidth, cardWidth * 9f / 16f)
                                    backgroundColor(YijianColors.backgroundLight)
                                    allCenter()
                                }
                                View {
                                    attr {
                                        size(44f, 44f)
                                        borderRadius(22f)
                                        backgroundColor(Color(0x80000000))
                                        allCenter()
                                    }
                                    Text {
                                        attr {
                                            text("▶")
                                            fontSize(20f)
                                            color(YijianColors.textPrimary)
                                        }
                                    }
                                }
                                if (video.duration > 0) {
                                    View {
                                        attr {
                                            absolutePosition(bottom = 6f, right = 6f)
                                            padding(left = 6f, right = 6f, top = 2f, bottom = 2f)
                                            backgroundColor(Color(0xCC000000))
                                            borderRadius(4f)
                                        }
                                        Text {
                                            attr {
                                                text(video.formattedDuration)
                                                fontSize(11f)
                                                color(YijianColors.textPrimary)
                                            }
                                        }
                                    }
                                }
                            }

                            // 标题行
                            View {
                                attr {
                                    flex(1f)
                                    padding(all = YijianTheme.Spacing.sm)
                                    justifyContentCenter()
                                }
                                Text {
                                    attr {
                                        text(video.title)
                                        fontSize(YijianTheme.FontSize.small)
                                        color(YijianColors.textPrimary)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 底部按钮
            View {
                attr {
                    height(70f)
                    backgroundColor(YijianColors.surfaceLight)
                    allCenter()
                }
                Button {
                    attr {
                        size(width = 220f, height = 44f)
                        borderRadius(YijianTheme.Radius.round)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        titleAttr {
                            text("🎬 添加视频")
                            fontSize(16f)
                            color(YijianColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                    event { click { ctx.onImportClick() } }
                }
            }
        }
    }
}

// ── 错误 Toast ——
private fun ViewContainer<*, *>.ErrorToast(message: String, onDismiss: () -> Unit) {
    View {
        attr {
            absolutePosition(top = 80f, left = 20f, right = 20f)
            zIndex(100)
            padding(top = 12f, bottom = 12f, left = 16f, right = 16f)
            backgroundColor(Color(0xE8FF3B30))
            borderRadius(10f)
            flexDirectionRow()
            alignItemsCenter()
        }
        event { click { onDismiss.invoke() } }

        View { attr { size(20f, 20f); borderRadius(10f); backgroundColor(Color(0xFFFFFFFF)); allCenter(); marginRight(10f) }
            Text { attr { text("!"); fontSize(13f); color(Color(0xFFFF3B30)); fontWeightBold() } } }

        Text { attr { text(message); fontSize(13f); color(Color(0xFFFFFFFF)); flex(1f) } }

        View { attr { size(24f, 24f); allCenter() }
            Text { attr { text("✕"); fontSize(14f); color(Color(0xAAFFFFFF)) } } }
    }
}