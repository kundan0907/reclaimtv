#!/usr/bin/env python3
"""Check a theme's config.json and print the overlay the launcher gets.

Usage: theme_merge.py <theme/config.json> [--dock pkg1,pkg2]

Keys the launcher does not know, and values of the wrong type, are dropped with a warning; the launcher ignores
them too, so the rest of the theme still applies. A file that is not a JSON object is an error.
"""
import argparse
import json
import re
import sys

BOOL_KEYS = {
    "fill_dock", "dock_title", "dock_row", "reorder_button", "show_network_pill", "icon_only_buttons",
    "library_button", "always_show_names", "wallpaper_true_color", "app_library", "shamsi_date",
    "module_cards", "reduced_motion", "animations", "advanced_effects", "show_home_settings_shortcut",
    "show_stats", "wallpaper", "settings_key_panel", "cinematic_home", "ambient_glow",
    "month_card", "weather", "light_mode", "widget_row", "dock_magnify", "glass_icons",
}
INT_KEYS = {
    "columns", "rows_below_dock", "wifi_label_width", "sky_intensity", "wallpaper_rotation_minutes",
    "wallpaper_dim", "home_idle_clock_minutes",
}
STR_KEYS = {
    "live_tv_package", "live_tv_action", "source_panel_service", "source_panel_action", "input_action",
    "vpn_app", "festival", "festival_until", "greeting", "greeting_name", "accent_color",
    "background_color", "wallpaper_order", "widget_accent",
}
LIST_KEYS = {
    "dock_apps", "hidden_apps", "launch_actions", "app_labels", "app_banners", "settings_key_keycodes",
    "input_key_keycodes", "app_keys", "race_calendar", "input_tiles",
}
HEX = re.compile(r"^#[0-9A-Fa-f]{6}$")
PACKAGE = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*(\.[A-Za-z0-9_]+)+$")
# A console tile in the dock: "input:" and the tile's label (see input_tiles).
INPUT_TILE = re.compile(r"^input:[^|:\n]{1,32}$")


def _type_ok(key, value):
    if key in BOOL_KEYS:
        return isinstance(value, bool)
    if key in INT_KEYS:
        return isinstance(value, int) and not isinstance(value, bool)
    if key in STR_KEYS:
        return isinstance(value, str)
    return isinstance(value, list)


def merge(config, dock=None):
    """The overlay for the launcher: known keys of the right type, with the dock replaced when one is given."""
    overlay = {}
    for key, value in config.items():
        if key not in BOOL_KEYS | INT_KEYS | STR_KEYS | LIST_KEYS:
            print(f"warning: unknown key dropped: {key}", file=sys.stderr)
        elif not _type_ok(key, value):
            print(f"warning: wrong type dropped: {key}", file=sys.stderr)
        else:
            overlay[key] = value
    if dock is not None:
        overlay["dock_apps"] = dock
    for key in ("dock_apps", "hidden_apps"):
        ok = lambda p: isinstance(p, str) and (PACKAGE.match(p) or (key == "dock_apps" and INPUT_TILE.match(p)))
        if key in overlay and not all(ok(p) for p in overlay[key]):
            raise ValueError(f"{key} must hold package names")
    for key in ("background_color", "widget_accent"):
        if key in overlay and not HEX.match(overlay[key]):
            raise ValueError(f"{key} must be #RRGGBB")
    if "input_tiles" in overlay and not all(isinstance(t, str) and t.strip() for t in overlay["input_tiles"]):
        raise ValueError("input_tiles must hold text such as PlayStation|PlayStation")
    return overlay


def main(argv=None):
    parser = argparse.ArgumentParser(description="Check a theme and print its launcher overlay as JSON.")
    parser.add_argument("config")
    parser.add_argument("--dock", help="comma-separated package names, in dock order")
    args = parser.parse_args(argv)
    try:
        with open(args.config, encoding="utf-8") as f:
            config = json.load(f)
    except (OSError, json.JSONDecodeError) as err:
        print(f"error: {args.config}: {err}", file=sys.stderr)
        return 1
    if not isinstance(config, dict):
        print(f"error: {args.config} must hold a JSON object", file=sys.stderr)
        return 1
    dock = [p for p in args.dock.split(",") if p] if args.dock else None
    try:
        overlay = merge(config, dock)
    except ValueError as err:
        print(f"error: {args.config}: {err}", file=sys.stderr)
        return 1
    json.dump(overlay, sys.stdout, indent=2, sort_keys=True)
    sys.stdout.write("\n")
    return 0


if __name__ == "__main__":
    sys.exit(main())
