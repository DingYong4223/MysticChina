# KuiklyUI API 速查手册

> 从 `KuiklyUI/core/src/commonMain` 源码直接提取，2026-06-12。  
> 开发指南见 [KUIKLY_GUIDE.md](KUIKLY_GUIDE.md)。

---

## 目录

1. [视图系统](#视图系统)
2. [通用属性 Attr](#通用属性-attr)
3. [容器属性 ContainerAttr](#容器属性-containerattr)
4. [通用事件 Event](#通用事件-event)
5. [Pager 类](#pager-类)
6. [PageData 字段](#pagedata-字段)
7. [ComposeView 类](#composeview-类)
8. [响应式系统](#响应式系统)
9. [Animation 动画](#animation-动画)
10. [Timer 定时器](#timer-定时器)
11. [指令系统](#指令系统)
12. [模块系统 Modules](#模块系统-modules)
13. [Color 颜色](#color-颜色)
14. [ViewConst 常量](#viewconst-常量)
15. [数据类型参考](#数据类型参考)

---

## 视图系统

### View — 通用容器

```kotlin
View { }    // DivView，继承 GroupAttr + GroupEvent
```

**专有 Attr（GroupAttr，继承 ContainerAttr）**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `highlightBackgroundColor(color)` | `Color` | 按下时高亮背景色 |
| `backgroundImage(src, imageAttr?)` | `String, (ImageAttr.() -> Unit)?` | 容器背景图片 |
| `borderBottom/Top/Left/Right(border)` | `Border` | 单边边框 |
| `screenFramePause(pause)` | `Boolean` | 暂停 VSYNC 帧事件 |

**专有 Event（GroupEvent，继承 Event）**

| 事件 | 回调 | 说明 |
|-----|-----|------|
| `touchDown(isSync, handler)` | `Boolean=false, (TouchParams) -> Unit` | 触摸按下 |
| `touchUp(isSync, handler)` | `Boolean=false, (TouchParams) -> Unit` | 触摸抬起 |
| `touchCancel(isSync, handler)` | `Boolean=false, (TouchParams) -> Unit` | 触摸取消 |
| `touchMove(isSync, handler)` | `Boolean=false, (TouchParams) -> Unit` | 触摸移动 |
| `screenFrame(handler)` | `() -> Unit` | 屏幕刷新帧（VSYNC） |

---

### Text — 文本

```kotlin
Text { }    // TextView，继承 TextAttr + TextEvent
```

**TextAttr（继承 Attr）**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `text(text)` | `String` | 文本内容（同 `value()`） |
| `color(color)` | `Color` 或 `Long` | 文字颜色 |
| `fontSize(size, scaleFontSizeEnable?)` | `Float, Boolean?` | 字体大小 |
| `fontWeightNormal()` / `fontWeight400()` | — | 字重 400 |
| `fontWeightMedium()` / `fontWeight500()` | — | 字重 500 |
| `fontWeightSemiBold()` / `fontWeight600()` | — | 字重 600 |
| `fontWeightBold()` / `fontWeight700()` | — | 字重 700 |
| `fontWeightLight()` | — | 字重 300（iOS/鸿蒙） |
| `fontWeightExtraBold()` / `fontWeight800()` | — | 字重 800 |
| `fontFamily(family)` | `String` | 字体族 |
| `fontStyleItalic()` | — | 斜体 |
| `lines(lines)` | `Int` | 最大行数（0=不限） |
| `lineHeight(lineHeight)` | `Float` | 行高 |
| `lineSpacing(value)` | `Float` | 行间距 |
| `letterSpacing(value)` | `Float` | 字间距 |
| `paragraphSpacing(value)` | `Float` | 段落间距 |
| `textAlignLeft()` / `textAlignCenter()` / `textAlignRight()` | — | 文本对齐 |
| `textOverFlowTail()` | — | 末尾 `...` 省略 |
| `textOverFlowClip()` | — | 末尾截断 |
| `textOverFlowMiddle()` | — | 中间省略 |
| `textDecorationUnderLine()` | — | 下划线 |
| `textDecorationLineThrough()` | — | 删除线 |
| `textShadow(offsetX, offsetY, radius, color)` | `Float×3, Color` | 文字阴影 |
| `textStroke(color, width)` | `Color, Float=2f` | 文字描边（Android 不支持） |
| `lineBreakMargin(margin)` | `Float` | 末行折叠保留距离（"更多"按钮） |
| `firstLineHeadIndent(indent)` | `Float` | 首行缩进 |
| `useDpFontSizeDim(useDp)` | `Boolean` | Android 字体用 dp 单位 |
| `backgroundLinearGradient(direction, vararg stops)` | `Direction, ColorStop...` | 文字渐变色（覆盖 Attr 基类） |

**TextEvent（继承 Event）**

| 事件 | 说明 |
|-----|------|
| `onLineBreakMargin(handler)` | lineBreakMargin 折叠触发 |

---

### Image — 图片

```kotlin
Image { }    // ImageView，继承 ImageAttr + ImageEvent
```

**ImageAttr（继承 Attr）**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `src(src, isDotNineImage?)` | `String, Boolean=false` | 图片路径（http / file / base64） |
| `src(uri, isDotNineImage?)` | `ImageUri, Boolean=false` | 使用 ImageUri |
| `placeholderSrc(placeholder)` | `String` | 占位图 |
| `resizeCover()` | — | 等比裁剪填满 |
| `resizeContain()` | — | 等比完整显示 |
| `resizeStretch()` | — | 拉伸填满 |
| `blurRadius(radius)` | `Float` (0~12.5) | 高斯模糊 |
| `tintColor(color)` | `Color?` | 着色（null = 清除） |
| `capInsets(top, left, bottom, right)` | `Float×4` | .9图拉伸区域 |
| `maskLinearGradient(direction, vararg stops)` | `Direction, ColorStop...` | 渐变遮罩 |

**ImageUri 工厂**
```kotlin
ImageUri.commonAssets(path)   // assets://common/path
ImageUri.pageAssets(path)     // assets://#pageName#/path
ImageUri.file(path)           // file://path
```

**ImageEvent（继承 Event）**

| 事件 | 回调参数 | 说明 |
|-----|---------|------|
| `loadSuccess(handler)` | `LoadSuccessParams(src: String)` | 加载成功 |
| `loadFailure(handler)` | `LoadFailureParams(src: String, errorCode: Int)` | 加载失败 |
| `loadResolution(handler)` | `LoadResolutionParams(width: Int, height: Int)` | 获取图片分辨率 |

---

### Scroller — 滚动容器

```kotlin
Scroller { }    // ScrollerView，继承 ScrollerAttr + ScrollerEvent
```

**ScrollerAttr（继承 ContainerAttr）**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `scrollEnable(value)` | `Boolean` | 是否允许手势滚动 |
| `bouncesEnable(enable, limitHeaderBounces?)` | `Boolean, Boolean=false` | 边界回弹 |
| `showScrollerIndicator(value)` | `Boolean` | 显示滚动条 |
| `pagingEnable(enable)` | `Boolean` | 分页滚动 |
| `flingEnable(enable)` | `Boolean` | 惯性滚动（默认 true） |
| `syncScroll(enable)` | `Boolean` | 同步滚动 |
| `scrollWithParent(enable)` | `Boolean` | 到达边缘联动父级 |
| `nestedScroll(forward, backward)` | `KRNestedScrollMode×2` | 嵌套滚动模式 |

**KRNestedScrollMode**: `SELF_ONLY` / `SELF_FIRST` / `PARENT_FIRST`

**ScrollerEvent（继承 Event）**

| 事件 | 回调参数 | 说明 |
|-----|---------|------|
| `scroll(handler)` | `(ScrollParams) -> Unit` | 滚动中（异步） |
| `scroll(sync, handler)` | `Boolean, ScrollParams` | 滚动中（sync=true 同步） |
| `scrollEnd(handler)` | `(ScrollParams) -> Unit` | 滚动结束 |
| `dragBegin(handler)` | `(ScrollParams) -> Unit` | 开始拖拽 |
| `dragEnd(handler)` | `(ScrollParams) -> Unit` | 结束拖拽 |
| `willDragEndBySync(handler)` | `(WillEndDragParams) -> Unit` | 将要松手（同步，可自定义吸附） |
| `scrollToTop(handler)` | `() -> Unit` | 点击状态栏回顶部 |
| `contentSizeChanged(handler)` | `(Float, Float) -> Unit` | 内容尺寸变化 |

**ScrollParams 字段**: `offsetX`, `offsetY`, `contentWidth`, `contentHeight`, `viewWidth`, `viewHeight`, `isDragging`  
**WillEndDragParams 额外**: `velocityX`, `velocityY`, `targetContentOffsetX`, `targetContentOffsetY`

**ScrollerView 实例方法（通过 ViewRef 调用）**

```kotlin
setContentOffset(offsetX, offsetY, animated = false)
setContentOffset(offsetX, offsetY, springAnimation?)
abortContentOffsetAnimate()
setContentInset(top, left, bottom, right, animated = false)
```

---

### List — 虚拟化列表

```kotlin
List { }    // ListView，继承 ListAttr + ListEvent（含 Scroller 全部 API）
```

**ListAttr 额外方法**

| 方法 | 参数 | 默认 | 说明 |
|-----|-----|-----|------|
| `firstContentLoadMaxIndex(maxIndex)` | `Int` | 8 | 首屏分批加载最大条数 |
| `preloadViewDistance(distance)` | `Float` | — | 预加载距离（0=一屏） |

---

### RichText — 富文本

```kotlin
RichText { }       // 容器
Span { }           // 文字 Span（继承 TextAttr，额外 click 事件）
PlaceholderSpan { } // 占位 Span（spanFrameDidChanged 回调）
ImageSpan { }      // 图片 Span（继承 PlaceholderSpan + IImageAttr）
```

**RichTextAttr 额外方法**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `spans(spans)` | `ArrayList<ISpan>` | 批量设置 Span |

**TextSpan 额外方法**

| 方法 | 说明 |
|-----|------|
| `click(handler)` | Span 单击 |

**PlaceholderSpan 方法**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `placeholderSize(width, height)` | `Float, Float` | 占位尺寸 |
| `spanFrameDidChanged(handler)` | `(Frame) -> Unit` | 占位位置变化（在文字中叠加 View） |

**ImageSpan 额外方法**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `size(width, height)` | `Float, Float` | 图片尺寸 |
| `borderRadius(radius)` | `Float` | 圆角 |
| `verticalAlignOffset(offset)` | `Float` | 垂直对齐偏移 |
| `click(handler)` | `(ClickParams) -> Unit` | 图片 Span 点击 |

---

### Input — 输入框

```kotlin
Input { }    // InputView，继承 InputAttr + InputEvent
```

**InputAttr（继承 Attr）**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `text(text)` | `String` | 设置文本内容 |
| `textInputState(state)` | `TextInputState` | 原子设置文本+选区+组合态 |
| `inputSpans(spans)` | `InputSpans` | 富文本输入样式 |
| `fontSize(size)` | `Float` | 字体大小 |
| `color(color)` | `Color` | 文字颜色 |
| `tintColor(color)` | `Color` | 光标颜色 |
| `placeholderColor(color)` | `Color` | 占位符颜色 |
| `selectionColor(color)` | `Color` | 选区颜色 |
| `placeholder(text)` | `String` | 占位文本 |
| `lines(lines)` | `Int` | 最大行数 |
| `keyboardTypePassword()` | — | 密码键盘 |
| `keyboardTypeNumber()` | — | 数字键盘 |
| `keyboardTypeEmail()` | — | 邮箱键盘 |
| `returnKeyTypeDone/Search/Send/Next/Go()` | — | Return 键类型 |
| `maxTextLength(length, type)` | `Int, LengthLimitType` | 最大长度 |
| `autofocus(focus)` | `Boolean` | 自动聚焦 |
| `editable(editable)` | `Boolean` | 可编辑 |
| `enablePinyinCallback(enable)` | `Boolean` | 拼音回调 |
| `autoHideKeyboardOnImeAction(enable)` | `Boolean` | IME 动作后收键盘 |

**LengthLimitType**: `BYTE(0)` / `CHARACTER(1)` / `VISUAL_WIDTH(2)`

**InputEvent（继承 Event）**

| 事件 | 回调参数 | 说明 |
|-----|---------|------|
| `textDidChange(isSyncEdit?, handler)` | `Boolean=false, InputParams` | 文本变化 |
| `textInputStateChange(isSyncEdit?, handler)` | `Boolean=true, TextInputState` | 文本/选区/组合态变化 |
| `selectionChange(handler)` | `TextInputState` | 仅选区变化 |
| `inputFocus(handler)` | `InputParams` | 获取焦点 |
| `inputBlur(handler)` | `InputParams` | 失去焦点 |
| `inputReturn(handler)` | `InputParams` | 按下 Return 键 |
| `keyboardHeightChange(isSync?, handler)` | `Boolean=false, KeyboardParams` | 键盘高度变化 |

**KeyboardParams 字段**: `height: Float`, `duration: Float`, `curve: Int`

**InputView 实例方法（通过 ViewRef 调用）**

```kotlin
focus()
blur()
setText(text: String)
cursorIndex(callback: (Int) -> Unit)
setCursorIndex(index: Int)
setTextInputState(state: TextInputState)
getTextInputState(callback: (TextInputState) -> Unit)
```

---

### TextArea — 多行文本输入

```kotlin
TextArea { }    // 支持多行，API 与 Input 基本相同
```

额外 Attr：
- `textAlignCenter/Left/Right()` — 文本对齐
- `enablesReturnKeyAutomatically(flag)` — iOS 空内容时禁用 Return 键

---

### Slider — 滑块

```kotlin
Slider { }    // SliderView（ComposeView），继承 SliderAttr + SliderEvent
```

**SliderAttr**

| 方法 | 参数 | 默认 | 说明 |
|-----|-----|-----|------|
| `currentProgress(progress01)` | `Float` | — | 当前进度 [0,1] |
| `progressColor(color)` | `Color` | 蓝色 | 进度条颜色 |
| `trackColor(color)` | `Color` | 灰色 | 轨道颜色 |
| `thumbColor(color)` | `Color` | 白色 | 滑块颜色 |
| `trackThickness(thickness)` | `Float` | 5f | 轨道厚度 |
| `thumbSize(size)` | `Size` | 10×10 | 滑块尺寸 |
| `sliderDirection(horizontal)` | `Boolean` | true | 方向（true=横向） |
| `progressViewCreator(creator)` | `ViewBuilder` | — | 自定义进度条视图 |
| `trackViewCreator(creator)` | `ViewBuilder` | — | 自定义轨道视图 |
| `thumbViewCreator(creator)` | `ViewBuilder` | — | 自定义滑块视图 |

**SliderEvent**

| 事件 | 回调 | 说明 |
|-----|-----|------|
| `progressDidChanged(handler)` | `Float` | 进度变化 |
| `beginDragSlider(handler)` | `PanGestureParams` | 开始拖拽 |
| `endDragSlider(handler)` | `PanGestureParams` | 结束拖拽 |

---

### Video — 视频

```kotlin
Video { }    // VideoView，继承 VideoAttr + VideoEvent
```

**VideoAttr**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `src(src)` | `String` | 视频源 URL |
| `playControl(control)` | `VideoPlayControl` | 播控枚举 |
| `muted(muted)` | `Boolean` | 静音 |
| `rate(rate)` | `Float` | 倍速（1.0/1.25/1.5/2.0） |
| `resizeModeToCover/Contain/Stretch()` | — | 缩放模式 |

**VideoPlayControl**: `PREPLAY(1)` / `PLAY(2)` / `PAUSE(3)` / `STOP(4)`

**VideoEvent**

| 事件 | 回调 | 说明 |
|-----|-----|------|
| `playStateDidChanged(handler)` | `PlayState, JSONObject` | 状态变化 |
| `playTimeDidChanged(handler)` | `curTime: Int, totalTime: Int` | 进度变化（毫秒） |
| `firstFrameDidDisplay(handler)` | — | 首帧显示 |

**PlayState**: `NONE` / `PLAYING` / `BUFFERING` / `PAUSED` / `PLAY_END` / `ERROR`

---

### Canvas — 画布

```kotlin
Canvas(init) { context, width, height -> /* 绘制代码 */ }
```

**CanvasContext 绘制 API**

```kotlin
// 路径
beginPath(); moveTo(x, y); lineTo(x, y); arc(cx,cy,r,start,end,ccw)
closePath(); quadraticCurveTo(cpx,cpy,x,y); bezierCurveTo(cp1x,cp1y,cp2x,cp2y,x,y)

// 填充/描边
fill(); stroke()
fillStyle(color); strokeStyle(color)
fillStyle(linearGradient); strokeStyle(linearGradient)
lineWidth(width); setLineDash(intervals)
lineCapRound(); lineCapButt(); lineCapSquare()

// 变换
save(); restore(); saveLayer(x,y,w,h)
translate(x,y); scale(x,y); rotate(angle); skew(x,y)
transform(floatArray)   // 3×3 矩阵

// 裁剪
clip(intersect); clipPathIntersect(); clipPathDifference()

// 文字
font(size, family); font(style, weight, size, family)
textAlign(align)
fillText(text, x, y); strokeText(text, x, y)
measureText(value): TextMetrics

// 图片
drawImage(image, dx, dy)
drawImage(image, dx, dy, dw, dh)
drawImage(image, sx, sy, sw, sh, dx, dy, dw, dh)

// 渐变
createLinearGradient(x0,y0,x1,y1): LinearGradient
// LinearGradient.addColorStop(stop01, color)
```

---

### WaterfallList — 瀑布流

```kotlin
WaterfallList { }    // 继承 ListView，额外 WaterfallListAttr
```

**WaterfallListAttr 额外方法**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `listWidth(width)` | `Float` | **必须设置**，列表总宽度 |
| `columnCount(count)` | `Int` | 列数（默认1，动态修改立即重排） |
| `itemSpacing(spacing)` | `Float` | 列间距 |
| `lineSpacing(spacing)` | `Float` | 行间距 |
| `contentPadding(top,left,bottom,right)` | `Float×4` | 内容区内边距 |

---

### Tabs — 标签页

```kotlin
Tabs { }        // TabsView，配合 PageList 使用
TabItem { state -> }   // state.selected: Boolean（响应式）
```

**TabsAttr 额外方法**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `scrollParams(params)` | `ScrollParams` | **必须设置**，来自 PageList 的滚动参数 |
| `defaultInitIndex(index)` | `Int` | 初始选中 index |
| `indicatorInTabItem(creator)` | `ViewBuilder` | 可滚动指示条视图 |
| `indicatorAlignCenter()` | — | 指示条居中 |
| `indicatorAlignAspectRatio()` | — | 指示条按比例（默认） |

---

### HoverView — 吸顶视图

```kotlin
ScrollerView.Hover { }    // 作为 Scroller 直接子视图
```

**HoverAttr 额外方法**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `bringIndex(index)` | `Int` | 多个 HoverView 时的置顶层级 |
| `hoverMarginTop(offset)` | `Float` | 悬停时距顶部偏移 |

---

### Modal — 浮层/弹窗

```kotlin
Modal(inWindow = false) { }
// inWindow = false: 挂载到 Pager 根节点
// inWindow = true:  挂载到窗口顶层（覆盖整屏）
```

---

### Blur — 毛玻璃

```kotlin
Blur { }    // BlurView，继承 BlurAttr
```

**BlurAttr**

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `blurRadius(radius)` | `Float` (最大12.5f) | 模糊半径 |
| `targetBlurViewNativeRefs(refs)` | `List<Int>` | 指定目标 View（Android 性能优化） |
| `blurOtherLayer(blur)` | `Boolean` | Android：是否模糊其他 Layer |

---

### 其他视图

```kotlin
ActivityIndicator { }    // 加载指示器
SafeArea { }             // 安全区域容器
Mask { }                 // 遮罩视图
APNG { }                 // APNG 动图
PAGView { }              // PAG 动效
```

---

## 通用属性 Attr

所有视图的 `attr {}` 块均可调用。

### 尺寸定位

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `width(width)` | `Float` | 宽度 |
| `height(height)` | `Float` | 高度 |
| `size(width, height)` | `Float, Float` | 同时设置宽高 |
| `maxWidth/maxHeight(v)` | `Float` | 最大宽/高 |
| `minWidth/minHeight(v)` | `Float` | 最小宽/高 |
| `flex(flex)` | `Float` | 主轴填充比例 |
| `positionAbsolute()` | — | 绝对定位 |
| `positionRelative()` | — | 相对定位（默认） |
| `absolutePositionAllZero()` | — | 绝对定位填满父容器 |
| `absolutePosition(top,left,bottom,right)` | `Float×4` | 绝对定位四边（可省略） |
| `top/left/bottom/right(v)` | `Float` 或 `Percentage` | 定位值 |

### 外边距

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `margin(all)` | `Float` | 四边等距 |
| `margin(top,left,bottom,right)` | `Float×4` | 各方向（可省略） |
| `marginTop/Left/Bottom/Right(v)` | `Float` | 单边 |

### 样式

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `backgroundColor(color)` | `Color` 或 `Long` | 背景色 |
| `backgroundLinearGradient(direction, vararg stops)` | `Direction, ColorStop...` | 线性渐变（至少2个stop） |
| `borderRadius(v)` | `Float` | 统一圆角 |
| `borderRadius(tl,tr,bl,br)` | `Float×4` | 四角分别设置 |
| `border(border)` | `Border(width, style, color)` | 边框 |
| `boxShadow(shadow)` | `BoxShadow` | 阴影 |
| `boxShadow(shadow, useShadowPath)` | `BoxShadow, Boolean` | 阴影（iOS shadowPath 优化） |
| `opacity(opacity)` | `Float` (0~1) | 透明度 |
| `visibility(visible)` | `Boolean` | 可见性 |
| `overflow(clipChild)` | `Boolean` | 裁剪超出内容 |
| `zIndex(zIndex)` | `Int` | 层叠顺序 |

### 变换

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `transform(rotate)` | `Rotate` | 旋转 |
| `transform(scale)` | `Scale` | 缩放 |
| `transform(translate)` | `Translate` | 位移 |
| `transform(skew)` | `Skew` | 倾斜 |
| `transform(rotate,scale,translate,anchor,skew)` | 组合 | 完整变换 |

### 动画绑定

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `animate(animation, value)` | `Animation, Any` | 新接口：值变化时动画插值 |
| `animation(animation, value)` | `Animation, Any` | 旧接口（勿混用） |

### 交互

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `touchEnable(enable)` | `Boolean` | 触摸响应 |
| `preventTouch(enable)` | `Boolean` | 阻止触摸透传 |
| `capture(vararg rule)` | `CaptureRule?...` | 优先捕获手势 |

### 对齐（自身）

| 方法 | 说明 |
|-----|------|
| `alignSelf(align)` | 自身交叉轴对齐 |
| `alignSelfCenter/FlexStart/FlexEnd/Stretch()` | 快捷方式 |

### 无障碍与调试

| 方法 | 参数 | 说明 |
|-----|-----|------|
| `accessibility(text)` | `String` | 无障碍描述 |
| `accessibilityRole(role)` | `AccessibilityRole` | 无障碍角色 |
| `testTag(tag)` | `String` | 测试标签 |
| `debugName(name)` | `String` | UI Inspector 显示名 |
| `keepAlive(keep)` | `Boolean` | List 中不回收 |
| `autoDarkEnable(enable)` | `Boolean` | 自动暗黑模式 |
| `interfaceStyle(style)` | `InterfaceStyle` | 强制界面样式（iOS） |

---

## 容器属性 ContainerAttr

`View/Scroller/List` 等容器视图额外提供，内边距和 FlexBox 布局方法。

### FlexBox

| 方法 | 说明 |
|-----|------|
| `flexDirectionColumn()` | 竖向排列（默认） |
| `flexDirectionRow()` | 横向排列 |
| `flexWrapNoWrap()` | 不换行 |
| `flexWrapWrap()` | 自动换行 |
| `justifyContentCenter()` | 主轴居中 |
| `justifyContentFlexStart()` | 主轴起点 |
| `justifyContentFlexEnd()` | 主轴终点 |
| `justifyContentSpaceBetween()` | 两端对齐 |
| `justifyContentSpaceAround()` | 间隔分布 |
| `justifyContentSpaceEvenly()` | 均匀分布 |
| `alignItemsCenter()` | 交叉轴居中 |
| `alignItemsFlexStart()` | 交叉轴起点 |
| `alignItemsFlexEnd()` | 交叉轴终点 |
| `alignItemsStretch()` | 交叉轴拉伸（默认） |
| `allCenter()` | 主轴+交叉轴均居中 |
| `alignContent(align)` | 多行在交叉轴上的对齐 |

### 内边距

| 方法 | 说明 |
|-----|------|
| `padding(all)` | 四边等距 |
| `padding(top,left,bottom,right)` | 各方向（可省略） |
| `paddingTop/Left/Bottom/Right(v)` | 单边 |

---

## 通用事件 Event

所有视图的 `event {}` 块均可调用。

| 事件 | 回调参数 | 说明 |
|-----|---------|------|
| `click(handler)` | `(ClickParams) -> Unit` | 单击 |
| `doubleClick(handler)` | `(ClickParams) -> Unit` | 双击 |
| `longPress(handler)` | `(LongPressParams) -> Unit` | 长按 |
| `pan(handler)` | `(PanGestureParams) -> Unit` | 滑动手势 |
| `pinch(handler)` | `(PinchGestureParams) -> Unit` | 捏合手势 |
| `animationCompletion(handler)` | `(AnimationCompletionParams) -> Unit` | 动画结束 |
| `mouseEnter(handler)` | `() -> Unit` | 鼠标进入（macOS） |
| `mouseExit(handler)` | `() -> Unit` | 鼠标离开（macOS） |

---

## Pager 类

`@Page("PageName") class MyPage : BasePager()`

### 核心属性

| 属性 | 类型 | 说明 |
|-----|-----|------|
| `pageData` | `PageData` | 设备+页面参数（响应式，宽高变化自动重渲染） |
| `pagerData` | `PageData` | 等同 pageData（ComposeView 内使用） |
| `lifecycleScope` | `LifecycleScope` | 协程作用域（绑定页面生命周期） |
| `isAppeared` | `Boolean` | 当前是否可见 |

### 可重写生命周期

```kotlin
open fun created()              // 解析 pageData.params，初始化状态
open fun viewWillLoad()         // body() 前
// [body() 执行]
open fun viewDidLoad()          // body() 后，可命令式操作
open fun viewDidLayout()        // 首次布局完成
open fun pageDidAppear()        // 每次页面出现
open fun pageDidDisappear()     // 每次页面消失
open fun pageWillDestroy()      // 取消协程、释放资源
open fun viewWillUnload()
open fun viewDidUnload()
open fun viewDestroyed()
open fun themeDidChanged(data: JSONObject)
```

### 模块访问

```kotlin
acquireModule<RouterModule>(ModuleConst.ROUTER)           // 不存在抛异常
getModule<SharedPreferencesModule>(ModuleConst.SHARED_PREFERENCES)  // 不存在返回 null
```

### 任务调度

```kotlin
addNextTickTask { }                   // 下一 tick
addTaskWhenPagerUpdateLayoutFinish { } // 布局完成后
```

### ViewRef

```kotlin
private var myRef: ViewRef<ListView<*, *>>? = null

// body() 内绑定
List { ref { myRef = it } }

// 命令式调用
myRef?.view?.setContentOffset(0f, 0f, animated = true)
```

---

## PageData 字段

| 字段 | 类型 | 响应式 | 说明 |
|-----|-----|:----:|------|
| `params` | `JSONObject` | — | 页面传入参数 |
| `pageViewWidth` | `Float` | ✅ | 根视图宽度（dp） |
| `pageViewHeight` | `Float` | ✅ | 根视图高度（dp） |
| `statusBarHeight` | `Float` | — | 状态栏高度 |
| `navigationBarHeight` | `Float` | — | 导航栏高度（statusBar+44） |
| `safeAreaInsets` | `EdgeInsets` | ✅ | 安全区域（top/bottom/left/right） |
| `density` | `Float` | — | 屏幕密度（默认3.0） |
| `platform` | `String` | — | `"android"/"iOS"/"macOS"/"ohos"` |
| `isIOS` / `isAndroid` / `isMacOS` / `isOhOs` | `Boolean` | — | 平台判断 |
| `isIphoneX` | `Boolean` | — | `isIOS && statusBarHeight > 30` |
| `appVersion` | `String` | — | 宿主 App 版本 |
| `nativeBuild` | `Int` | — | native 构建版本 |
| `deviceWidth/Height` | `Float` | ✅ | 设备屏幕宽高 |
| `activityWidth/Height` | `Float` | ✅ | Activity 宽高（nativeBuild≥8） |

---

## ComposeView 类

```kotlin
class MyView : ComposeView<MyAttr, MyEvent>() {
    override fun createAttr() = MyAttr()
    override fun createEvent() = MyEvent()
    override fun body(): ViewBuilder { … }
}

fun ViewContainer<*, *>.MyView(init: MyView.() -> Unit) {
    addChild(MyView(), init)
}
```

### 核心方法

```kotlin
emit(eventName: String, param: Any?)    // 向父组件发射自定义事件
getViewAttr(): A                         // 获取 Attr 对象
getViewEvent(): E                        // 获取 Event 对象
animateToAttr(animation, completion?) { … }  // 命令式动画
toImage(type, sampleSize, callback)      // 截图
```

### ComposeEvent 自定义事件

```kotlin
// 父组件注册处理函数
class MyEvent : ComposeEvent() {
    fun onMyEvent(handler: (String) -> Unit) {
        registerEvent("myEvent") { data ->
            handler(data as? String ?: "")
        }
    }
}

// 子组件触发
emit("myEvent", "payload")
```

---

## 响应式系统

### 声明（在 Pager/ComposeView 内直接使用）

```kotlin
var title by observable("")                           // 标量
var count by observable(0)
var isActive by observable(false)
var items: ObservableList<T> by observableList()      // 列表
var ids: ObservableSet<String> by observableSet()     // 集合
```

### ObservableList 额外方法

```kotlin
// Myers diff 最小化更新
list.diffUpdate(newList) { a, b -> a.id == b.id }

// 所有 MutableList 方法均响应式
list.add(item); list.removeAt(0); list.clear()
list.addAll(items); list.set(index, item)
```

### bindValueChange（手动监听）

```kotlin
bindValueChange(
    valueBlock = { ctx.someValue },
    byOwner = this
) { newValue ->
    // 任意处理
}

// 清除
unbindAllValueChange(byOwner = this)
```

---

## Animation 动画

### 工厂方法

```kotlin
// 普通缓动
Animation.linear(durationS: Float)
Animation.easeIn(durationS: Float)
Animation.easeOut(durationS: Float)
Animation.easeInOut(durationS: Float)

// 弹簧动画（durationS: 时长，damping: 阻尼0~1，velocity: 初速度）
Animation.springLinear(durationS, damping, velocity)
Animation.springEaseIn(durationS, damping, velocity)
Animation.springEaseOut(durationS, damping, velocity)   // 最常用
Animation.springEaseInOut(durationS, damping, velocity)

// 键盘同步动画（iOS）
Animation.keyboard(durationS, curve = 7)
```

### 链式修饰

```kotlin
animation.delay(seconds: Float)          // 延迟开始
animation.repeatForever(forever: Boolean) // 无限循环
```

### animateToAttr — 命令式动画

```kotlin
view.animateToAttr(Animation.easeInOut(0.3f)) {
    translateY(100f)
    opacity(0f)
}

view.animateToAttr(
    animation = Animation.springEaseOut(0.4f, 0.7f, 0f),
    completion = { finished -> }
) {
    opacity(1f)
}
```

### ScrollerView 的弹簧滚动

```kotlin
val springAnim = SpringAnimation(durationMs = 300, damping = 0.7f, velocity = 0f)
scrollerRef?.view?.setContentOffset(0f, 0f, springAnimation = springAnim)

// 或线性动画
val linearAnim = SetContentOffsetAnimation.linear(durationMs = 200)
scrollerRef?.view?.setContentOffset(0f, 0f, animation = linearAnim)
```

---

## Timer 定时器

### 单次延迟

```kotlin
// 在 Pager/ComposeView 内（绑定页面生命周期）
val ref = setTimeout(300) {    // 300ms 后执行
    doSomething()
}
clearTimeout(ref)              // 取消
```

### 重复执行

```kotlin
private val timer = Timer()

override fun pageDidAppear() {
    timer.schedule(
        delay = 0,       // 首次延迟（毫秒）
        period = 1000    // 重复间隔（毫秒）
    ) {
        count++
    }
}

override fun pageWillDestroy() {
    timer.cancel()
}
```

> ⚠️ **`setInterval` / `clearInterval` 全局函数不存在于 KuiklyUI core。** 请用 `Timer` 类。

---

## 指令系统

### 条件渲染

```kotlin
vif({ condition }) { /* view */ }
velseif({ condition }) { /* view */ }
velse { /* view */ }
```

- 三者必须相邻，不能插入其他节点
- 条件必须用 lambda 包裹使其响应式

### 列表渲染（普通）

```kotlin
// 标准（只有 item）
vfor({ ctx.observableList }) { item ->
    ItemView { }  // 必须且只创建一个子视图
}

// 带索引
vforIndex({ ctx.observableList }) { item, index, count ->
    Text { attr { text("$index/$count") } }
}
```

### 虚拟化列表渲染（List 专用）

```kotlin
List {
    attr { flex(1f) }
    vforLazy({ ctx.observableList }, maxLoadItem = 30) { item, index, count ->
        ItemView { }
    }
}
```

---

## 模块系统 Modules

### ModuleConst 常量

```kotlin
ModuleConst.ROUTER             // "KRRouterModule"
ModuleConst.SHARED_PREFERENCES // "KRSharedPreferencesModule"
ModuleConst.NETWORK            // "KRNetworkModule"
ModuleConst.NOTIFY             // "KRNotifyModule"
ModuleConst.MEMORY             // "KRMemoryCacheModule"
ModuleConst.SNAPSHOT           // "KRSnapshotModule"
ModuleConst.CODEC              // "KRCodecModule"
ModuleConst.TURBO_DISPLAY      // "KRTurboDisplayModule"
ModuleConst.CALENDAR           // "KRCalendarModule"
ModuleConst.FONT               // "KRFontModule"
ModuleConst.VSYNC              // "KRVsyncModule"
ModuleConst.BACK_PRESS         // "KRBackPressModule"
ModuleConst.FILE               // "KRFileModule"
ModuleConst.REFLECTION         // "KRReflectionModule"
ModuleConst.PERFORMANCE        // "KRPerformanceModule"
```

### RouterModule

```kotlin
val router = acquireModule<RouterModule>(ModuleConst.ROUTER)
router.openPage("PageName", JSONObject().apply { put("key", "value") })
router.closePage()
router.backHandle(isConsumed = true)  // 消费返回键
```

### SharedPreferencesModule

```kotlin
val sp = acquireModule<SharedPreferencesModule>(ModuleConst.SHARED_PREFERENCES)
sp.setString("key", "value");   sp.getString("key")        // "" if missing
sp.setInt("key", 42);           sp.getInt("key")           // null if missing
sp.setFloat("key", 1.0f);       sp.getFloat("key")         // null if missing
sp.setObject("key", jsonObj);   sp.getObject("key")        // null if missing
```

### NetworkModule

```kotlin
val net = acquireModule<NetworkModule>(ModuleConst.NETWORK)

net.httpRequest(
    url = "https://...",
    isPost = false,
    param = JSONObject(),
    headers = null,
    timeout = 30
) { data, success, errorMsg, response ->
    // 注意：回调在子线程
}

net.requestGetBinary(url, param) { bytes, success, errorMsg, response -> }
```

### NotifyModule

```kotlin
val notify = acquireModule<NotifyModule>(ModuleConst.NOTIFY)

val ref = notify.addNotify("eventName") { data: JSONObject? -> }
notify.removeNotify("eventName", ref)
notify.postNotify("eventName", JSONObject(), crossProcess = false)
```

### 自定义 Module

```kotlin
class MyModule : Module() {
    override fun moduleName() = "MyModule"

    fun doSomething(callback: CallbackFn) {
        asyncToNativeMethod("doSomething", null, callback)
    }
}

// 注册
override fun createExternalModules() = mapOf("MyModule" to MyModule())

// 使用
acquireModule<MyModule>("MyModule").doSomething { data -> }
```

---

## Color 颜色

```kotlin
// 构造
Color(0xFF8B0000)                    // Long（ARGB，最常用）
Color(0xFF8B0000.toInt())            // Int（等价，某些溢出情况需要）
Color(255, 139, 0, 1.0f)            // RGBA 分量（R,G,B 0-255, alpha 0.0-1.0）
Color(0xFF8B0000, alpha01 = 0.5f)   // Long + 单独指定 alpha

// 预定义常量
Color.WHITE     = Color(0xffFFFFFFL)
Color.BLACK     = Color(0xff000000L)
Color.RED       = Color(0xffFF0000L)
Color.BLUE      = Color(0xff0000FFL)
Color.GREEN     = Color(0xff00FF00L)
Color.YELLOW    = Color(0xffFFFF00L)
Color.GRAY      = Color(0xff999999L)
Color.TRANSPARENT = Color(0x00000000L)

// 方法
color.opacity(0.5f)   // 返回新 Color，alpha = 0.5（不修改原 Color）
color.hexColor        // 读取 ARGB Long 值
```

---

## ViewConst 常量

```kotlin
// native 组件名（viewName() 返回值）
ViewConst.TYPE_VIEW          = "KRView"
ViewConst.TYPE_TEXT          = "KRTextView" (对应 "TextView")
ViewConst.TYPE_IMAGE         = "KRImageView"
ViewConst.TYPE_RICH_TEXT     = "KRRichTextView"
ViewConst.TYPE_LIST          = "KRListView"
ViewConst.TYPE_SCROLLER      = "KRScrollView"
ViewConst.TYPE_CANVAS        = "KRCanvasView"
ViewConst.TYPE_TEXT_FIELD    = "KRTextFieldView"
ViewConst.TYPE_TEXT_AREA     = "KRTextAreaView"
ViewConst.TYPE_BLUR_VIEW     = "KRBlurView"
ViewConst.TYPE_MODAL_VIEW    = "KRModalView"
```

---

## 数据类型参考

| 类型 | 构造 | 说明 |
|-----|-----|------|
| `Border` | `Border(lineWidth, BorderStyle.SOLID, color)` | 边框；BorderStyle: SOLID/DOTTED/DASHED |
| `BorderRectRadius` | `BorderRectRadius(tl, tr, bl, br)` | 四角圆角 |
| `BoxShadow` | `BoxShadow(offsetX, offsetY, radius, color, fill=true)` | 阴影 |
| `ColorStop` | `ColorStop(color, stopIn01)` | 渐变色节点（位置 0~1） |
| `Direction` | `Direction.TO_BOTTOM/TOP/LEFT/RIGHT/TO_BOTTOM_RIGHT/...` | 渐变方向（8个） |
| `Rotate` | `Rotate(angle=0, xAngle=0, yAngle=0)` | 旋转（z/x/y 轴，角度） |
| `Scale` | `Scale(x=1f, y=1f)` | 缩放 |
| `Translate` | `Translate(percentageX=0, percentageY=0, offsetX=0, offsetY=0)` | 位移 |
| `Anchor` | `Anchor(x=0.5f, y=0.5f)` | 变换锚点 |
| `Skew` | `Skew(horizontalAngle=0, verticalAngle=0)` | 倾斜（-90~90 度） |
| `Percentage` | `Percentage(50f)` = 50% | 百分比单位（用于定位） |
| `EdgeInsets` | `EdgeInsets(top, left, bottom, right)` | 矩形内边距 |
| `Frame` | `Frame(x, y, width, height)` | 矩形区域 |
| `Size` | `Size(width, height)` | 尺寸 |
| `CaptureRule` | `CaptureRule.click(area?)` / `.longPress(area?)` / `.pan(dir, area?)` | 手势捕获规则 |
| `InterfaceStyle` | `InterfaceStyle.AUTO/LIGHT/DARK` | 界面样式（iOS） |
| `AccessibilityRole` | `BUTTON/TEXT/IMAGE/CHECKBOX/SEARCH/NONE` | 无障碍角色 |
| `TextInputState` | `TextInputState(text, selectionStart, selectionEnd, composingStart, composingEnd)` | 输入框完整状态 |
| `SpringAnimation` | `SpringAnimation(durationMs, damping, velocity)` | 弹簧动画（Scroller 滚动） |
