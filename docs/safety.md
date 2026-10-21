# Safety

Each rule below exists because a real TV broke without it.

## What the scripts change

- **Disable, never uninstall.** `debloat.sh` runs `pm disable-user --user 0`. `revert.sh` runs `pm enable --user 0` for every package recorded as disabled. Packages already disabled before a run are not recorded, so revert leaves them alone.
- **Animation scales.** `speed.sh` records the first value it sees and restores it on revert. A second run does not overwrite the record.
- **Theme overlay.** `apply-theme.sh` writes one `config.json` the launcher reads over its built-in settings. `--reset` removes it.
- **Nothing else.** No root, no firmware flashing, no bootloader changes, no app installs.

## Guards

- **`lists/keep.txt`** is checked first. The Play Store and Downloader are on it, so you can always install apps again. The launcher and the stock launcher are on it too. Lists in `lists/` cannot override it.
- **Setup wizard (`com.google.android.tungsten.setupwraith`).** Disabling it can delete the Wi-Fi it saved during setup, which takes the TV off the network. It is on the keep list and skipped without `--allow-setupwraith`.
- **The app that answers HOME.** Disabling it can leave Home with nothing to open, and on Google TV the setup wizard can take over and crash the system. Skipped unless you pass `--allow-home`.
- **Package names.** Every list entry must match a Java-style package name. A list with anything else stops before the TV is touched. Entries are never pasted into a shell unvalidated.
- **TV address.** Must look like an IP or host name, optionally with a port. Anything else is refused before ADB runs.
- **Dock order.** `apply-theme.sh` replaces the dock only with `--dock`. The questionnaire asks first, because a dock the user arranged is theirs.
- **Windows files.** Lists may have a BOM or CRLF line endings; both are stripped.

## ADB exposure

On some firmware (`ro.adb.secure=0`), any device on your network can open a shell on port 5555 without a prompt. `connect.sh` warns about this.

- Turn ADB on only while cleaning up, and off afterwards.
- Use a trusted network. Never forward port 5555 from your router.

## Legal and scope

- IPTV playlists come from the iptv-org index of publicly available streams. Only add channels you are allowed to watch. Do not buy IPTV from resellers who sell unlicensed streams.
- This project does not document, support or distribute apps that stream unlicensed films. For free films, see `docs/free-movies.md`.
