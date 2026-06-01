# 首页三 Tab 框架设计规格

**日期**：2026-05-29  
**项目**：yijian（一剪）  
**状态**：已批准  

---

## 背景与目标

为 yijian App 搭建完整的首页导航框架，由三个 Tab 组成（剪辑 / 学习 / 我的），替换现有的 `MainPage` 在路由中的位置，为二期视频编辑功能提供统一入口。

---

## 导航架构

### 路由变更
```
之前：SplashPage → MainPage → EditorPage
之后：SplashPage → HomePage（3-Tab）→ EditorPage
                              └→ MainPage（选择视频时作为子页面）
```

### 实现方案
**单 Pager + 手动 Tab 切换**：`HomePage` 是一个 Pager，内部维护 `selectedTab: Int` 可观察状态，点击底部 Tab 切换显示不同内容区域，无页面跳转。整个首页在一个 Activity 内完成，Tab 切换零延迟。

---

## 文件结构

```
新增文件：
  shared/src/commonMain/kotlin/com/yijian/
  ├── pages/
  │   └── HomePage.kt              ← 3-Tab 主页（@Page 注解，替换 MainPage 导航位置）
  └── model/
      └── UserProfile.kt           ← 用户资料数据类 + SharedPreferences 本地存储

修改文件：
  pages/SplashPage.kt              ← 跳转目标: "MainPage" → "HomePage"
  pages/MainPage.kt                ← 降级为"选择视频"子页面，从 HomePage 内启动
```

---

## 数据模型

### UserProfile.kt
```kotlin
data class UserProfile(
    val nickname: String = "创作者",
    val bio: String = "记录生活的每一刻",
    val avatarEmoji: String = "🎬"
)
```

**存储**：Android 端 `SharedPreferences`（key: `yijian_user_profile`），通过 `expect/actual` 桥接到 commonMain  
- `commonMain`：声明 `expect object ProfileStorage { fun load(): UserProfile; fun save(p: UserProfile) }`  
- `androidMain`：`actual` 实现调用 `SharedPreferences`（序列化为 JSON 字符串，手动拼接，不引入额外库）  
- `iosMain`：`actual` 实现调用 `NSUserDefaults`（占位，同结构）  
**UI 驱动**：`HomePage` 持有 `var userProfile by observable(ProfileStorage.load())`，修改后调用 `ProfileStorage.save()` 再更新 observable

---

## HomePage 核心状态

```kotlin
@Page("HomePage", supportInLocal = true)
internal class HomePage : BasePager() {
    var selectedTab by observable(0)          // 0=剪辑 1=学习 2=我的
    var userProfile by observable(UserProfile.load())
    var draftList by observableList<VideoInfo>()  // 草稿列表（mock）
}
```

---

## 布局规格

### 整体骨架
```
┌─────────────────────────────────┐
│           内容区（flex 1）        │  ← vif 切换三个 Tab 内容
├─────────────────────────────────┤
│  [✂ 剪辑]   [🎓 学习]   [👤 我的]│  ← 底部固定 Tab Bar
└─────────────────────────────────┘
```

---

### Tab 1：剪辑

```
┌─────────────────────────────────┐
│  一剪                            │  ← TopBar，标题左对齐，18sp，加粗
├─────────────────────────────────┤
│  ╔═══════════════════════════╗  │
│  ║  🎬  + 新建剪辑            ║  │  ← 渐变色按钮（青→紫），56dp 高，圆角 12dp
│  ╚═══════════════════════════╝  │     点击 → jumpPage("MainPage")
├─────────────────────────────────┤
│  草稿箱                  全部 > │  ← Section 标题行，14sp
│                                 │
│  [卡片][卡片]                    │  ← 2列网格，卡片间距 8dp
│  [卡片][卡片]                    │
│                                 │
│  （空态：暂无草稿，点击创建吧）    │  ← 草稿列表为空时居中显示
└─────────────────────────────────┘
```

