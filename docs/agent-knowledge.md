# Agent knowledge

Pitfalls confirmed on real TVs. Each entry says where the evidence came from. Add entries only with evidence.

- **Force-stopping the launcher switches off its accessibility services.** Restarting Home with `am force-stop` removes `com.gathin.tvhome/...HomeKeyService` from the enabled list, and the service stays unbound. Put the list back and switch accessibility off and on again, as `apply-theme.sh` does. Evidence: a Skyworth TV, Android 11, after a reinstall; the service showed as crashed until it was re-enabled.
- **`content read` can return "No result found" on the first query.** Reading `content://com.gathin.tvhome.theme/config.json` worked on the next attempt. Evidence: the same TV.
- **Load average looks alarming and means little here.** A load average near 21 came with about 80% idle CPU. Judge speed by CPU idle, free memory and animation time, not load average. Evidence: the same TV.
- **Only the first animation scale is recorded.** A second `speed.sh` run would otherwise record the shortened value as the original. Covered by `tests/test_scripts_dryrun.py`.
- **`adb shell` reads stdin.** Calls inside a loop would eat the loop's input, so `lib.sh` redirects stdin from `/dev/null`. This is a precaution; it was not reproduced here.
- **The model string can differ from the box.** A TV sold under one brand can report another in `ro.product.brand`. Use `connect.sh` output, not the box, to choose a path.
- **The launcher's accent colour is festival-only.** `accent_color` is read only with the festival skin (`Festival.java`, `MainActivity.java`). Themes therefore differ by background, layout, dock and motion. A global accent needs a launcher change.
- **Wallpapers are out of scope for v1.** The presets set `wallpaper: false`; `tests/test_theme_merge.py` enforces it.
