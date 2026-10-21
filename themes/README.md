# Themes

A theme is a folder with a `config.json`. `scripts/apply-theme.sh` merges it with the dock you choose and pushes it to the launcher, which overlays it on its built-in settings. Nothing is rebuilt.

| Theme | Look | Layout |
|---|---|---|
| `dark` | Near-black background, glow behind the dock | 5 columns, dock title shown |
| `batman` | Black, no glow, quiet names | 5 columns, dock title shown |
| `tesla` | Pure black, minimal, icon-only top buttons | 4 columns, no dock title |
| `aurora` | Navy background, names always shown, sky backdrop | 5 columns |
| `compact` | Black, more apps on screen, stats line | 6 columns, two grid rows under the dock |

Presets ship without wallpapers (v1). The launcher APK must be built and installed for the keys below the first table to do anything (`docs/launcher.md`). Colour comes from `background_color`. The launcher's accent colour is used only with its festival skin, so the presets do not set one.

## Bundled themes

These are built into the launcher APK, each with its own wallpaper (credits: `launcher/THEME-CREDITS.md`). Choose one on the TV: gear > Theme. `scripts/launcher_options.py --theme <id>` copies a theme's colours and light mode into a `config.json`; it cannot set the wallpaper.

| id | Name | Accent | Mode |
|---|---|---|---|
| `aurora` | Aurora | `#3FD0C0` | dark |
| `aurora-light` | Aurora · Light | `#1FA896` | light |
| `carbon` | Carbon | `#4DA3FF` | dark |
| `daylight` | Daylight | `#5B8DEF` | light |
| `gotham` | Gotham | `#A9B4C8` | dark |
| `ironman-arc` | Iron Man · Arc reactor | `#35D6F5` | dark |
| `mountains` | Mountains | `#8D99AE` | dark |
| `mountains-light` | Mountains · Light | `#6B7A90` | light |
| `northern` | Northern Lights | `#52E08A` | dark |
| `panther-vibranium` | Black Panther · Vibranium | `#8A5CF5` | dark |
| `rcb` | RCB | `#E0252B` | dark |
| `rosso` | Rosso Corsa | `#E03A3A` | dark |
| `rosso-light` | Rosso Corsa · Light | `#C9302C` | light |
| `sunset` | Sunset | `#FF8A5B` | dark |
| `synthwave` | Synthwave | `#FF4FD8` | dark |
| `violet` | Violet Aurora | `#C65BFF` | dark |

The presets above (`dark`, `batman`, `tesla`, `aurora`, `compact`) are separate overlay folders. `aurora` exists in both places; `launcher_options.py` uses the folder.

Fan-art wallpapers (Iron Man, Black Panther) are not included. `ironman-arc` and `panther-vibranium` use generated artwork.

## Keys

Every key is optional. An unknown key or a wrong type is dropped with a warning, and the rest of the theme still applies. The launcher behaves the same way.

| Key | Type | Meaning |
|---|---|---|
| `dock_apps` | list of packages | Dock apps, in order; missing apps are skipped |
| `hidden_apps` | list of packages | Never shown as tiles |
| `background_color` | `#RRGGBB` | Colour behind everything |
| `columns` | 1 to 8 | Tiles per row |
| `rows_below_dock` | 0 to 3 | Grid rows visible under the dock |
| `dock_title` | bool | Name of the focused dock app above the dock |
| `dock_row` | bool | Dock as its own row |
| `always_show_names` | bool | Names under every tile |
| `ambient_glow` | bool | Glow behind the dock in the focused app's colour |
| `sky_intensity` | 0 to 100 | Clock-driven dark backdrop; 0 hides it |
| `animations` | bool | Basic focus and entrance motion |
| `reduced_motion` | bool | Calmer motion for weak GPUs |
| `advanced_effects` | bool | Lifted tiles and icon zoom |
| `icon_only_buttons` | bool | Wi-Fi and Settings as round icon buttons |
| `show_network_pill` | bool | Wi-Fi or Ethernet pill in the top bar |
| `show_stats` | bool | One line with CPU, free memory and free storage |
| `wallpaper` | bool | Must stay `false` in v1 |
| `light_mode` | bool | Dark text on pale glass |
| `widget_accent` | `#RRGGBB` | Colour of the widgets and glow |
| `month_card` | bool | Calendar and days-left cards under the top icons |
| `weather` | bool | Weather card and line (uses the internet) |
| `widget_row` | bool | Widgets and recent apps in the last app row |
| `dock_magnify` | bool | Dock magnification and glass dock |
| `glass_icons` | bool | Glass app tiles |
| `input_tiles` | list of `Label\|match` | HDMI console tiles; dock one with `input:Label` in `dock_apps` |

The full list of keys lives in `scripts/theme_merge.py`. A key missing from it is dropped with a warning.

## Make your own

Copy `themes/dark/config.json` to a new folder, edit it, then check it:

```bash
python3 scripts/theme_merge.py themes/mytheme/config.json
```

Warnings name keys the launcher will ignore. Then push it with `scripts/apply-theme.sh <tv> themes/mytheme`.
