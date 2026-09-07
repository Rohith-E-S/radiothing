# RadioThing

A dot-matrix internet radio player for Android, styled like an instrument panel from a black lab bench. Browse tens of thousands of stations from the [radio-browser.info](https://www.radio-browser.info) open directory, tune in, and watch the signal on a live CRT-style equalizer.

**Open • Free • No ads • No trackers**

## Features

- **Browse** — search and filter the radio-browser.info directory by country, genre/tag, language, bitrate, and codec; pull-to-refresh and endless pagination
- **Now Playing** — CRT-style dot equalizer driven by the stream's real FFT data, sleep timer, queue, volume
- **Favorites & History** — favorite stations with undo, automatic listening log
- **Playlists** — group stations into "trays", play a tray as a queue, send any station to a tray
- **Mini player** — swipe up to expand; prev/next when a queue is active
- **Nothing-Phone-style dot-matrix UI** — all glyphs drawn in-house or rendered with the OFL-licensed Doto typeface

## Install

[comment]: <> (TODO: add F-Droid badge when the package is accepted)

- **F-Droid:** coming soon
- **Google Play:** published as RadioThing
- Or build from source:

```bash
git clone https://github.com/Rohith-E-S/radiothing.git
cd radiothing
./gradlew :app:assembleDebug
```

Android 11 (API 30) or newer.

## Building

The project is a standard Gradle build with Kotlin DSL:

| Module    | Purpose                                          |
|-----------|--------------------------------------------------|
| `:app`    | Application entry point, DI wiring               |
| `:ui`     | Compose screens, components, theming             |
| `:domain` | Use cases and models                             |
| `:data`   | Repository implementations, Room, Retrofit       |
| `:player` | ExoPlayer/Media3 playback engine                 |

Release signing is optional for local builds — see [docs/release-signing.md](docs/release-signing.md). Without `keystore.properties` the release build is simply unsigned.

## License

```
Copyright (C) 2026 Rohith E S

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
```

See [LICENSE](LICENSE) for the full text.

## Third-party assets & libraries

- [Doto typeface](https://fonts.google.com/specimen/Doto) — © 2024 The Doto Project Authors, licensed under the [SIL Open Font License 1.1](licenses/Doto-OFL.txt)
- App code is licensed under GPL-3.0. Bundled libraries (Kotlin, Jetpack Compose, Media3, OkHttp, Retrofit, Room, Hilt, Coil, Gson, kotlinx.coroutines) remain under their own OSI-approved licenses.
- Station metadata is provided by the community-run [radio-browser.info](https://www.radio-browser.info) API.
