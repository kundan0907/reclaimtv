# Setup: turn on ADB on the TV

The scripts talk to the TV over ADB on your Wi-Fi. Turn ADB on only while you clean up, and off afterwards.

## On the TV

1. Open **Settings → Device Preferences → About** (on some firmware: **Settings → System → About**).
2. Select **Build** (or "Android TV OS build") seven times until it says you are a developer.
3. Open **Developer options** and turn on **USB debugging** and **Network debugging** (on some firmware, **ADB debugging**).
4. Note the TV's IP address: **Settings → Network & Internet** (the network name), then its IP address.

The TV and your computer must be on the same network. Avoid public Wi-Fi.

## On your computer

- macOS: `brew install android-platform-tools`
- Windows or Linux: install Android platform-tools (`adb`) from Google.

Connect, then accept the prompt on the TV ("Allow USB debugging?", tick "Always allow"):

```bash
adb connect 192.0.2.10:5555
```

Replace the address with the TV's. Check it with `scripts/connect.sh 192.0.2.10`.

## When you are done

Turn off **Network debugging** (or **ADB debugging**) in Developer options. The scripts never need it on afterwards.
