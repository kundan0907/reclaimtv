# Themes

A theme is a folder with a `config.json`. `scripts/apply-theme.sh` merges it with the dock you choose and pushes it to the launcher, which overlays it on its built-in settings. Nothing is rebuilt.

| Theme | Look | Layout |
|---|---|---|
| `dark` | Near-black background, glow behind the dock | 5 columns, dock title shown |
| `batman` | Black, no glow, quiet names | 5 columns, dock title shown |
| `tesla` | Pure black, minimal, icon-only top buttons | 4 columns, no dock title |
| `aurora` | Navy background, names always shown, sky backdrop | 5 columns |
| `compact` | Black, more apps on screen, stats line | 6 columns, two grid rows under the dock |

Presets ship without wallpapers (v1). Colour comes from `background_color`. The launcher's accent colour is used only with its festival skin, so the presets do not set one.

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

The full list of keys lives in `scripts/theme_merge.py`. A key missing from it is dropped with a warning.

## Make your own

Copy `themes/dark/config.json` to a new folder, edit it, then check it:

```bash
python3 scripts/theme_merge.py themes/mytheme/config.json
```

Warnings name keys the launcher will ignore. Then push it with `scripts/apply-theme.sh <tv> themes/mytheme`.
