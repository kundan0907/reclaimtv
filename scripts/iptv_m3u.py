#!/usr/bin/env python3
"""Fetch iptv-org playlists, keep the streams that answer, and write one clean M3U for OwnTV.

Usage:
  iptv_m3u.py fetch <countries|categories|languages> <name> <out.m3u>   e.g. countries us
  iptv_m3u.py verify <in.m3u> <out.m3u> [--min-height 480] [--jobs 8] [--timeout 8]

verify keeps a stream when its playlist answers 200 with #EXTM3U and, for a master playlist, its tallest
variant reaches --min-height. A media playlist with no RESOLUTION has unknown height and is kept.
Requests send a VLC user agent, because some hosts refuse other clients.
Only add channels you are allowed to watch: the iptv-org index lists public streams, not licensed ones.
"""
import argparse
import re
import sys
import urllib.request
from concurrent.futures import ThreadPoolExecutor

BASE = "https://iptv-org.github.io/iptv"
KINDS = ("countries", "categories", "languages")
VLC_UA = "VLC/3.0.20 LibVLC/3.0.20"
NAME = re.compile(r"^[a-z0-9-]+$")
RESOLUTION = re.compile(r"RESOLUTION=\d+x(\d+)")


def playlist_url(kind, name):
    if kind not in KINDS or not NAME.match(name):
        raise ValueError(f"bad playlist: {kind}/{name}")
    return f"{BASE}/{kind}/{name}.m3u"


def parse(text):
    """(#EXTINF line, URL) pairs. Other tags between them, such as #EXTVLCOPT, are skipped."""
    entries, info = [], None
    for raw in text.splitlines():
        line = raw.strip()
        if line.startswith("#EXTINF"):
            info = line
        elif line and not line.startswith("#") and info is not None:
            entries.append((info, line))
            info = None
    return entries


def dedupe(entries):
    seen, out = set(), []
    for info, url in entries:
        if url not in seen:
            seen.add(url)
            out.append((info, url))
    return out


def max_height(playlist):
    heights = [int(h) for h in RESOLUTION.findall(playlist)]
    return max(heights) if heights else None


def fetch_text(url, timeout, limit=None):
    req = urllib.request.Request(url, headers={"User-Agent": VLC_UA})
    with urllib.request.urlopen(req, timeout=timeout) as resp:
        if resp.status != 200:
            raise OSError(f"HTTP {resp.status} from {url}")
        return resp.read(limit).decode("utf-8", "replace")


def probe(url, timeout):
    """The playlist text when the stream answers with an HLS playlist, else None."""
    try:
        body = fetch_text(url, timeout, limit=65536)
    except (OSError, ValueError):  # urllib errors and timeouts are OSError subclasses
        return None
    return body if body.lstrip().startswith("#EXTM3U") else None


def verify(entries, min_height, jobs, timeout):
    def keep(entry):
        body = probe(entry[1], timeout)
        if body is None:
            return False
        height = max_height(body)
        return height is None or height >= min_height

    with ThreadPoolExecutor(max_workers=jobs) as pool:
        flags = list(pool.map(keep, entries))
    return [entry for entry, ok in zip(entries, flags) if ok]


def write(entries, path):
    with open(path, "w", encoding="utf-8") as f:
        f.write("#EXTM3U\n")
        for info, url in entries:
            f.write(f"{info}\n{url}\n")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="cmd", required=True)
    fetch = sub.add_parser("fetch")
    fetch.add_argument("kind", choices=KINDS)
    fetch.add_argument("name")
    fetch.add_argument("out")
    fetch.add_argument("--timeout", type=float, default=15)
    check = sub.add_parser("verify")
    check.add_argument("src")
    check.add_argument("out")
    check.add_argument("--min-height", type=int, default=480)
    check.add_argument("--jobs", type=int, default=8)
    check.add_argument("--timeout", type=float, default=8)
    args = parser.parse_args(argv)

    if args.cmd == "fetch":
        try:
            entries = dedupe(parse(fetch_text(playlist_url(args.kind, args.name), args.timeout)))
        except (OSError, ValueError) as err:
            print(f"error: {err}", file=sys.stderr)
            return 1
        write(entries, args.out)
        print(f"fetched {len(entries)} streams -> {args.out}")
        return 0

    with open(args.src, encoding="utf-8") as f:
        entries = dedupe(parse(f.read()))
    kept = verify(entries, args.min_height, args.jobs, args.timeout)
    write(kept, args.out)
    print(f"kept {len(kept)} of {len(entries)} streams -> {args.out}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
