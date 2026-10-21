import contextlib
import io
import json
import pathlib
import subprocess
import sys
import tempfile
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))
import launcher_options as lo  # noqa: E402
import theme_merge as tm  # noqa: E402


def build(*argv):
    with contextlib.redirect_stderr(io.StringIO()):
        return lo.build(lo.parser().parse_args(argv))


class BuildTests(unittest.TestCase):
    def test_flags_become_launcher_keys(self):
        config, _ = build("--light", "--weather", "--year-widget", "--widget-row", "--magnify", "--glass-icons")
        self.assertEqual(config, {"light_mode": True, "weather": True, "month_card": True, "widget_row": True,
                                  "dock_magnify": True, "glass_icons": True})

    def test_tv_side_choices_write_no_key_and_print_steps(self):
        config, notes = build("--calendar-mode", "monthly", "--widget-size", "large", "--rotation", "60",
                              "--continue-watching")
        self.assertEqual(config, {})
        text = " ".join(notes)
        for needle in ("Monthly", "Large", "every 60 minutes", "allow_listener"):
            self.assertIn(needle, text)

    def test_console_adds_a_tile_and_a_dock_slot(self):
        config, _ = build("--dock-apps", "a.b.c,d.e.f", "--console", "PlayStation")
        self.assertEqual(config["input_tiles"], ["PlayStation|PlayStation"])
        self.assertEqual(config["dock_apps"], ["a.b.c", "d.e.f", "input:PlayStation"])

    def test_no_console_means_no_tile(self):
        config, _ = build("--dock-apps", "a.b.c")
        self.assertNotIn("input_tiles", config)
        self.assertEqual(config["dock_apps"], ["a.b.c"])

    def test_bundled_theme_carries_its_look_only(self):
        config, notes = build("--theme", "daylight")
        self.assertTrue(config["light_mode"])
        self.assertRegex(config["widget_accent"], r"^#[0-9A-Fa-f]{6}$")
        self.assertNotIn("theme", config)
        self.assertIn("Settings gear > Theme", notes[0])

    def test_preset_is_the_base(self):
        config, _ = build("--theme", "dark", "--weather")
        self.assertTrue(config["weather"])
        self.assertIn("background_color", config)

    def test_bad_answers_are_errors(self):
        for argv in (("--theme", "nope"), ("--rotation", "7"), ("--dock-apps", "not a package")):
            with self.subTest(argv=argv), self.assertRaises(ValueError):
                build(*argv)


class CliTests(unittest.TestCase):
    def run_cli(self, *argv):
        return subprocess.run([sys.executable, str(ROOT / "scripts" / "launcher_options.py"), *argv],
                              capture_output=True, text=True)

    def test_dry_run_prints_valid_json_and_writes_nothing(self):
        with tempfile.TemporaryDirectory() as tmp:
            r = self.run_cli("--weather", "--out", tmp + "/x", "--dry-run")
            self.assertEqual(r.returncode, 0)
            self.assertEqual(json.loads(r.stdout), {"weather": True})
            self.assertFalse(pathlib.Path(tmp, "x").exists())

    def test_out_writes_a_config_the_merge_accepts(self):
        with tempfile.TemporaryDirectory() as tmp:
            r = self.run_cli("--magnify", "--glass-icons", "--out", tmp)
            self.assertEqual(r.returncode, 0)
            config = json.loads(pathlib.Path(tmp, "config.json").read_text(encoding="utf-8"))
            err = io.StringIO()
            with contextlib.redirect_stderr(err):
                self.assertEqual(tm.merge(config), config)
            self.assertEqual(err.getvalue(), "")

    def test_list_themes_names_presets_and_bundled(self):
        out = self.run_cli("--list-themes").stdout
        for needle in ("dark", "daylight", "northern", "violet"):
            self.assertIn(needle, out)
        self.assertNotIn("ironman ", out)   # the fan-art themes are not in this repo


class ThemeMergeKeysTests(unittest.TestCase):
    def test_new_keys_are_accepted_and_checked(self):
        err = io.StringIO()
        with contextlib.redirect_stderr(err):
            out = tm.merge({"widget_row": True, "widget_accent": "#112233", "input_tiles": ["PlayStation|PlayStation"],
                            "dock_apps": ["a.b.c", "input:PlayStation"]})
        self.assertEqual(err.getvalue(), "")
        self.assertEqual(len(out), 4)
        with self.assertRaises(ValueError):
            tm.merge({"widget_accent": "red"})
        with self.assertRaises(ValueError):
            tm.merge({"input_tiles": [""]})


if __name__ == "__main__":
    unittest.main()
