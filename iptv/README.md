# IPTV

Channels come from the iptv-org index (https://github.com/iptv-org/iptv, Unlicense). Nothing from it is copied into this repository. `scripts/iptv_m3u.py` downloads the playlist you ask for at run time.

## Make a playlist

```bash
python3 scripts/iptv_m3u.py fetch countries us iptv.m3u        # all channels for a country
python3 scripts/iptv_m3u.py fetch categories news iptv-news.m3u  # one category across countries
python3 scripts/iptv_m3u.py verify iptv.m3u iptv-ok.m3u --min-height 480
```

- `fetch` takes `countries`, `categories` or `languages`, then a name such as `us`, `news` or `eng`.
- `verify` keeps a channel when its playlist answers with HLS, and when its tallest variant reaches `--min-height` (a channel without a stated height is kept). Lower it to `360` for more channels; raise it for better picture only.
- Verification sends a VLC user agent, because some hosts refuse others. It takes a few minutes for a full country.

Then import `iptv-ok.m3u` into OwnTV: `docs/owntv.md`.

## Rules

- Only add channels you are allowed to watch. The index lists publicly available streams; it does not license them.
- Do not import playlists from paid IPTV resellers, however they are offered.
- Playlists are not stored in the repository. Keep your own copies out of git.
