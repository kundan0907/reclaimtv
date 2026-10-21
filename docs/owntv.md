# OwnTV: IPTV player

OwnTV plays playlists; it does not supply channels. The channels come from `iptv/` (iptv-org) through `scripts/iptv_m3u.py`.

## Install

OwnTV is open source: https://github.com/ahXN00/OwnTV. Install its APK with Downloader or `adb install`, from the project's own releases.

## Load a playlist

1. Make the playlist: `python3 scripts/iptv_m3u.py verify iptv.m3u iptv-ok.m3u --min-height 480`.
2. Open OwnTV's remote companion. It runs on the TV on port 8089, and OwnTV shows the address and a PIN on screen. Open that address in a browser on your computer, enter the PIN, and upload `iptv-ok.m3u`.
3. Alternatively, in OwnTV choose **Settings → Sources & guide → Playlists** and add the file there.

Check the import on the TV. Load a few channels before you decide the playlist works.

## Limits

- No programme guide (EPG). OwnTV shows channels without a schedule.
- Playback depends on the stream and the network. `verify` checks that the playlist answers, not that the picture plays.
