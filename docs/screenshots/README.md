# Screenshots

`home-no-wallpaper.png` is the home screen on the author's TV: the dock, the top bar and no wallpaper. It shows the default layout the presets build on.

## Add your own

Take a screenshot from your TV with ADB on (see `docs/setup.md`):

```bash
adb -s 192.0.2.10:5555 exec-out screencap -p > docs/screenshots/home-dark.png
```

Before you commit one, check it. Remove it if it shows any of these:

- a Wi-Fi name, IP address or account name
- playlist names, last-watched channels or watch history
- a greeting that uses your name (`greeting_name` in a theme)
- an app you have not published a screenshot of (for example, a piracy streaming app)
