"""Runs debloat.sh, revert.sh and speed.sh against a fake adb that logs every call and answers reads from fixtures.

No TV is needed: the tests check the guards, the dry-run promise and the undo record.
"""
import os
import pathlib
import subprocess
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]

FAKE_ADB = """#!/usr/bin/env bash
echo "$*" >> "$FAKE_LOG"
if [ "$1" = connect ]; then echo "connected to $2"; exit 0; fi
shift 2
[ "$1" = shell ] || exit 0
shift
case "$*" in
  "pm list packages") cat "$FIXTURES/installed.txt" ;;
  "pm list packages -d --user 0") cat "$FIXTURES/disabled.txt" ;;
  cmd*) echo "${FAKE_HOME:-com.gathin.tvhome}/.MainActivity" ;;
  "settings get global "*) echo 1.0 ;;
esac
"""

INSTALLED = [
    "com.google.android.youtube.tvmusic",
    "com.google.android.play.games",
    "com.google.android.tungsten.setupwraith",
    "com.android.vending",
    "com.coocaa.demonow.lightining",
    "com.example.homeapp",
    "org.smarttube.stable",
]


class ScriptTests(unittest.TestCase):
    def setUp(self):
        self.tmp = tempfile.TemporaryDirectory()
        d = pathlib.Path(self.tmp.name)
        self.fixtures = d / "fixtures"
        self.fixtures.mkdir()
        (self.fixtures / "installed.txt").write_text("\n".join(INSTALLED) + "\n", encoding="utf-8")
        (self.fixtures / "disabled.txt").write_text("com.google.android.play.games\n", encoding="utf-8")
        self.lists = d / "lists"
        self.lists.mkdir()
        (self.lists / "debloat-google.txt").write_text(
            "﻿# test list\r\ncom.google.android.youtube.tvmusic  # music\r\n"
            "com.google.android.play.games\ncom.google.android.tungsten.setupwraith\n"
            "com.android.vending\ncom.nothere.app\n", encoding="utf-8")
        (self.lists / "debloat-firmware.txt").write_text(
            "com.coocaa.demonow.lightining\ncom.example.homeapp\n", encoding="utf-8")
        self.log = d / "adb.log"
        self.log.write_text("", encoding="utf-8")
        self.adb = d / "adb"
        self.adb.write_text(FAKE_ADB, encoding="utf-8")
        self.adb.chmod(0o755)
        self.state = d / "state"
        self.env = {
            **os.environ,
            "ADB": str(self.adb),
            "FAKE_LOG": str(self.log),
            "FIXTURES": str(self.fixtures),
            "TV_CLEANUP_STATE": str(self.state),
            "TV_CLEANUP_LISTS": str(self.lists),
            "FAKE_HOME": "com.example.homeapp",
        }

    def tearDown(self):
        self.tmp.cleanup()

    def run_script(self, name, *args):
        return subprocess.run(["bash", str(ROOT / "scripts" / name), *args],
                              env=self.env, capture_output=True, text=True)

    def calls(self):
        return self.log.read_text(encoding="utf-8").splitlines()

    def test_dry_run_plans_guards_and_writes_nothing(self):
        r = self.run_script("debloat.sh", "192.0.2.10", "full", "--dry-run")
        self.assertEqual(r.returncode, 0, r.stderr)
        out = r.stdout
        self.assertIn("[dry-run] adb shell pm disable-user --user 0 com.google.android.youtube.tvmusic", out)
        self.assertIn("[dry-run] adb shell pm disable-user --user 0 com.coocaa.demonow.lightining", out)
        self.assertIn("off      com.google.android.play.games", out)
        self.assertIn("keep     com.android.vending", out)
        self.assertIn("keep     com.google.android.tungsten.setupwraith", out)
        self.assertIn("skip     com.example.homeapp", out)
        self.assertIn("absent   com.nothere.app", out)
        self.assertFalse(any("disable-user" in c for c in self.calls()))
        self.assertFalse(self.state.exists())

    def test_real_run_records_and_revert_undoes(self):
        r = self.run_script("debloat.sh", "192.0.2.10", "full")
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertIn("-s 192.0.2.10:5555 shell pm disable-user --user 0 com.google.android.youtube.tvmusic", self.calls())
        record = (self.state / "192.0.2.10_5555" / "disabled.txt").read_text(encoding="utf-8").split()
        self.assertEqual(sorted(record), ["com.coocaa.demonow.lightining", "com.google.android.youtube.tvmusic"])

        r = self.run_script("speed.sh", "192.0.2.10", "--scale", "0.5")
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertIn("-s 192.0.2.10:5555 shell settings put global window_animation_scale 0.5", self.calls())

        r = self.run_script("revert.sh", "192.0.2.10")
        self.assertEqual(r.returncode, 0, r.stderr)
        self.assertIn("-s 192.0.2.10:5555 shell pm enable --user 0 com.google.android.youtube.tvmusic", self.calls())
        self.assertIn("-s 192.0.2.10:5555 shell settings put global window_animation_scale 1.0", self.calls())
        self.assertFalse((self.state / "192.0.2.10_5555" / "disabled.txt").exists())

    def test_bad_tv_address_is_refused_before_adb_runs(self):
        r = self.run_script("connect.sh", "192.0.2.10;rm")
        self.assertNotEqual(r.returncode, 0)
        self.assertIn("TV address expected", r.stderr)
        self.assertEqual(self.calls(), [])

    def test_bad_entry_in_a_list_stops_before_anything_runs(self):
        (self.lists / "debloat-firmware.txt").write_text("com.good.app\ncom.bad;rm -rf\n", encoding="utf-8")
        r = self.run_script("debloat.sh", "192.0.2.10", "full", "--dry-run")
        self.assertNotEqual(r.returncode, 0)
        self.assertIn("not a package name", r.stderr)
        self.assertNotIn("disable", r.stdout)

    def test_speed_rejects_out_of_range_scale(self):
        r = self.run_script("speed.sh", "192.0.2.10", "--scale", "5")
        self.assertNotEqual(r.returncode, 0)
        self.assertIn("between 0 and 1", r.stderr)


if __name__ == "__main__":
    unittest.main()
