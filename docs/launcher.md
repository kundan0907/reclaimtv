# The launcher

`launcher/` is the home screen that themes and options configure. It started from Clean Your TV (https://github.com/Gathin23/cleanyourtv, MIT, Copyright 2026 Gathin), at commit `b7aaa8870b234958c5656805621a4d42ac3989c7`. The licence is kept at `launcher/LICENSE-CLEANYOURTV`. Image credits: `launcher/THEME-CREDITS.md`.

The package name stays `com.gathin.tvhome`, because the theme provider and the scripts use it.

## What was added to the upstream launcher

- Glass app tiles, a glass dock and dock magnification.
- Calendar (weekly or monthly) and days-left-in-year cards under the top icons, and a weather card.
- A widget row in the last app row, with a continue-watching tile.
- Light mode, theme-coloured glow, and 16 bundled themes with wallpapers.
- Theme rotation and screensaver widgets.
- Console tiles for HDMI devices, such as a PlayStation.
- Widget size (small, medium, large).
- A SmartTube banner and glyph.
- Pressing Home pauses any playing media, so an app does not keep playing behind the home screen.

## Build and install

The new features are in the launcher app, not in the theme files. Build and install the APK once, or none of the options below will do anything.

You need JDK 17 and the Android SDK for API 34.

```bash
cd launcher
./build-and-install.sh <tv-ip>
```

On Windows use `build-and-install.ps1`. Or build by hand with `./gradlew assembleRelease` and `adb -s <tv>:5555 install -r app/build/outputs/apk/release/app-release.apk`. The release build is signed with the debug key unless you add your own signing config. The APK is not in this repo.

## Options

Pick them with `scripts/launcher_options.py`, or write the keys yourself (`themes/README.md`). Every option is off unless the user turns it on. Some choices live on the TV, in the gear menu, and cannot be pushed from a computer.

| Option | Flag | Key, or where to set it | Default | Undo |
|---|---|---|---|---|
| Theme | `--theme <id>` | Presets in `themes/`; bundled themes are chosen on the TV: gear > Theme | launcher's own | gear > Theme, or `apply-theme.sh --reset` |
| Light mode | `--light` | `light_mode` | off | `light_mode: false` |
| Weather | `--weather` | `weather` | off | `weather: false` |
| Calendar and days-left cards | `--year-widget` | `month_card` | off | `month_card: false` |
| Calendar mode | `--calendar-mode weekly\|monthly` | On the TV: press OK on the calendar, then Left (weekly) or Right (monthly) | weekly | same keys |
| Widget row | `--widget-row` | `widget_row` | off | `widget_row: false` |
| Widget size | `--widget-size small\|medium\|large` | On the TV: gear > Launcher settings > Widget size | medium | same menu |
| Dock magnification | `--magnify` | `dock_magnify` | off | `dock_magnify: false` |
| Glass icons | `--glass-icons`, `--original-icons` | `glass_icons`, or gear > Launcher settings > Icons | the launcher's own | same menu |
| Icon-only top bar | | `icon_only_buttons` | off | `icon_only_buttons: false` |
| Theme rotation | `--rotation 0\|15\|60\|1440` | On the TV: gear > Launcher settings > Theme rotation | off | choose Off |
| Console tile | `--console PlayStation` | `input_tiles`, and `input:PlayStation` in `dock_apps` | none | remove the keys |
| Continue-watching picture | `--continue-watching` | Notification access (below) | off | revoke the access |

`scripts/launcher_options.py --list-themes` lists every theme. Add `--out mytv` to write `mytv/config.json`, then push it:

```bash
scripts/launcher_options.py --theme northern --weather --year-widget --widget-row --magnify --glass-icons --out mytv
scripts/apply-theme.sh <tv-ip> mytv --dry-run
scripts/apply-theme.sh <tv-ip> mytv
```

A bundled theme id carries its colours into `config.json`. Its wallpaper is inside the APK, so choose the same theme on the TV (gear > Theme) to get the picture.

### Console tile

Only add this if the user has a PlayStation, Xbox or Switch on HDMI. The tile shows while that device is connected and on (HDMI-CEC). An entry is `Label|match`, where `match` is part of the device name the TV reports. `--console PlayStation` writes `PlayStation|PlayStation`. Put `input:PlayStation` in `dock_apps` to dock it.

### Privacy

- **Weather** needs the internet. It sends the TV's public IP to ipwho.is once a day to get an approximate location (rounded to about 1 km), and asks Open-Meteo for the forecast every 30 minutes or so. No account and no key. Ask before enabling.
- **Continue-watching picture** shows the picture and title of what was last played. It reads the media sessions that apps publish, which needs notification access for the launcher. A YouTube app publishes only a video id, so the launcher then fetches YouTube's public thumbnail and title for that id. Ask before enabling. Grant it with:

```bash
adb -s <tv-ip>:5555 shell cmd notification allow_listener com.gathin.tvhome/com.gathin.tvhome.MediaWatcher
```

Revoke it with `cmd notification disallow_listener` and the same component, or in the TV's notification-access settings.

## Themes

Themes are overlays on the launcher, plus 16 that are built in. See `themes/README.md`.
