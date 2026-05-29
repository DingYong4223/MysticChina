package com.yijian.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.directives.vfor
import com.tencent.kuikly.core.reactive.handler.observableList
import com.tencent.kuikly.core.timer.setTimeout
import com.tencent.kuikly.core.views.*
import com.tencent.kuikly.core.views.compose.Button
import com.yijian.base.BasePager
import com.yijian.model.VideoInfo
import com.yijian.module.GalleryModule
import com.yijian.theme.YijianColors
import com.yijian.theme.YijianTheme

/**
 * 主页 — 视频媒体库 (剪映风格)
 *
 * 布局:
 * ┌──────────────────────────┐
 * │  TopBar: 一剪             │
 * ├──────────────────────────┤
 * │                          │
 * │    ┌──────────────┐      │
 * │    │  🎬 导入视频  │      │  ← 空状态 / 按钮区域
 * │    └──────────────┘      │
 * │                          │
 * │  视频网格列表 (2列)       │
 * │  ┌──────┐ ┌──────┐      │
 * │  │ 📹   │ │ 📹   │      │
 * │  │title │ │title │      │
 * │  └──────┘ └──────┘      │
 * ├──────────────────────────┤
 * │ BottomBar: 草稿箱 | 导入  │
 * └──────────────────────────┘
 */
@Page("MainPage", supportInLocal = true)
internal class MainPage : BasePager() {

    private var videoList by observableList<VideoInfo>()
    private var _nextId = 0L
    private val gallery: GalleryModule by lazy { acquireModule(GalleryModule.MODULE_NAME) }

    private fun nextId(): String = (++_nextId).toString()

    override fun created() {
        super.created()
    }

    /** 点击导入 — 打开系统文件选择器 */
    private fun onImportClick() {
        gallery.pickVideo { result ->
            val res = result ?: return@pickVideo
            val path = res.optString("path", "")
            if (res.optBoolean("cancelled", false) || path.isEmpty()) return@pickVideo
            videoList.add(
                VideoInfo(
                    id = nextId(),
                    title = res.optString("name", "未知.mp4"),
                    path = path,
                    fileSize = res.optLong("size", 0L)
                )
            )
        }
    }

    /** 点击视频 — 跳转预览页 */
    private fun onVideoClick(video: VideoInfo) {
        val params = """{"videoPath":"${video.path}","videoTitle":"${video.title}","videoId":"${video.id}"}"""
        jumpPage("PreviewPage", params)
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(YijianColors.background)
                flexDirectionColumn()
                size(pagerData.pageViewWidth, pagerData.pageViewHeight)
            }

            // ── 顶部导航 ──
            View {
                attr {
                    size(pagerData.pageViewWidth, YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                    backgroundColor(YijianColors.background)
                    flexDirectionRow()
                    alignItemsCenter()
                    paddingTop(pagerData.statusBarHeight)
                }
                Text {
                    attr {
                        flex(1f)
                        text("一剪")
                        fontSize(YijianTheme.FontSize.title)
                        color(YijianColors.textPrimary)
                        fontWeightBold()
                        marginLeft(YijianTheme.Spacing.lg)
                    }
                }
            }

            // ── 内容区 ──
            if (ctx.videoList.isEmpty()) {
                // 空状态 — 引导用户导入视频
                View {
                    attr {
                        flex(1f)
                        allCenter()
                        flexDirectionColumn()
                    }
                    Text {
                        attr {
                            text("🎬")
                            fontSize(48f)
                            marginBottom(YijianTheme.Spacing.lg)
                        }
                    }
                    Text {
                        attr {
                            text("还没有视频")
                            fontSize(YijianTheme.FontSize.subtitle)
                            color(YijianColors.textSecondary)
                            marginBottom(YijianTheme.Spacing.sm)
                        }
                    }
                    Text {
                        attr {
                            text("点击下方按钮导入你的第一条视频")
                            fontSize(YijianTheme.FontSize.body)
                            color(YijianColors.textTertiary)
                            marginBottom(YijianTheme.Spacing.xl)
                        }
                    }
                }
            } else {
                // 有内容 — 显示列表
                List {
                    attr {
                        flex(1f)
                        backgroundColor(YijianColors.background)
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
                                    width(cardWidth)
                                    flexDirectionColumn()
                                    margin(YijianTheme.Spacing.xs)
                                    backgroundColor(YijianColors.surface)
                                    borderRadius(YijianTheme.Radius.md)
                                    overflow(true)
                                }
                                event {
                                    click { ctx.onVideoClick(video) }
                                }

                                // 缩略图占位
                                View {
                                    attr {
                                        width(cardWidth)
                                        height(cardWidth * 9f / 16f)
                                        backgroundColor(YijianColors.backgroundLight)
                                        allCenter()
                                    }
                                    // 播放图标
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
                                    // 时长标签
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
                                        padding(all = YijianTheme.Spacing.sm)
                                        flexDirectionRow()
                                        alignItemsCenter()
                                        justifyContentSpaceBetween()
                                    }
                                    Text {
                                        attr {
                                            flex(1f)
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
            }

            // ── 底部工具栏 ──
            View {
                attr {
                    size(pagerData.pageViewWidth, YijianTheme.BarHeight.bottomBar + 20f)
                    backgroundColor(YijianColors.surfaceLight)
                    flexDirectionRow()
                    alignItemsCenter()
                    justifyContentCenter()
                    paddingBottom(20f)
                }
                Button {
                    attr {
                        size(width = 200f, height = 44f)
                        borderRadius(YijianTheme.Radius.round)
                        backgroundLinearGradient(
                            Direction.TO_RIGHT,
                            ColorStop(YijianColors.gradientStart, 0f),
                            ColorStop(YijianColors.gradientEnd, 1f)
                        )
                        titleAttr {
                            text(if (ctx.videoList.isEmpty()) "🎬 导入第一个视频" else "🎬 添加视频")
                            fontSize(16f)
                            color(YijianColors.textPrimary)
                            fontWeightBold()
                        }
                    }
                    event {
                        click { ctx.onImportClick() }
                    }
                }
            }
        }
    }
}