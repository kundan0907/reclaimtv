import http.server
import pathlib
import sys
import threading
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "scripts"))
import iptv_m3u as m  # noqa: E402

MASTER_720 = ("#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1,RESOLUTION=1280x720\nhi.m3u8\n"
              "#EXT-X-STREAM-INF:BANDWIDTH=0,RESOLUTION=640x360\nlo.m3u8\n")
MASTER_240 = "#EXTM3U\n#EXT-X-STREAM-INF:BANDWIDTH=1,RESOLUTION=426x240\nlo.m3u8\n"
MEDIA = "#EXTM3U\n#EXTINF:10,\nseg.ts\n"


class ParseTests(unittest.TestCase):
    def test_parse_skips_vlc_options_and_orphan_urls(self):
        text = ('#EXTM3U\n#EXTINF:-1 group-title="News",One\n#EXTVLCOPT:http-user-agent=x\n'
                "http://a/1.m3u8\nhttp://orphan\n")
        self.assertEqual(m.parse(text), [('#EXTINF:-1 group-title="News",One', "http://a/1.m3u8")])

    def test_dedupe_keeps_the_first_url(self):
        self.assertEqual(m.dedupe([("i1", "u"), ("i2", "u"), ("i3", "v")]), [("i1", "u"), ("i3", "v")])

    def test_max_height_reads_the_tallest_variant(self):
        self.assertEqual(m.max_height(MASTER_720), 720)
        self.assertIsNone(m.max_height(MEDIA))

    def test_playlist_url_rejects_path_tricks(self):
        self.assertEqual(m.playlist_url("countries", "us"), "https://iptv-org.github.io/iptv/countries/us.m3u")
        with self.assertRaises(ValueError):
            m.playlist_url("countries", "../x")
        with self.assertRaises(ValueError):
            m.playlist_url("nope", "us")


class _Handler(http.server.BaseHTTPRequestHandler):
    ROUTES = {"/hd.m3u8": MASTER_720, "/sd.m3u8": MASTER_240, "/media.m3u8": MEDIA, "/plain.txt": "hello"}

    def do_GET(self):
        body = self.ROUTES.get(self.path)
        if body is None:
            self.send_error(404)
            return
        data = body.encode()
        self.send_response(200)
        self.send_header("Content-Length", str(len(data)))
        self.end_headers()
        self.wfile.write(data)

    def log_message(self, *args):
        pass


class VerifyTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.server = http.server.ThreadingHTTPServer(("127.0.0.1", 0), _Handler)
        cls.thread = threading.Thread(target=cls.server.serve_forever, daemon=True)
        cls.thread.start()
        cls.base = f"http://127.0.0.1:{cls.server.server_address[1]}"

    @classmethod
    def tearDownClass(cls):
        cls.server.shutdown()
        cls.server.server_close()

    def test_keeps_tall_and_unknown_height_and_drops_low_plain_and_missing(self):
        entries = [
            ("hd", f"{self.base}/hd.m3u8"),
            ("sd", f"{self.base}/sd.m3u8"),
            ("media", f"{self.base}/media.m3u8"),
            ("plain", f"{self.base}/plain.txt"),
            ("gone", f"{self.base}/404.m3u8"),
        ]
        kept = [info for info, _ in m.verify(entries, min_height=480, jobs=4, timeout=5)]
        self.assertEqual(kept, ["hd", "media"])


if __name__ == "__main__":
    unittest.main()
