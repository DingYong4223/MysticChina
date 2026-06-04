package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.log.KLog
import com.tencent.kuikly.core.views.*
import com.yijian.manager.DraftManager
import com.yijian.theme.YijianColors

/**
 * 底部操作栏 — 选中模式时覆盖 Tab Bar
 *
 * ┌─────────────────────────────────────┐
 * │         │                            │
 * │  全选    │         删除(N)            │
 * │         │                            │
 * └─────────────────────────────────────┘
 *
 * 直接持有 [DraftManager] 引用，使 attr{} 能追踪 observable 字段
 * 实现响应式重绘（selectedCount / isAllSelected 变化时自动更新文字和颜色）。
 */
internal fun ViewContainer<*, *>.DraftActionBar(
    mgr: DraftManager,
    safeAreaBottom: Float,
) {
    View {
        attr {
            height(56f + safeAreaBottom)
            backgroundColor(Color(0xFF1E1E1E))
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(safeAreaBottom)
        }

        // 全选/反选 — attr{} 读取 mgr.isAllSelected（observable），自动追踪
        View {
            attr { flex(1f); allCenter() }
            event {
                click {
                    KLog.d("DraftBar", "toggleAll clicked, isAllSelected=${mgr.isAllSelected}")
                    if (mgr.isAllSelected) mgr.deselectAll() else mgr.selectAll()
                }
            }
            Text {
                attr {
                    text(if (mgr.isAllSelected) "反选" else "全选")
                    fontSize(15f)
                    color(YijianColors.textPrimary)
                }
            }
        }

        // 分割线
        View { attr { width(1f); height(30f); backgroundColor(YijianColors.surfaceLight) } }

        // 删除 — attr{} 读取 mgr.selectedCount（observable），自动追踪
        View {
            attr { flex(1f); allCenter() }
            event {
                click {
                    if (mgr.selectedCount > 0) mgr.remove(mgr.selectedIds.toList())
                }
            }
            Text {
                attr {
                    text(if (mgr.selectedCount > 0) "删除(${mgr.selectedCount})" else "删除")
                    fontSize(15f)
                    color(if (mgr.selectedCount > 0) Color(0xFFFF3B30) else Color(0xFF666666))
                }
            }
        }
    }
}
