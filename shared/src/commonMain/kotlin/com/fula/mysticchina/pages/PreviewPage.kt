package com.fula.mysticchina.pages

import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.App
import com.fula.mysticchina.base.BasePager
import com.fula.mysticchina.components.ProgressBar
import com.fula.mysticchina.player.*
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

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
                backgroundColor(MysticChinaColors.background)
                size(pagerData.pageViewWidth, pagerData.pageViewHeight)
            }

            // 视频画面区域
            View {
                attr {
                    absolutePositionAllZero()
                    backgroundColor(MysticChinaColors.background)
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
                                color(MysticChinaColors.textPrimary)
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
                        height(MysticChinaTheme.BarHeight.topBar + pagerData.statusBarHeight)
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
                            size(MysticChinaTheme.BarHeight.topBar, MysticChinaTheme.BarHeight.topBar)
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
                                color(MysticChinaColors.textPrimary)
                            }
                        }
                    }
                    Text {
                        attr {
                            flex(1f)
                            text(ctx.videoTitle)
                            fontSize(MysticChinaTheme.FontSize.subtitle)
                            color(MysticChinaColors.textPrimary)
                            textAlignCenter()
                            marginRight(MysticChinaTheme.BarHeight.topBar)
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
                        paddingBottom(MysticChinaTheme.Spacing.lg)
                    }

                    // 进度行
                    View {
                        attr {
                            flexDirectionRow()
                            alignItemsCenter()
                            padding(
                                left = MysticChinaTheme.Spacing.lg,
                                right = MysticChinaTheme.Spacing.lg
                            )
                            marginBottom(MysticChinaTheme.Spacing.sm)
                        }
                        Text {
                            attr {
                                text(ctx.controller.currentTimeText)
                                fontSize(MysticChinaTheme.FontSize.caption)
                                color(MysticChinaColors.textSecondary)
                                marginRight(MysticChinaTheme.Spacing.sm)
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
                                fontSize(MysticChinaTheme.FontSize.caption)
                                color(MysticChinaColors.textSecondary)
                                marginLeft(MysticChinaTheme.Spacing.sm)
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
                                    color(MysticChinaColors.textPrimary)
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
                            padding(all = MysticChinaTheme.Spacing.xl)
                            backgroundColor(Color(0xCC000000))
                            borderRadius(MysticChinaTheme.Radius.lg)
                            allCenter()
                            flexDirectionColumn()
                        }
                        Text {
                            attr {
                                text("⚠ ${ctx.controller.errorMessage}")
                                fontSize(MysticChinaTheme.FontSize.body)
                                color(MysticChinaColors.error)
                                marginBottom(MysticChinaTheme.Spacing.md)
                            }
                        }
                        View {
                            attr {
                                height(36f)
                                padding(left = MysticChinaTheme.Spacing.xl, right = MysticChinaTheme.Spacing.xl)
                                backgroundColor(MysticChinaColors.primary)
                                borderRadius(MysticChinaTheme.Radius.round)
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
                                    fontSize(MysticChinaTheme.FontSize.body)
                                    color(MysticChinaColors.textPrimary)
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
        val player = com.fula.mysticchina.player.PlatformPlayerFactory.createPlayer()
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