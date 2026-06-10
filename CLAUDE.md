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

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **ExploringChina** (1775 symbols, 3445 relationships, 104 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/ExploringChina/context` | Codebase overview, check index freshness |
| `gitnexus://repo/ExploringChina/clusters` | All functional areas |
| `gitnexus://repo/ExploringChina/processes` | All execution flows |
| `gitnexus://repo/ExploringChina/process/{name}` | Step-by-step execution trace |

## CLI

| Task | Read this skill file |
|------|---------------------|
| Understand architecture / "How does X work?" | `.claude/skills/gitnexus/gitnexus-exploring/SKILL.md` |
| Blast radius / "What breaks if I change X?" | `.claude/skills/gitnexus/gitnexus-impact-analysis/SKILL.md` |
| Trace bugs / "Why is X failing?" | `.claude/skills/gitnexus/gitnexus-debugging/SKILL.md` |
| Rename / extract / split / refactor | `.claude/skills/gitnexus/gitnexus-refactoring/SKILL.md` |
| Tools, resources, schema reference | `.claude/skills/gitnexus/gitnexus-guide/SKILL.md` |
| Index, status, clean, wiki CLI commands | `.claude/skills/gitnexus/gitnexus-cli/SKILL.md` |

<!-- gitnexus:end -->
