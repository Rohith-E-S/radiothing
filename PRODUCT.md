# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

RadioThing serves **everyday internet radio listeners** across multiple jobs, not a single niche:

- **Explorers** — browsing global stations by country, language, genre/tag, falling into rabbit holes.
- **Curators** — saving favorites, building playlists, revisiting their personal library.
- **Lean-back listeners** — hitting play for work, focus, driving, or background and expecting reliable playback with minimal interaction.

The user confirmed "all of these" — the product must support discovery, collection, and passive listening equally. No single persona dominates. All usage is mobile, often while multitasking, with background playback and system notification controls.

Other audiences are not yet segmented; the product is intentionally broad.

## Product Purpose

RadioThing is a free, open, ad-free Android radio player that lets anyone discover and play world-wide internet radio without accounts, paywalls, or bloat.

It exists because existing radio apps are ad-heavy, closed, or over-designed. RadioThing provides a fast, honest way to tune in anywhere on earth.

Success means: user finds a station they like in <30 seconds, playback is rock-solid in background, and their curated favorites/playlists survive across sessions.

## Positioning

**Brutalist attitude over polished sameness.** Unlike TuneIn / Radio Garden, RadioThing does not compete on proprietary catalogs or glossy maps — it competes on design conviction and openness.

The meaningfully different mechanism:
- 100% powered by the open **Radio Browser** community API (no proprietary catalog).
- A distinctive **brutalist black/red visual world** — PureBlack backgrounds, BrightRed/LiveRed accents, monospace type, rectangle shapes, dot-matrix iconography — that is core to the brand, not decoration. The user named "Design / attitude" as the primary differentiator.

A competitor could copy the station data, but could not copy the world without becoming RadioThing.

## Operating Context

- **Native Android app** (minSdk 30, targetSdk 34, Compose + Material 3, edge-to-edge).
- **Multi-module Clean Architecture:** `:app` (Hilt DI, navigation), `:domain` (models/use-cases), `:data` (Retrofit + Room + DataStore, Radio Browser API, ServerResolver with fallback interceptor), `:player` (Media3 ExoPlayer + MediaSession), `:ui` (Compose screens, theme, components).
- **Key workflows:** Browse/search → filter (country/language/tag) → play; save to Favorites; add to Playlists; Recently Played/History; Now Playing (full-screen + MiniPlayer) with play/pause/next/previous/volume; Settings/History management.
- **Navigation:** BottomNavBar (Browse / Favorites / Playlists / History / Settings) + NowPlaying as expanded destination; MiniPlayer docks above bottom nav on all other screens.
- **Playback environment:** Foreground service + MediaSession + POST_NOTIFICATIONS permission (Android 13+), handles background, lock-screen, and interruption.
- **Data:** Live network via Radio Browser API; local persistence via Room (favorites, playlists, recently played) and DataStore (settings). No login, no sync, no backend spint.
- **Rituals & objects:** Stations already carry metadata — name, url/resolved url, homepage, favicon, tags, country, language, codec, bitrate, votes, click counts, last-check.

## Capabilities and Constraints

**Confirmed capabilities:**
- Search & browse stations; filter by country / language / tag.
- Playback via Media3 ExoPlayer with queue/next/previous.
- Favorites (persisted), Playlists with detail routes, Recently Played history.
- MiniPlayer + full NowPlaying, VolumeBar, FilterChips/Sheet, Skeleton loading, dot-matrix components.
- Notifications and background playback.

**Hard constraints (must preserve):**
- Free, no ads, open source — monetization is off-limits; do not add paywalls, ads, or tracking.
- Radio Browser is the source of truth; do not invent a proprietary station catalog or fake stations.
- No user accounts / auth — local-first.
- Android-native; do not pivot to web/PWA as primary platform without explicit decision.

**Terminology:** Station, Favorite, Playlist, History/Recently Played, Now Playing, Browse.

**Undecided (do not invent):** Monetization/donation model details, sync/cross-device strategy, offline recording, podcast integration.

## Brand Commitments

- **Name:** RadioThing — locked.
- **Voice:** Direct, utilitarian, slightly contrarian — the UI copy should match the brutalist aesthetic, not corporate polish.
- **Visual identity is binding:** PureBlack `#000000` / DarkGray `#1A1A1A` / BorderGray `#333333` + BrightRed `#FF2D2D` / LiveRed `#FF0000`; TextWhite 100/70/40; FontFamily.Monospace for titles/headlines (wide letter-spacing), sans fallback for body; RectangleShape cards, 2dp Chip radius; dot-matrix icons and ASCII art elements. This world was cited as the core differentiator — preserve and extend, do not soften.
- **Assets:** DotMatrixIcon, AsciiArtText in `ui/components`; RadioColors/RadioTypography/RadioShapes in `ui/theme`.

## Evidence on Hand

- Real runnable Android codebase at repo root — see `app/src/main/java/com/radiothing/app/MainActivity.kt:39`, `ui/theme/`, `player/` (Media3), `data/api/RadioBrowserApi.kt`.
- Real catalog via Radio Browser API DTOs (`data/api/dto/StationDto.kt`, etc.) — not mocked.
- No fabricated testimonials, case studies, press logos, or pricing — future work must not invent them.
- No public launch metrics to claim.

## Product Principles

1. **Open over owned** — Use and honor the community catalog; never wall it off.
2. **Attitude is a feature** — The brutalist world is not a skin; every new surface must commit to it.
3. **Play never breaks** — Background reliability, minimal taps to audio, and honest error states outrank novelty.
4. **Local-first, no baggage** — No accounts, no ads, no permissions beyond what playback requires.
5. **Fast to tune, easy to keep** — Discovery is immediate; curation (favorites/playlists/history) is durable and effortless.

## Accessibility & Inclusion

- Android-native accessibility expected: TalkBack labels, sufficient contrast (white on PureBlack, red accents used sparingly and never as sole signifier), touch targets ≥48dp, and notification controls usable without opening the app.
- No product-specific WCAG audit target has been set — treat as undecided; do not claim compliance until audited.
