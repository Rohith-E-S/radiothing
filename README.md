# RadioThing

A lean, matte-black internet radio player for Android. Tune into 50,000+ live
stations from the [radio-browser](https://www.radio-browser.info/) directory,
watch the real FFT visualizer breathe, and leave the audio running while you
do other things.

[![Download](https://img.shields.io/github/v/release/Rohith-E-S/radiothing?include_prereleases&label=download&sort=semver)](https://github.com/Rohith-E-S/radiothing/releases/latest)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## Download

Grab the latest APK from the
[Releases page](https://github.com/Rohith-E-S/radiothing/releases) and install
it directly (Android 11+). Point
[Obtainium](https://github.com/ImranR98/Obtainium) at this repo for
auto-updates.

## Features

- **Browse** stations by country, tag and language, with search and
  pull-to-refresh
- **Real spectrum visualizer** driven by the player's own audio output —
  no microphone permission, ever
- **Favorites, playlists and recently played**, stored locally on-device
- **Sleep timer** with volume fade, playback queue, buffer-size control
- Foreground playback that survives Doze, with battery-efficient DSP audio
  offload when the device supports it

## Privacy

No account, no analytics, no ads, no tracking. Favorites, playlists, history
and settings never leave your device. Station data comes from the public
[radio-browser](https://www.radio-browser.info/) directory; audio streams come
directly from the stations themselves.

## Building

```bash
./gradlew :app:assembleRelease
```

Requires JDK 17+ (the Gradle daemon JVM is pinned via
`gradle/gradle-daemon-jvm.properties`). Release signing reads credentials from
a gitignored `keystore.properties` — see [docs/release-signing.md](docs/release-signing.md).

## License

[MIT](LICENSE)
