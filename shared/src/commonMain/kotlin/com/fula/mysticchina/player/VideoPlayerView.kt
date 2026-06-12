package com.fula.mysticchina.player

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.fula.mysticchina.components.IconButton
import com.fula.mysticchina.components.ProgressBar
import com.fula.mysticchina.theme.MysticChinaColors
import com.fula.mysticchina.theme.MysticChinaTheme

/**
 * VideoPlayerView 属性
 */
internal class VideoPlayerAttr : ComposeAttr() {
    var videoPath: String = ""
    var controller: PlayerController? = null
}

/**
 * VideoPlayerView 事件
 */
internal class VideoPlayerEvent : ComposeEvent() {
    var onBackClick: (() -> Unit)? = null
}

/**
 * 视频播放器 UI 组件
 *
 * 组成：
 * 1. 视频画面区域（通过原生桥接渲染）
 * 2. 控制栏覆盖层（半透明）
 *    - 顶部：返回按钮 + 标题
 *    - 中央：大播放/暂停按钮（仅在暂停时显示）
 *    - 底部：播放/暂停 + 进度条 + 时间显示
 */
internal class VideoPlayerView : ComposeView<VideoPlayerAttr, VideoPlayerEvent>() {

    override fun createAttr(): VideoPlayerAttr = VideoPlayerAttr()

    override fun createEvent(): VideoPlayerEvent = VideoPlayerEvent()

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            // 最外层容器 — 全屏视频区域
            View {
                attr {
                    size(pagerData.pageViewWidth, pagerData.pageViewHeight)
                    backgroundColor(MysticChinaColors.background)
                }

                // 视频画面渲染区域（原生层）
                // 通过 Kuikly 的 expand-native-api 桥接到原生 VideoView
                View {
                    attr {
                        size(pagerData.pageViewWidth, pagerData.pageViewHeight)
                        backgroundColor(MysticChinaColors.background)
                        // video surface (native layer placeholder)
                    }
                }

                // 加载指示器
                if (ctx.attr.controller?.playerState == PlayerState.LOADING) {
                    View {
                        attr {
                            absolutePositionAllZero()
                            allCenter()
                        }
                        // 简单的加载动画指示
                        Text {
                            attr {
                                text("加载中...")
                                fontSize(MysticChinaTheme.FontSize.body)
                                color(MysticChinaColors.textSecondary)
                            }
                        }
                    }
                }

                // ============ 控制栏覆盖层 ============
                if (ctx.attr.controller?.isControlsVisible == true) {
                    // 点击画面切换控制栏 — 整个半透明区域
                    View {
                        attr {
                            absolutePositionAllZero()
                        }
                        event {
                            click {
                                ctx.attr.controller?.toggleControls()
                            }
                        }

                        // ---- 顶部控制栏 ----
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

                            // 返回按钮
                            View {
                                attr {
                                    size(MysticChinaTheme.BarHeight.topBar, MysticChinaTheme.BarHeight.topBar)
                                    allCenter()
                                }
                                event {
                                    click {
                                        ctx.event.onBackClick?.invoke()
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

                            // 标题
                            Text {
                                attr {
                                    flex(1f)
                                    text("视频预览")
                                    fontSize(MysticChinaTheme.FontSize.subtitle)
                                    color(MysticChinaColors.textPrimary)
                                    marginRight(MysticChinaTheme.BarHeight.topBar)
                                }
                            }
                        }

                        // ---- 中央大播放按钮（暂停时显示） ----
                        if (ctx.attr.controller?.playerState == PlayerState.PAUSED
                            || ctx.attr.controller?.playerState == PlayerState.READY
                            || ctx.attr.controller?.playerState == PlayerState.COMPLETED) {
                            View {
                                attr {
                                    absolutePositionAllZero()
                                    allCenter()
                                }
                                event {
                                    click {
                                        ctx.attr.controller?.togglePlayPause()
                                    }
                                }
                                View {
                                    attr {
                                        size(60f, 60f)
                                        borderRadius(30f)
                                        backgroundColor(Color(0x80000000))
                                        allCenter()
                                    }
                                    Text {
                                        attr {
                                            text(if (ctx.attr.controller?.playerState == PlayerState.COMPLETED) "↻" else "▶")
                                            fontSize(28f)
                                            color(MysticChinaColors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }

                        // ---- 底部控制栏 ----
                        View {
                            attr {
                                absolutePosition(bottom = 0f, left = 0f, right = 0f)
                                height(MysticChinaTheme.BarHeight.bottomBar + 30f)
                                backgroundLinearGradient(
                                    Direction.TO_TOP,
                                    ColorStop(Color(0xCC000000), 0f),
                                    ColorStop(Color(0x00000000), 1f)
                                )
                                flexDirectionColumn()
                                justifyContentCenter()
                            }

                            // 进度条
                            View {
                                attr {
                                    flexDirectionRow()
                                    alignItemsCenter()
                                    padding(left = MysticChinaTheme.Spacing.lg, right = MysticChinaTheme.Spacing.lg)
                                    marginBottom(MysticChinaTheme.Spacing.sm)
                                }

                                // 当前时间
                                Text {
                                    attr {
                                        text(ctx.attr.controller?.currentTimeText ?: "00:00")
                                        fontSize(MysticChinaTheme.FontSize.caption)
                                        color(MysticChinaColors.textSecondary)
                                        marginRight(MysticChinaTheme.Spacing.sm)
                                    }
                                }

                                // 进度条
                                View {
                                    attr {
                                        flex(1f)
                                    }
                                    ProgressBar {
                                        attr {
                                            progress = ctx.attr.controller?.currentProgress ?: 0f
                                            barHeight = 3f
                                            showThumb = true
                                            enableDrag = true
                                        }
                                    }
                                }

                                // 总时长
                                Text {
                                    attr {
                                        text(ctx.attr.controller?.durationText ?: "00:00")
                                        fontSize(MysticChinaTheme.FontSize.caption)
                                        color(MysticChinaColors.textSecondary)
                                        marginLeft(MysticChinaTheme.Spacing.sm)
                                    }
                                }
                            }

                            // 底部控制按钮
                            View {
                                attr {
                                    flexDirectionRow()
                                    allCenter()
                                    paddingBottom(MysticChinaTheme.Spacing.lg)
                                }

                                // 播放/暂停按钮
                                View {
                                    attr {
                                        size(48f, 48f)
                                        allCenter()
                                    }
                                    event {
                                        click {
                                            ctx.attr.controller?.togglePlayPause()
                                        }
                                    }
                                    Text {
                                        attr {
                                            text(
                                                when (ctx.attr.controller?.playerState) {
                                                    PlayerState.PLAYING -> "⏸"
                                                    PlayerState.COMPLETED -> "↻"
                                                    else -> "▶"
                                                }
                                            )
                                            fontSize(32f)
                                            color(MysticChinaColors.textPrimary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 控制栏隐藏时 — 点击画面可以唤出
                    View {
                        attr {
                            absolutePositionAllZero()
                        }
                        event {
                            click {
                                ctx.attr.controller?.toggleControls()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 扩展方法
 */
internal fun ViewContainer<*, *>.VideoPlayer(init: VideoPlayerView.() -> Unit) {
    addChild(VideoPlayerView(), init)
}
