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
import theme_merge as tm  # noqa: E402


class MergeTests(unittest.TestCase):
    def test_unknown_and_wrong_type_keys_are_dropped_with_warnings(self):
        err = io.StringIO()
        with contextlib.redirect_stderr(err):
            out = tm.merge({"columns": 5, "columns_typo": 3, "dock_title": "yes"})
        self.assertEqual(out, {"columns": 5})
        self.assertIn("unknown key dropped: columns_typo", err.getvalue())
        self.assertIn("wrong type dropped: dock_title", err.getvalue())

    def test_dock_argument_replaces_the_theme_dock(self):
        out = tm.merge({"dock_apps": ["a.b.c"]}, ["x.y.z", "p.q.r"])
        self.assertEqual(out["dock_apps"], ["x.y.z", "p.q.r"])

    def test_bad_package_name_in_dock_is_an_error(self):
        with self.assertRaises(ValueError):
            tm.merge({}, ["not a package"])

    def test_background_must_be_hex(self):
        with self.assertRaises(ValueError):
            tm.merge({"background_color": "black"})


class PresetTests(unittest.TestCase):
    def test_every_preset_is_valid_and_warning_free(self):
        presets = sorted((ROOT / "themes").glob("*/config.json"))
        self.assertGreaterEqual(len(presets), 5)
        for path in presets:
            with self.subTest(theme=path.parent.name):
                config = json.loads(path.read_text(encoding="utf-8"))
                err = io.StringIO()
                with contextlib.redirect_stderr(err):
                    tm.merge(config)
                self.assertEqual(err.getvalue(), "")
                self.assertFalse(config.get("wallpaper"), "v1 presets ship without wallpapers")


class CliTests(unittest.TestCase):
    def test_non_object_config_fails_with_exit_1(self):
        with tempfile.TemporaryDirectory() as tmp:
            path = pathlib.Path(tmp) / "config.json"
            path.write_text("[1, 2]", encoding="utf-8")
            r = subprocess.run([sys.executable, str(ROOT / "scripts" / "theme_merge.py"), str(path)],
                               capture_output=True, text=True)
        self.assertEqual(r.returncode, 1)
        self.assertIn("JSON object", r.stderr)


if __name__ == "__main__":
    unittest.main()
