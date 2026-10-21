"""Checks that the AI entry points exist, carry the required steps, and leak no personal data."""
import pathlib
import re
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
COPY_TEXT = ("Set up my Android TV with https://github.com/kundan0907/tv-cleanup — "
             "start with START_HERE.md and ask before each change.")
PERSONAL = re.compile(r"moviebox|community\.mbox|\b(?!192\.0\.2\.)\d{1,3}(?:\.\d{1,3}){3}\b", re.I)


class StartHereTests(unittest.TestCase):
    def setUp(self):
        self.text = (ROOT / "START_HERE.md").read_text(encoding="utf-8")

    def test_asks_every_question_and_dry_runs_first(self):
        for needle in ("TV brand", "Wi-Fi or Ethernet", "Theme", "Dock apps", "IPTV", "Debloat level", "Animation speed"):
            self.assertIn(needle, self.text)
        self.assertIn("--dry-run", self.text)

    def test_states_the_cloud_limit_and_the_local_path(self):
        self.assertIn("cannot reach a TV on the home network", self.text)
        self.assertIn("Claude Code", self.text)
        self.assertIn("Codex CLI", self.text)

    def test_wording_of_the_rules_is_present(self):
        self.assertIn("Never uninstall", self.text)
        self.assertIn("lists/keep.txt", self.text)


class ReadmeTests(unittest.TestCase):
    def test_copy_text_is_exact_and_sits_above_the_feature_list(self):
        readme = (ROOT / "README.md").read_text(encoding="utf-8")
        self.assertIn(COPY_TEXT, readme)
        self.assertLess(readme.index("## Set up with an AI"), readme.index("## What you get"))


class PersonalDataTests(unittest.TestCase):
    def test_entry_points_have_no_personal_data(self):
        for name in ("START_HERE.md", "README.md", "SKILL.md"):
            with self.subTest(file=name):
                self.assertIsNone(PERSONAL.search((ROOT / name).read_text(encoding="utf-8")))


if __name__ == "__main__":
    unittest.main()
