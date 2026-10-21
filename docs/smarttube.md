# SmartTube: YouTube for Android TV

SmartTube is an open-source YouTube client for Android TV: https://github.com/yuliskov/SmartTube

This repository does not include the APK. Get it from the project's own releases page, then install it:

1. On the TV, install **Downloader** (`com.esaba.downloader`) from the Play Store, and open it.
2. Enter the release URL from the SmartTube releases page, download the APK, and install it.
3. Or, from the computer: `adb -s <tv>:5555 install <file>.apk`.

The package name is `org.smarttube.stable`. Check it with `pm list packages | grep smarttube`.

SmartTube is on the keep list, so the debloat scripts never disable it.
