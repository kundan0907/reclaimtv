#!/usr/bin/env python3
"""Turn the user's launcher answers into a config.json overlay for scripts/apply-theme.sh.

Usage: launcher_options.py [options]            print the overlay (notes go to stderr)
       launcher_options.py --out DIR [options]  write DIR/config.json, then: scripts/apply-theme.sh <tv> DIR
       launcher_options.py --list-themes

Only keys the launcher reads are written (scripts/theme_merge.py checks them). A few choices live on the TV, in
Settings gear > Launcher settings, not in config.json: calendar mode, widget size, theme rotation and the
continue-watching tile. For those the script prints what to do instead of writing a key.
"""
import argparse
import json
import pathlib
import sys

REPO = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(REPO / "scripts"))
import theme_merge  # noqa: E402

BUNDLED = REPO / "launcher" / "app" / "src" / "main" / "assets" / "themes"
# From a bundled theme only its look carries into config.json; its wallpaper is inside the APK.
LOOK_KEYS = ("background_color", "widget_accent", "light_mode", "ambient_glow")
ROTATION = (0, 15, 60, 1440)
NOTIFICATION_ACCESS = "adb -s <tv>:5555 shell cmd notification allow_listener com.gathin.tvhome/com.gathin.tvhome.MediaWatcher"


def _load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def presets():
    """Folders in themes/ that hold a config.json: name -> path."""
    return {p.parent.name: p for p in sorted((REPO / "themes").glob("*/config.json"))}


def bundled():
    """Themes built into the launcher APK: id -> (label, config)."""
    found = {}
    for path in sorted(BUNDLED.glob("*/config.json")):
        config = _load(path)
        found[path.parent.name] = (config.get("theme_label", path.parent.name), config)
    return found


def list_themes():
    lines = ["Presets (colour and layout, pushed with scripts/apply-theme.sh):"]
    lines += [f"  {name}" for name in presets()]
    lines.append("Bundled in the launcher (look carries over; the wallpaper is picked on the TV, Settings gear > Theme):")
    for tid, (label, config) in bundled().items():
        lines.append(f"  {tid:<18} {label}  {'light' if config.get('light_mode') else 'dark'}  {config.get('widget_accent', '')}")
    return "\n".join(lines)


def build(args):
    """(config, notes) for these answers. Raises ValueError for an answer that cannot work."""
    config, notes = {}, []
    if args.theme:
        if args.theme in presets():
            config.update(_load(presets()[args.theme]))
        elif args.theme in bundled():
            label, theme = bundled()[args.theme]
            config.update({k: theme[k] for k in LOOK_KEYS if k in theme})
            notes.append(f"Theme {label}: after applying, choose it on the TV (Settings gear > Theme) to get its wallpaper.")
        else:
            raise ValueError(f"unknown theme {args.theme!r}; see --list-themes")
    if args.light:
        config["light_mode"] = True
    if args.weather:
        config["weather"] = True
        notes.append("Weather asks ipwho.is for an approximate location once a day and Open-Meteo for the forecast. "
                     "Confirm with the user before applying.")
    if args.year_widget:
        config["month_card"] = True   # one switch for the calendar and the days-left cards under the top icons
    if args.widget_row:
        config["widget_row"] = True
    if args.magnify:
        config["dock_magnify"] = True
    if args.glass_icons:
        config["glass_icons"] = True
    if args.original_icons:
        config["glass_icons"] = False
    if args.dock_apps:
        config["dock_apps"] = [p for p in args.dock_apps.split(",") if p]
    if args.hidden_apps:
        config["hidden_apps"] = [p for p in args.hidden_apps.split(",") if p]
    if args.console:
        config["input_tiles"] = [f"{name}|{name}" for name in args.console]
        if "dock_apps" in config:
            config["dock_apps"] += [f"input:{name}" for name in args.console]
        else:
            notes.append("No --dock-apps given, so the console tile sorts in with the other apps. Pass --dock-apps to put it in the dock.")
        notes.append("The console tile appears only while a matching HDMI device is connected and powered (HDMI-CEC).")
    if args.calendar_mode:
        notes.append(f"Calendar mode is chosen on the TV: press OK on the calendar, then Left for Weekly or Right for Monthly (you chose {args.calendar_mode.title()}).")
    if args.widget_size:
        notes.append(f"Widget size is chosen on the TV: Settings gear > Launcher settings > Widget size > {args.widget_size.title()}.")
    if args.rotation is not None:
        if args.rotation not in ROTATION:
            raise ValueError("--rotation must be one of 0 (off), 15, 60 or 1440 minutes")
        if args.rotation:
            notes.append(f"Theme rotation is chosen on the TV: Settings gear > Launcher settings > Theme rotation (every {args.rotation} minutes).")
    if args.continue_watching:
        notes.append("Continue-watching pictures need notification access for the launcher. It reads only what media apps "
                     "publish, and for a YouTube app it can fetch YouTube's public thumbnail and title for the video. "
                     f"Confirm with the user, then run: {NOTIFICATION_ACCESS}")
    return theme_merge.merge(config), notes


