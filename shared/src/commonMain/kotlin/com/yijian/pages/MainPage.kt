package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.views.*
import com.yijian.base.BasePager
import com.yijian.components.TopBar
import com.yijian.components.VideoThumbnail
import com.yijian.model.MediaItem
import com.yijian.model.VideoInfo
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * 主页 — 视频媒体库
 *
 * 功能：
 * - 展示视频列表（网格布局）
 * - 点击视频进入预览页面
 * - 模拟本地视频数据
 */
@Page("MainPage", supportInLocal = true)
internal class MainPage : BasePager() {

    private var videoList by observableList<VideoInfo>()

    override fun created() {
        super.created()
        loadMockData()
    }

    private fun loadMockData() {
        // 模拟视频数据 — 实际项目中通过相册API获取
        val mockVideos = listOf(
            VideoInfo("1", "午后阳光.mp4", "file:///movies/sunset.mp4", duration = 15200L),
            VideoInfo("2", "城市街景.mp4", "file:///movies/street.mp4", duration = 45000L),
            VideoInfo("3", "旅行记录.mp4", "file:///movies/travel.mp4", duration = 120000L),
            VideoInfo("4", "美食制作.mp4", "file:///movies/cooking.mp4", duration = 32000L),
            VideoInfo("5", "宠物日常.mp4", "file:///movies/pet.mp4", duration = 18500L),
            VideoInfo("6", "运动健身.mp4", "file:///movies/sport.mp4", duration = 60000L),
            VideoInfo("7", "音乐演奏.mp4", "file:///movies/music.mp4", duration = 240000L),
            VideoInfo("8", "自然风光.mp4", "file:///movies/nature.mp4", duration = 78000L),
            VideoInfo("9", "教学教程.mp4", "file:///movies/tutorial.mp4", duration = 900000L),
            VideoInfo("10", "活动记录.mp4", "file:///movies/event.mp4", duration = 180000L)
        )
        videoList.addAll(mockVideos)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(YijianColors.background)
            }

            // 顶部导航栏
            TopBar {
                attr {
                    title = "一剪"
                    showBack = false
                    backgroundColor = YijianColors.background
                }
            }

            // 视频列表
            List {
                attr {
                    flex(1f)
                    backgroundColor(YijianColors.background)
                }

                vfor({ ctx.videoList }) { video ->
                    View {
                        attr {
                            flexDirectionColumn()
                            margin(YijianTheme.Spacing.sm)
                            width(pagerData.pageViewWidth / 2 - YijianTheme.Spacing.lg)
                            backgroundColor(YijianColors.surface)
                            borderRadius(YijianTheme.Radius.md)
                            overflowHidden()
                        }
                        event {
                            click {
                                // 跳转到预览页面并传递视频路径
                                val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
                                ctx.jumpPage("PreviewPage", params)
                            }
                        }

                        // 缩略图区域
                        View {
                            attr {
                                width(pagerData.pageViewWidth / 2 - YijianTheme.Spacing.lg)
                                aspectRatio(16f / 9f)
                                backgroundColor(YijianColors.backgroundLight)
                            }

                            Image {
                                attr {
                                    size(width = pagerData.pageViewWidth / 2 - YijianTheme.Spacing.lg,
                                        height = (pagerData.pageViewWidth / 2 - YijianTheme.Spacing.lg) * 9f / 16f)
                                    resizeCover()
                                    backgroundColor(YijianColors.backgroundLight)
                                }
                            }

                            // 播放图标叠加
                            View {
                                attr {
                                    absolutePositionAllZero()
                                    allCenter()
                                }
                                View {
                                    attr {
                                        size(40f, 40f)
                                        borderRadius(20f)
                                        backgroundColor(Color(0x80000000))
                                        allCenter()
                                    }
                                    Text {
                                        attr {
                                            text("▶")
                                            fontSize(18f)
                                            color(YijianColors.textPrimary)
                                        }
                                    }
                                }
                            }

                            // 时长标签
                            View {
                                attr {
                                    absolutePosition(bottom = YijianTheme.Spacing.xs, right = YijianTheme.Spacing.xs)
                                    paddingHorizontal(YijianTheme.Spacing.sm)
                                    paddingVertical(YijianTheme.Spacing.xxs)
                                    backgroundColor(Color(0xCC000000))
                                    borderRadius(YijianTheme.Radius.sm)
                                }
                                Text {
                                    attr {
                                        text(video.formattedDuration)
                                        fontSize(YijianTheme.FontSize.caption)
                                        color(YijianColors.textPrimary)
                                    }
                                }
                            }
                        }

                        // 标题
                        Text {
                            attr {
                                padding(YijianTheme.Spacing.sm)
                                text(video.title)
                                fontSize(YijianTheme.FontSize.small)
                                color(YijianColors.textPrimary)
                                maxLines(1)
                                lineLimit(1)
                            }
                        }
                    }
                }
            }
        }
    }
}
