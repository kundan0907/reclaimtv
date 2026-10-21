# Troubleshooting

## `cannot reach <tv>`

- Developer options and network debugging must be on (see `setup.md`).
- The computer and TV must be on the same network. Check the IP again.
- Accept the "Allow USB debugging?" prompt on the TV. If it keeps asking, run `adb kill-server` and connect again.
- `adb devices` should list the TV as `device`. `unauthorized` means the prompt was not accepted.

## Home does not open, or the wrong launcher shows

- Run `scripts/revert.sh <tv>`, then press Home. Re-run `debloat.sh` without the package that answered Home.
- `apply-theme.sh <tv> --reset` removes the theme overlay.

## Home key stops working after the launcher was reinstalled

The launcher's accessibility service (`HomeKeyService`) can show as crashed after a reinstall. Re-enable it:

```bash
adb -s <tv>:5555 shell settings put secure accessibility_enabled 0
adb -s <tv>:5555 shell settings put secure enabled_accessibility_services com.gathin.tvhome/com.gathin.tvhome.HomeKeyService
adb -s <tv>:5555 shell settings put secure accessibility_enabled 1
```

`apply-theme.sh` already does this when it restarts Home.

## A theme did not change

- Check it passes the checker: `python3 scripts/theme_merge.py themes/<name>/config.json`. Warnings name the keys the launcher ignores.
- Some firmware blocks writes to `Android/data`. `apply-theme.sh` falls back to the launcher's theme provider; if both fail, the output says so.

## Wi-Fi dropped

Re-join the network in Settings. Do not disable the setup wizard to fix it (see `safety.md`).

## IPTV channels fail

- Many streams are geo-blocked or offline. `iptv_m3u.py verify` checks only that the playlist answers; some streams still fail on playback.
- Try a lower `--min-height` (for example `360`), or another country or category.
