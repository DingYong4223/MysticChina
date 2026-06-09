# ExploringChina — 包名变更 + 首页重构 设计文档

**日期**: 2026-06-09  
**状态**: 待实现

---

## 1. 目标

将 yijian 框架代码改造为 ExploringChina（探索中国文化）项目：
1. 全局包名从 `com.yijian` 更换为 `com.fula.exploringchina`
2. 首页重构为文化功能卡片网格，首张卡片为"汉字练习"入口

---

## 2. 包名变更范围

| 变更项 | 旧值 | 新值 |
|---|---|---|
| Kotlin 包路径 | `com.yijian` | `com.fula.exploringchina` |
| Android namespace (shared) | `com.yijian` | `com.fula.exploringchina` |
| Android app ID (androidApp) | `com.yijian.android` | `com.fula.exploringchina` |
| SharedPreferences key 前缀 | `yijian_*` | `exploringchina_*` |
| `rootProject.name` | `"yijian"` | `"exploringchina"` |
| `IKuiklyRenderModuleExport` 内部 TAG | `YijianMain` | `ExploringChinaMain` |

**不变**：`@Page` 注解名称字符串（`"HomePage"`, `"SplashPage"` 等）及 `jumpPage` 调用，KuiklyUI 依赖坐标。

---

## 3. 主题色系

从剪映冷色蓝紫系改为中国红/金色暖色系：

| 色值 | 用途 | 替换自 |
|---|---|---|
| `#E8352A` | primary（中国红） | `#23D3FD` |
| `#F5A623` | accent（金黄） | `#AD37FE` |
| `#E8352A` | gradientStart | `#23D3FD` |
| `#F5A623` | gradientEnd | `#AD37FE` |
| 其余背景/文字色 | 保持不变 | — |

---

## 4. SplashPage 改动

- Logo 字母：`W` → `探`
- 应用名称：`一剪` → `探索中国`
- 副标题：`你的智能视频剪辑助手` → `探索中华文化之美`
- 跳转目标：`HomePage`（不变）

---

## 5. HomePage 重构

### 5.1 Tab 结构

| index | 标签 | 图标 | 内容组件 |
|---|---|---|---|
| 0 | 探索 | 🧭 | `ExploreTabContent` |
| 1 | 学习 | 📚 | `LearnTabContent`（占位） |
| 2 | 我的 | 👤 | `ProfileTabContent`（保留现有） |

删除原有 `ClipTabContent`、`DraftCard`、`DraftActionBar` 集成和 `DraftManager` 依赖（首页不再需要）。

### 5.2 ExploreTabContent 布局

```
┌────────────────────────────────────┐
│  探索中国文化             [状态栏]  │  ← 顶部标题（随 statusBarHeight 留白）
├────────────────────────────────────┤
│  ┌──────────────┐ ┌──────────────┐ │
│  │  🖊           │ │  🔜          │ │
│  │  汉字练习     │ │  更多功能    │ │
│  │  写好每一笔   │ │  即将上线    │ │
│  └──────────────┘ └──────────────┘ │
└────────────────────────────────────┘
```

- 每张卡片：正方形（屏宽/2 - margin），渐变背景，emoji + 功能名 + 副标题
- "汉字练习"卡片点击：`jumpPage("HanziPage")`
- "更多功能"卡片：disabled 样式，点击无响应

### 5.3 新增 HanziPage（占位）

`@Page("HanziPage")` — 仅显示标题"汉字练习"和"即将上线"文案，有返回按钮。

---

## 6. 文件变更清单

### 新建
- `shared/src/commonMain/kotlin/com/fula/exploringchina/pages/HanziPage.kt`

### 重命名目录（包路径）
- `shared/src/commonMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `shared/src/androidMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `shared/src/appleMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `shared/src/iosMain/kotlin/com/yijian/` → `com/fula/exploringchina/`
- `androidApp/src/main/java/com/yijian/android/` → `com/fula/exploringchina/`

### 修改内容
- `shared/src/commonMain/kotlin/.../theme/Colors.kt` — 主题色
- `shared/src/commonMain/kotlin/.../pages/SplashPage.kt` — 品牌文案
- `shared/src/commonMain/kotlin/.../pages/HomePage.kt` — Tab 重构 + ExploreTabContent
- `shared/build.gradle.kts` — namespace
- `androidApp/build.gradle.kts` — namespace + applicationId
- `settings.gradle.kts` — rootProject.name
- `AndroidManifest.xml` — package（如有显式声明）
