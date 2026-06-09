package com.fula.exploringchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.exploringchina.App
import com.fula.exploringchina.base.BasePager
import com.fula.exploringchina.components.ProgressBar
import com.fula.exploringchina.player.*
import com.fula.exploringchina.theme.YijianColors
import com.fula.exploringchina.theme.YijianTheme

/**
 * 视频预览页面 — 全屏播放器
 *
 * 功能：
 * - 全屏视频渲染
 * - 播放/暂停控制
 * - 进度显示与拖动
 * - 控制栏 3s 自动隐藏
 * - 播放完成 → 显示重播按钮
 * - 返回上一页
 *
 * @Page("PreviewPage", supportInLocal = true)
 */
@Page("PreviewPage", supportInLocal = true)
internal class PreviewPage : BasePager() {

    private val controller = PlayerController()
    private var videoPath by observable("")
    private var videoTitle by observable("视频预览")
    private var isControlVisible by observable(true)

    override fun created() {
        super.created()
        videoPath = pageData.params.optString(App.Param.VIDEO_PATH, "")
        videoTitle = pageData.params.optString(App.Param.VIDEO_TITLE, "视频预览")

        // 创建平台适配的播放器并绑定
        setupPlayer()
    }

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                backgroundColor(YijianColors.background)
                size(pagerData.pageViewWidth, pagerData.pageViewHeight)
            }

            // 视频画面区域
            View {
                attr {
                    absolutePositionAllZero()
                    backgroundColor(YijianColors.background)
                }
                event {
                    click { ctx.controller.toggleControls() }
                }
            }

            // 中央大播放按钮 — 暂停/准备好/完成时显示
            if (ctx.controller.playerState == PlayerState.PAUSED
                || ctx.controller.playerState == PlayerState.READY
                || ctx.controller.playerState == PlayerState.COMPLETED
            ) {
                View {
                    attr {
                        absolutePositionAllZero()
                        allCenter()
                    }
                    event {
                        click { ctx.controller.togglePlayPause() }
                    }
                    View {
                        attr {
                            size(64f, 64f)
                            borderRadius(32f)
                            backgroundColor(Color(0x80000000))
                            allCenter()
                        }
                        Text {
                            attr {
                                text(
                                    if (ctx.controller.playerState == PlayerState.COMPLETED) "↻"
                                    else "▶"
                                )
                                fontSize(30f)
                                color(YijianColors.textPrimary)
                            }
                        }
                    }
                }
            }

            // 控制栏 — 顶部 + 底部
            View {
                attr {
                    absolutePositionAllZero()
                }

                // 顶部：返回 + 标题
                View {
                    attr {
                        absolutePosition(top = 0f, left = 0f, right = 0f)
                        height(YijianTheme.BarHeight.topBar + pagerData.statusBarHeight)
                        backgroundLinearGradient(
                            Direction.TO_BOTTOM,
                            ColorStop(Color(0xCC000000), 0f),
                            ColorStop(Color(0x00000000), 1f)
                        )
                        flexDirectionRow()
                        alignItemsCenter()
                        paddingTop(pagerData.statusBarHeight)
                    }
                    // 返回
                    View {
                        attr {
                            size(YijianTheme.BarHeight.topBar, YijianTheme.BarHeight.topBar)
                            allCenter()
                        }
                        event {
                            click {
                                ctx.controller.release()
                                ctx.closePage()
                            }
                        }
                        Text {
                            attr {
                                text("<")
                                fontSize(22f)
                                color(YijianColors.textPrimary)
                            }
                        }
                    }
                    Text {
                        attr {
                            flex(1f)
                            text(ctx.videoTitle)
                            fontSize(YijianTheme.FontSize.subtitle)
                            color(YijianColors.textPrimary)
                            textAlignCenter()
                            marginRight(YijianTheme.BarHeight.topBar)
                        }
                    }
                }

                // 底部：进度条 + 时间 + 播放/暂停
                View {
                    attr {
                        absolutePosition(bottom = 0f, left = 0f, right = 0f)
                        height(80f)
                        backgroundLinearGradient(
                            Direction.TO_TOP,
                            ColorStop(Color(0xCC000000), 0f),
                            ColorStop(Color(0x00000000), 1f)
                        )
                        flexDirectionColumn()
                        justifyContentFlexEnd()
                        paddingBottom(YijianTheme.Spacing.lg)
                    }

                    // 进度行
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                            padding(
                                left = YijianTheme.Spacing.lg,
                                right = YijianTheme.Spacing.lg
                            )
                            marginBottom(YijianTheme.Spacing.sm)
                        }
                        Text {
                            attr {
                                text(ctx.controller.currentTimeText)
                                fontSize(YijianTheme.FontSize.caption)
                                color(YijianColors.textSecondary)
                                marginRight(YijianTheme.Spacing.sm)
                            }
                        }
                        View {
                            attr { flex(1f); height(20f); justifyContentCenter() }
                            ProgressBar {
                                attr {
                                    progress = ctx.controller.currentProgress
                                    barHeight = 3f
                                    showThumb = true
                                }
                            }
                        }
                        Text {
                            attr {
                                text(ctx.controller.durationText)
                                fontSize(YijianTheme.FontSize.caption)
                                color(YijianColors.textSecondary)
                                marginLeft(YijianTheme.Spacing.sm)
                            }
                        }
                    }

                    // 播放/暂停按钮行
                    View {
                        attr {
                            allCenter()
                            flexDirectionRow()
                        }
                        View {
                            attr {
                                size(48f, 48f)
                                allCenter()
                            }
                            event {
                                click { ctx.controller.togglePlayPause() }
                            }
                            Text {
                                attr {
                                    text(
                                        when (ctx.controller.playerState) {
                                            PlayerState.PLAYING -> "⏸"
                                            PlayerState.COMPLETED -> "↻"
                                            else -> "▶"
                                        }
                                    )
                                    fontSize(28f)
                                    color(YijianColors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }

            // 错误提示
            if (ctx.controller.playerState == PlayerState.ERROR && ctx.controller.errorMessage.isNotEmpty()) {
                View {
                    attr {
                        absolutePositionAllZero()
                        allCenter()
                    }
                    View {
                        attr {
                            padding(all = YijianTheme.Spacing.xl)
                            backgroundColor(Color(0xCC000000))
                            borderRadius(YijianTheme.Radius.lg)
                            allCenter()
                            flexDirectionColumn()
                        }
                        Text {
                            attr {
                                text("⚠ ${ctx.controller.errorMessage}")
                                fontSize(YijianTheme.FontSize.body)
                                color(YijianColors.error)
                                marginBottom(YijianTheme.Spacing.md)
                            }
                        }
                        View {
                            attr {
                                height(36f)
                                padding(left = YijianTheme.Spacing.xl, right = YijianTheme.Spacing.xl)
                                backgroundColor(YijianColors.primary)
                                borderRadius(YijianTheme.Radius.round)
                                allCenter()
                            }
                            event {
                                click {
                                    ctx.setupPlayer()
                                }
                            }
                            Text {
                                attr {
                                    text("重试")
                                    fontSize(YijianTheme.FontSize.body)
                                    color(YijianColors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /** 创建平台播放器并绑定到控制器 */
    private fun setupPlayer() {
        // Android: MediaPlayer, iOS: AVPlayer — 通过 Kuikly 桥接层创建
        val player = com.fula.exploringchina.player.PlatformPlayerFactory.createPlayer()
        controller.bind(player)
        if (videoPath.isNotEmpty()) {
            controller.loadVideo(videoPath)
        }
    }

    override fun viewDestroyed() {
        super.viewDestroyed()
        controller.release()
    }
}