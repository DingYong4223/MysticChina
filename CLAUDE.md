# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android APK (standard build — uses published KuiklyUI Maven artifacts)
./gradlew :androidApp:assembleDebug

# Verify shared module compiles for Android
./gradlew :shared:compileKotlinAndroid

# Run unit tests
./gradlew :shared:testDebugUnitTest

# Full source build (enables macOS/JS platforms — requires ../KuiklyUI source)
cp settings.full.gradle.kts settings.gradle.kts
./gradlew :androidApp:assembleDebug

# Clean
./gradlew clean
```

**Build mode detection**: `settings.gradle.kts` (default) uses published Maven AAR — Android + iOS only. `settings.full.gradle.kts` adds KuiklyUI source modules from `../KuiklyUI/`, enabling macOS and JS targets. The shared module auto-detects mode via `rootProject.findProject(":core-annotations") != null`.

## Architecture

This is a **Kuikly + Kotlin Multiplatform** video editor app (剪映-style). Kuikly is a Tencent reactive UI framework with Kotlin DSL, FlexBox layout, and `observable` fields that drive automatic UI updates.

### Layer Structure

```
commonMain (Kotlin DSL Pagers/ComposeViews)
    │
    ├── bridge/       expect interfaces → platform actual implementations
    ├── pages/        full-screen Pagers (Splash → Home/Main → Preview → Editor)
    ├── player/       IVideoPlayer interface + PlayerController (state machine)
    ├── components/   reusable ComposeViews (TopBar, BottomBar, ProgressBar, …)
    ├── manager/      DraftManager — draft lifecycle
    ├── model/        VideoInfo, MediaItem, EditModels
    ├── theme/        Colors + Theme (dark-first, CapCut-inspired)
    └── util/         Constants, FormatUtil, PlatformTime (expect/actual)

androidMain  — AndroidVideoPlayer (MediaPlayer), AndroidFileBridge, SurfaceRegistry
iosMain      — IOSVideoPlayer (AVPlayer stub), IOSFileBridge stub
appleMain    — shared Apple platform code (iOS + macOS)
```

### Page Routing

Pages are registered via `@Page` KSP annotation and declared in `App.kt`. Navigation uses `KuiklyRenderAdapterManager.krRouterAdapter`:
- `openPage(pageName, pageData)` → launches a new `MainActivity` with extras
- `closePage()` → `Activity.finish()`

Default start page: `SplashPage` → `HomePage` → `MainPage` (media library) → `PreviewPage` → `EditorPage`.

### Native Bridge Pattern

Custom native views and modules are registered in `MainActivity`:
- **`VideoRenderView`** → `VideoRenderViewImpl` (Android `SurfaceView` wrapper)
- **`GalleryModule`** → `GalleryModuleExport` inner class — handles `pickVideo` via `ACTION_OPEN_DOCUMENT`, copies to cache dir, returns JSON path

`IVideoPlayer` (`player/IVideoPlayer.kt`) is the cross-platform contract. Platform factories (`PlatformPlayerFactory.kt`) provide `actual` implementations per target.

### KSP Workaround

`compileCommonMainKotlinMetadata` deletes KSP-generated metadata output before running, because Kuikly's KSP processor generates Android-style entry classes into `commonMain` metadata but that source set cannot see `IKuiklyCoreEntry` (Android-only). This is intentional — do not remove the `tasks.matching` block in `shared/build.gradle.kts`.

## Key Dependencies

| Dependency | Version | Source |
|---|---|---|
| Kotlin | 2.1.21 | — |
| KuiklyUI core | 2.0.0 | `mirrors.tencent.com` (or local `../KuiklyUI/`) |
| KuiklyUI core-render-android | 2.0.0 | same |
| AGP | 8.2.2 | — |
| KSP | 2.1.21-2.0.1 | — |

Maven repos required: `mirrors.tencent.com/nexus/repository/maven-tencent/` (Kuikly artifacts not on Maven Central).

## Package & Namespace

- Shared Kotlin package: `com.fula.exploringchina`
- Android app ID: `com.fula.exploringchina`
- iOS framework: `shared` (static, CocoaPods)
- `rootProject.name` is `"exploringchina"` in `settings.gradle.kts`
