# Screenshots

Taken from the author's TV. The README shows all of them.

| File | What it shows |
|---|---|
| `stock-home-before.png` | Stock Google TV home, before the launcher and the debloat. |
| `home-gotham-after.png` | tv-cleanup launcher with the Gotham look. The wallpaper is not in this repo. |
| `home-no-wallpaper.png` | tv-cleanup launcher with a plain background. |
| `hero-themes.jpg`, `theme-cycle.gif` | Eight themes side by side, and all 16 in a loop. |
| `themes/<id>.jpg` | Each bundled theme on the home screen. |
| `features/*.jpg` | Dock, widget row, hidden dock, calendar, year card, screensaver and settings. |

Theme and feature pictures are 1280x720 JPEGs from the author's TV. The continue-watching tile shows Big Buck Bunny (Blender Foundation, CC BY 3.0).

Not used in the repo: status pages (they show IP, MAC and serial numbers), playlist or watch-history screens, and any app the author chose not to show.

## Add your own

Take a screenshot with ADB on (see `docs/setup.md`):

```bash
adb -s 192.0.2.10:5555 exec-out screencap -p > docs/screenshots/home-dark.png
```

Check it before you commit. Remove it if it shows any of these:

- a Wi-Fi name, IP address, MAC address or serial number
- playlist names, last-watched channels or watch history
- a greeting that uses your name (`greeting_name` in a theme)
- an app you have not chosen to show
