# tv-cleanup

A skill for Claude Code and Codex that cleans up an Android TV or Google TV: it removes apps you do not use (reversibly), shortens animations, sets up IPTV in OwnTV, installs SmartTube, and applies a launcher theme you choose.

Ask your assistant: "clean up my TV" or "set up IPTV on my TV". It asks a short questionnaire, shows the plan, and then runs it.

## What you get

| Step | What it does | Undo |
|---|---|---|
| Debloat | Disables Google and TV-maker apps you do not use (`lists/`) | `scripts/revert.sh` |
| Speed | Halves animation scales | `scripts/revert.sh` |
| Launcher and theme | Dark, Batman, Tesla, Aurora or Compact (`themes/`) | `scripts/apply-theme.sh <tv> --reset` |
| IPTV | Public channels from iptv-org, checked and filtered for OwnTV (`iptv/`) | Remove the playlist in OwnTV |
| SmartTube | Installed from its official release (`docs/smarttube.md`) | Uninstall it |
| Free movies | Legal free sources (`docs/free-movies.md`) | Not applicable |

Nothing is uninstalled. The Play Store and Downloader are never touched (`lists/keep.txt`). See `docs/safety.md`.

## Screenshots

![Home screen on the author's TV: the dock, the top bar and no wallpaper](docs/screenshots/home-no-wallpaper.png)

More screenshots and how to take your own: `docs/screenshots/README.md`.

## Quick start

1. Install the skill (see `SKILL.md`): `~/.claude/skills/tv-cleanup` for Claude Code, or `~/.agents/skills/tv-cleanup` for Codex.
2. Turn on ADB on the TV (`docs/setup.md`).
3. Ask your assistant to clean up the TV, then answer the questions.

Run the scripts by hand if you prefer:

```bash
scripts/connect.sh 192.0.2.10
scripts/debloat.sh 192.0.2.10 light --dry-run
scripts/debloat.sh 192.0.2.10 light
scripts/apply-theme.sh 192.0.2.10 themes/dark
```

## Tests

```bash
python3 -m unittest discover -s tests
```

The tests use a fake `adb`, so they do not need a TV. They check the keep list, the setup-wizard and HOME guards, dry-run behaviour, the undo record and the theme checker.

## Status

- Tested: the scripts' dry-run and undo paths (fake ADB), the theme checker, and the playlist verifier (a local test server).
- Not tested here: a real TV run of these scripts, and the launcher build (`docs/launcher.md`). Test on your own TV first.

## Credits

- **Clean Your TV** by Gathin (MIT): the launcher in `launcher/`, and the safety rules that came from its device notes. See `NOTICE`.
- **iptv-org** (Unlicense): the playlist index used at run time.
- **OwnTV**, **SmartTube**: players you install separately from their own projects.

## License

MIT. See `LICENSE` and `NOTICE`.
