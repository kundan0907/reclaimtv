# The launcher

`launcher/` is the home screen that themes configure. It is taken from Clean Your TV (https://github.com/Gathin23/cleanyourtv, MIT, Copyright 2026 Gathin), at commit `b7aaa8870b234958c5656805621a4d42ac3989c7`. The licence is kept at `launcher/LICENSE-CLEANYOURTV`.

Changes from upstream:

- One upstream banner, for an app outside this project's scope, is left out.
- A SmartTube banner (`banner_smarttube.xml`) and glyph (`ic_glyph_smarttube.xml`) are added.

The package name stays `com.gathin.tvhome`, because the theme provider and the scripts use it.

## Build

You need JDK 17, the Android SDK for API 34, and Gradle through the included wrapper.

```bash
cd launcher
./gradlew assembleRelease
adb -s <tv>:5555 install -r app/build/outputs/apk/release/app-release.apk
```

The release build is signed with the debug key unless you add your own signing config.

Status: this build has not been run in this repository yet. If it fails, check the Gradle and SDK versions first.

## Themes

Themes are overlays on the launcher, not rebuilds. See `themes/README.md`.
