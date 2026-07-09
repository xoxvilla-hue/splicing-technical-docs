# CLAUDE.md

Guidance for AI assistants working in this repository.

## What this is

**Catch the Fruit** (จับผลไม้) — a small single-screen Android game for kids, built
with **Kotlin + Jetpack Compose (Material 3)**. The player drags a basket 🧺
left/right to catch falling fruit for points while dodging bombs 💣.

> Note: the Git repository is named `splicing-technical-docs`, but the contents
> are an Android game. Trust the code, not the repo name.

The entire game is intentionally tiny — essentially one Compose file — and is
designed to be forgiving for young players (missing fruit costs nothing; only
catching a bomb costs a life). Much of the source contains **Thai-language
comments and UI strings**; preserve Thai text and its meaning when editing.

## Layout

```
.
├── build.gradle.kts            # Root build (plugins declared, not applied)
├── settings.gradle.kts         # Repo config; rootProject "CatchFruit", includes :app
├── gradle.properties           # JVM args, AndroidX flags, caching
├── gradle/
│   ├── libs.versions.toml       # Version catalog — the single source of dependency versions
│   └── wrapper/                 # Gradle 8.14.3 wrapper
├── gradlew / gradlew.bat       # Wrapper scripts — always build via these
└── app/                        # The only module
    ├── build.gradle.kts         # Android + Compose config, dependencies
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/example/catchfruit/
        │   ├── MainActivity.kt   # Entry point: sets up Compose, calls GameScreen()
        │   └── GameScreen.kt     # ALL game logic lives here
        └── res/                  # Icons, colors, theme, strings (app_name is Thai)
```

## Where things live

Nearly everything meaningful is in **`app/src/main/java/com/example/catchfruit/GameScreen.kt`**.
Understand this file before making game changes:

- **`Phase` enum** (`START`, `PLAYING`, `GAME_OVER`) drives which UI is shown.
- **`FallingItem` data class** — an immutable falling object (fruit or bomb):
  `xFraction` (0f–1f horizontal position), `y` (px from top), `emoji`,
  `isBomb`, `speed` (px/sec).
- **`GameScreen()`** holds all state via `remember`/`rememberSaveable`
  (`best` score survives config changes) and contains:
  - The **game loop** — a `LaunchedEffect(phase)` running `withFrameNanos`,
    frame-rate independent via `dt`. It spawns items, moves them, resolves
    catches at the `catchLine`, and updates score/lives.
  - **Input** — a `pointerInput` block that maps drag X to `basketCenterX`.
  - **Rendering** — items and basket are drawn as **emoji `Text`** at computed
    `IntOffset`s; there are no image assets.
- **`Overlay()`** — the shared composable for the START and GAME_OVER screens.

Key gameplay rules baked into the loop:
- Difficulty scales with score (`1f + score / 30f`): faster spawns, faster fall.
- ~18% of spawns are bombs.
- Catching fruit `+1`; catching a bomb `-1` life (start with 3).
- **Missing fruit is never penalized** — deliberate, keep it that way unless asked.

`MainActivity.kt` is a thin shell: `enableEdgeToEdge()`, `MaterialTheme`,
`Surface`, then `GameScreen()`. App is locked to **portrait** in the manifest.

## Conventions

- **Language/UI:** Kotlin, Jetpack Compose only (no XML layouts, no Views).
- **Dependencies:** always add/bump versions through the **version catalog**
  (`gradle/libs.versions.toml`) and reference them as `libs.*` in
  `app/build.gradle.kts`. Do not hardcode versions in the module build file.
- **Compose versions** come from the BOM (`androidx-compose-bom`); individual
  Compose artifacts are declared without explicit versions.
- **State:** use Compose state (`mutableStateOf`, `mutableIntStateOf`,
  `mutableFloatStateOf`); use `rememberSaveable` only for things that must
  survive recomposition/rotation (e.g. `best`).
- **No assets:** characters are emoji strings. Add new fruit by extending the
  `FRUITS` list in `GameScreen.kt`.
- **Comments/strings:** existing comments are bilingual (Thai + English) and
  user-facing strings are Thai. Match that style; keep player-facing copy
  child-friendly.
- **Package:** `com.example.catchfruit` (namespace + applicationId).

## Build & run

There are **no tests, no lint config, and no CI** in this repo. "Verifying"
means it compiles and runs. Building requires the **Android SDK**; create a
`local.properties` with `sdk.dir=/path/to/Android/Sdk` (it is gitignored).

Always use the wrapper:

```bash
./gradlew assembleDebug     # Build debug APK -> app/build/outputs/apk/debug/
./gradlew installDebug      # Install onto a connected device/emulator
./gradlew build             # Full build (compiles release too)
```

Easiest path is opening the project folder in **Android Studio** and pressing
Run ▶ against a device/emulator.

Toolchain (pinned): Gradle **8.14.3**, Android Gradle Plugin **8.7.3**,
Kotlin **2.0.21**, Java **17**, `compileSdk`/`targetSdk` **34**,
`minSdk` **26** (Android 8.0). Compose BOM **2024.10.01**.

> The build environment here may not have the Android SDK installed. If a
> Gradle build fails only because the SDK/`local.properties` is missing, that is
> an environment limitation — say so rather than treating it as a code defect.

## Working agreements

- Keep the game small and self-contained; prefer editing `GameScreen.kt` over
  adding new files/modules unless the change genuinely warrants it.
- Preserve the kid-friendly design decisions (no penalty for misses, 3 lives,
  gentle difficulty ramp) unless the user asks to change them.
- After changing gameplay, re-read the game loop to confirm the difficulty,
  spawn, and catch math still hold together.

## Git workflow

- Development branch for this work: `claude/claude-md-docs-tmxy7h`.
- Commit with clear messages and push with `git push -u origin <branch>`.
- Do **not** open a pull request unless explicitly asked.