**草稿卡片规格**：
```
┌──────────────────┐
│  缩略图占位区     │  ← 16:9 比例，surfaceLight 背景色
│  [时长标签]      │     右下角时长 badge
├──────────────────┤
│ 标题（1行截断）   │  ← 12sp，textPrimary
│ 2小时前 · 15s   │  ← 10sp，textTertiary
└──────────────────┘
```

**Mock 草稿数据**：沿用现有 VideoInfo 测试数据（4 条）。  
**草稿卡片点击**：`jumpPage("EditorPage", params)`（传入 videoPath + videoTitle）

---

### Tab 2：学习（空占位）

```
┌─────────────────────────────────┐
│                                 │
│                                 │
│           🎓                    │  ← 48sp emoji，居中
│        即将上线                  │  ← 16sp，textPrimary，居中
│   学习内容正在精心准备中...       │  ← 12sp，textSecondary，居中
│                                 │
│                                 │
└─────────────────────────────────┘
```

---

### Tab 3：我的

```
┌─────────────────────────────────┐
│         ┌────────┐              │
│         │  🎬    │              │  ← 头像区：80×80 圆形，emoji 占位
│         └────────┘              │     点击触发编辑流程
│         昵称                    │  ← 20sp，加粗，居中，点击可编辑
│     一句话介绍你自己...            │  ← 14sp，textSecondary，居中，点击可编辑
├─────────────────────────────────┤
│  我的统计                        │  ← Section 标题
│  ┌──────┐  ┌──────┐  ┌──────┐ │
│  │  4   │  │  1   │  │3m12s │ │  ← 项目数 / 本月创作 / 总时长（mock）
│  │ 项目  │  │ 本月  │  │ 时长  │ │
│  └──────┘  └──────┘  └──────┘ │
├─────────────────────────────────┤
│  ⚙  设置                     > │  ← 列表项（点击暂无操作，预留）
│  📱  关于一剪                 > │  ← 列表项（预留）
└─────────────────────────────────┘
```

**编辑流程**：点击昵称/简介/头像区域 → `showEditDialog` 状态置 true → 显示输入面板（KuiklyUI Input）→ 确认 → `UserProfile.save()` → 刷新 observable

---

### 底部 Tab Bar

```
属性             值
─────────────────────────────────
高度             56f + safeAreaInsets.bottom
背景             YijianColors.background
顶部分割线        1dp，YijianColors.surfaceLight
图标字号          22sp（emoji）
标签字号          10sp
选中颜色         YijianColors.primary（青色 #23D3FD）
未选中颜色        YijianColors.textTertiary
选中指示         图标上方 4×4dp 圆点，primary 色
```

**Tab 定义**：
```kotlin
enum class Tab(val label: String, val icon: String) {
    CLIP("剪辑", "✂"),
    LEARN("学习", "🎓"),
    PROFILE("我的", "👤")
}
```

---

## 路由与页面变更明细

### SplashPage.kt 修改
```kotlin
// 之前
jumpPage("MainPage")

// 之后
jumpPage("HomePage")
```

### MainPage.kt 角色变更
- 原功能保留（媒体库、视频选择、GalleryModule 调用）
- 仅作为子页面被 `HomePage` 的剪辑 Tab 通过 `jumpPage("MainPage")` 启动
- `onVideoClick` 改为跳转 `EditorPage`（已完成）

---

## 边界条件

| 场景 | 处理 |
|------|------|
| 草稿列表为空 | 显示空态提示，引导点击「新建剪辑」|
| 用户资料读取失败 | 使用 `UserProfile()` 默认值，不崩溃 |
| 昵称/简介为空提交 | 保留原值，不存空字符串 |
| KSP 页面注册 | `@Page("HomePage")` 自动注册，SplashPage 跳转即可生效 |

---

## 不在本次范围内

- 真实草稿持久化（项目存储到本地数据库）
- 真实头像上传（图片选择器 + 存储）
- 学习 Tab 内容
- 设置页面功能
- 登录 / 账号体系
