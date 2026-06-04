package com.yijian.components

import com.tencent.kuikly.core.base.*
import com.tencent.kuikly.core.views.*
import com.yijian.theme.YijianColors

/**
 * 底部操作栏 — 选中模式时覆盖 Tab Bar
 *
 * ┌─────────────────────────────────────┐
 * │         │                            │
 * │  全选    │         删除(N)            │
 * │         │                            │
 * └─────────────────────────────────────┘
 */
class DraftActionBarConfig(
    val isAllSelected: Boolean,
    val selectedCount: Int,
    val onToggleSelectAll: () -> Unit,
    val onDelete: () -> Unit,
)

internal fun ViewContainer<*, *>.DraftActionBar(
    config: DraftActionBarConfig,
    safeAreaBottom: Float,
) {
    val selectAllLabel = if (config.isAllSelected) "反选" else "全选"
    val canDelete = config.selectedCount > 0

    View {
        attr {
            height(56f + safeAreaBottom)
            backgroundColor(Color(0xFF1E1E1E))
            flexDirectionRow()
            alignItemsCenter()
            paddingBottom(safeAreaBottom)
        }

        // 全选/反选
        View {
            attr { flex(1f); allCenter() }
            event { click { config.onToggleSelectAll.invoke() } }
            Text {
                attr {
                    text(selectAllLabel)
                    fontSize(15f); color(YijianColors.textPrimary)
                }
            }
        }

        // 分割线
        View { attr { width(1f); height(30f); backgroundColor(YijianColors.surfaceLight) } }

        // 删除
        View {
            attr { flex(1f); allCenter() }
            event { click { if (canDelete) config.onDelete.invoke() } }
            Text {
                attr {
                    text(if (config.selectedCount > 0) "删除(${config.selectedCount})" else "删除")
                    fontSize(15f)
                    color(if (canDelete) Color(0xFFFF3B30) else Color(0xFF666666))
                }
            }
        }
    }
}