def parser():
    p = argparse.ArgumentParser(description="Write a launcher config.json overlay from the user's answers.")
    p.add_argument("--theme", help="a preset (themes/) or a bundled theme id; see --list-themes")
    p.add_argument("--light", action="store_true", help="light mode: dark text, pale glass")
    p.add_argument("--weather", action="store_true", help="weather line and tile (uses the internet)")
    p.add_argument("--year-widget", action="store_true", help="calendar and days-left cards under the top icons")
    p.add_argument("--widget-row", action="store_true", help="widgets and recent apps in the last app row")
    p.add_argument("--calendar-mode", choices=("weekly", "monthly"), help="set on the TV; prints the steps")
    p.add_argument("--widget-size", choices=("small", "medium", "large"), help="set on the TV; prints the steps")
    p.add_argument("--magnify", action="store_true", help="dock magnification and the glass dock")
    p.add_argument("--glass-icons", action="store_true", help="glass app tiles")
    p.add_argument("--original-icons", action="store_true", help="the apps' own banners")
    p.add_argument("--rotation", type=int, help="theme rotation in minutes: 0, 15, 60 or 1440; set on the TV")
    p.add_argument("--continue-watching", action="store_true", help="print the steps for the continue-watching picture")
    p.add_argument("--console", action="append", metavar="NAME", help="HDMI console tile, for example PlayStation")
    p.add_argument("--dock-apps", help="comma-separated package names, in dock order")
    p.add_argument("--hidden-apps", help="comma-separated package names to hide")
    p.add_argument("--out", metavar="DIR", help="write DIR/config.json")
    p.add_argument("--dry-run", action="store_true", help="print only; write nothing")
    p.add_argument("--list-themes", action="store_true")
    return p


def main(argv=None):
    args = parser().parse_args(argv)
    if args.list_themes:
        print(list_themes())
        return 0
    try:
        config, notes = build(args)
    except ValueError as err:
        print(f"error: {err}", file=sys.stderr)
        return 1
    text = json.dumps(config, indent=2, sort_keys=True) + "\n"
    for note in notes:
        print(f"note: {note}", file=sys.stderr)
    if args.out and not args.dry_run:
        out = pathlib.Path(args.out)
        out.mkdir(parents=True, exist_ok=True)
        (out / "config.json").write_text(text, encoding="utf-8")
        print(f"wrote {out / 'config.json'}; apply it with: scripts/apply-theme.sh <tv> {out}", file=sys.stderr)
    else:
        sys.stdout.write(text)
        if args.dry_run:
            print("dry run: nothing written", file=sys.stderr)
    return 0


if __name__ == "__main__":
    sys.exit(main())
