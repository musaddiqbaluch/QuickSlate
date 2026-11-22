# Quick Slate

Exported from CodeAssist as a Gradle project. The build files here were generated from
the project model, so they are a faithful starting point rather than a build that was
ever run: open it in Android Studio (or run `gradle build`) and expect to adjust it.

## What it was generated with

- Android Gradle plugin 8.13.0
- Kotlin 2.4.0
- Gradle 8.13, through the wrapper included here (`./gradlew build`; on Linux or
  macOS run `chmod +x gradlew` first, since a zip keeps no executable bit)

Kotlin is pinned to the version CodeAssist compiled this project with, so the sources
build the same way there. It is newer than the D8/R8 this AGP bundles, which warns
about Kotlin metadata it cannot rewrite; raising the AGP version clears that.

## Modules

- `:app` (app): Android app

## Notes

- Module 'app' signs its 'release' build with the 'picked-1ac7b684-my-official-keystore-366225667640812468' keystore, which lives in the app keystore registry rather than in the project. Add a signingConfigs block and your keystore to sign it with Gradle.

The Android SDK location is not exported: Android Studio writes `local.properties`
on the first sync, or set `sdk.dir` there yourself.
