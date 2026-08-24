---
name: RadioThing
description: Matte black field instrument — minimal transistor radio rebuilt as a living signal lab.
colors:
  pure-black: "#050507"
  ink: "#09090B"
  panel: "#121214"
  panel-hover: "#17171A"
  grid-line: "#232326"
  hairline: "#2A2A2E"
  signal-red: "#FF3344"
  live-red: "#FF1A2D"
  signal-amber: "#FFA231"
  text-primary: "#FFFFFF"
  text-secondary: "#B3FFFFFF"
  text-tertiary: "#73FFFFFF"
  text-muted: "#59FFFFFF"
typography:
  display:
    fontFamily: "Fragment Mono, JetBrains Mono, monospace"
    fontWeight: 700
    fontSize: "48px"
    lineHeight: 1
    letterSpacing: "3px"
  headline:
    fontFamily: "Fragment Mono, JetBrains Mono, monospace"
    fontWeight: 700
    fontSize: "22px"
    lineHeight: 1.1
    letterSpacing: "2.5px"
  title:
    fontFamily: "Fragment Mono, JetBrains Mono, monospace"
    fontWeight: 600
    fontSize: "13px"
    lineHeight: 1.2
    letterSpacing: "0.4px"
  body:
    fontFamily: "Geist, Inter, system-ui, sans-serif"
    fontWeight: 400
    fontSize: "13px"
    lineHeight: 1.6
    letterSpacing: "0.2px"
  label:
    fontFamily: "Fragment Mono, JetBrains Mono, monospace"
    fontWeight: 700
    fontSize: "10px"
    lineHeight: 1
    letterSpacing: "1px"
rounded:
  sm: "6px"
  md: "12px"
  lg: "16px"
  xl: "18px"
  pill: "999px"
spacing:
  xs: "6px"
  sm: "10px"
  md: "16px"
  lg: "24px"
  xl: "32px"
components:
  button-primary:
    backgroundColor: "{colors.signal-red}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "10px 18px"
  button-ghost:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "10px 18px"
  station-card:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.lg}"
    padding: "14px"
  search-field:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.md}"
    padding: "14px 16px"
  chip-active:
    backgroundColor: "{colors.signal-red}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.sm}"
    padding: "8px 14px"
  chip-idle:
    backgroundColor: "{colors.panel}"
    textColor: "{colors.text-secondary}"
    rounded: "{rounded.sm}"
    padding: "8px 14px"
  bottom-nav:
    backgroundColor: "{colors.ink}"
    textColor: "{colors.text-primary}"
    rounded: "{rounded.xl}"
    padding: "6px"
---

# Design System: RadioThing

## Overview

**Creative North Star: "BLACK LAB — The Field Instrument"**

RadioThing is not a streaming catalog; it is a matte black field instrument left on at night. The world is Dieter Rams' minimal transistor crossed with an air-gapped signal lab: anodized enclosures, hairline chrome, amber seven-seg, and a single committed red that means *ON AIR* and nothing else. Every surface earns its air. Lists breathe with 10dp between enclosures and 14dp inside them; the type is set in mono caps with wide tracking so the radio reads like a bench instrument, not a feed. Color is restraint: black ground, panel elevations, and grid hairlines carry the structure, while Signal Red (#FF3344) owns the living signal — the pulsing dot, the active border, the primary action — across 30-40% of any playing surface. Amber (#FFA231) holds meter memory. This is the user-chosen direction (elevate brutalist, living signal, minimal transistor, seed db9cad78 candidate 7) and its four raises: art-forward 52dp artwork, module-snap grid, edge-light hairline discipline, and single-gesture deployment. The predictable opposite — a poster-wall catalog with terracotta serif — was kept out; one viewport after tuning, you remember the red pulse and the quiet.

**Key Characteristics:**
- Matte black lab material: Panel (#121214) on Pure Black (#050507) with 1dp Gridline (#232326) chrome
- Committed red discipline — red only means live/active, never decoration
- Mono as instrument voice (Fragment/JetBrains Mono), body in clean grotesk, 48dp touch everywhere
- Airy minimal transistor density — 10dp between cards, 52dp artwork, generous enclosure padding
- Living signal as signature — dot pulse, meter blocks, split-flap style numeric cadence

## Colors

Restrained strategy: neutrals carry the system; one saturated red carries the mechanism on 30-40% of active surfaces. Dark is scene-forced — night bench, OLED blacks, red preserves night vision.

### Primary
- **Signal Red** (#FF3344): The only saturated color. Live indicator, playing border, primary button, active nav, filled favorite. If it is not live/active, it is not red.
- **Live Red** (#FF1A2D): Peak variant for buffering/on-air emphasis and error states.

### Secondary
- **Signal Amber** (#FFA231): Seven-seg secondary — playlist accent, meter memory, tuning hint. Never competes with red for primary action.

### Neutral
- **Pure Black** (#050507): App ground, edge-to-edge system bars, scaffold background.
- **Ink** (#09090B): Navigation ground (bottom nav bar, mini-player dock) — one step above ground.
- **Panel** (#121214): Raised enclosures — station cards, search field, sheets, dialogs. The working surface.
- **Grid Line** (#232326): Hairline chrome — 1dp borders, dividers, inactive strokes.
- **Hairline** (#2A2A2E): Stronger border for focused/playing enclosures.
- **Text Primary** (#FFFFFF): Headings, station names, on-panel.
- **Text Secondary** (#B3FFFFFF / 70%): Metadata, subtitles, inactive labels — ≥4.5:1 on Panel.
- **Text Tertiary** (#73FFFFFF / 45%): Hints, captions, empty-state detail — used at ≥11sp.
- **Text Muted** (#59FFFFFF / 35%): Nav idle, disabled, divider labels.

### Named Rules
**The Red Means Live Rule.** Red appears only for live/playing/active/primary. Decorative red is banned; its rarity is the instrument's honesty.
**The Edge-Light Rule.** Color is confined to 1dp hairlines, dots, and active fills. Fields stay achromatic — Panel, Ink, Black — so the signal has ground.

## Typography

**Display Font:** Fragment Mono / JetBrains Mono, monospace (with system mono fallback)
**Body Font:** Geist / Inter, system grotesk
**Label/Mono Font:** Fragment Mono (unified mono for instrument voice)

**Character:** Mono is the instrument's voice — caps, wide tracking, tabular numerals for meters. Body is quiet grotesk for longer reading. No display serif; no hand-picked sizes per screen — every size maps to a Material role.

### Hierarchy
- **Display** (700, 48px, 1, 3px tracking): App wordmarks only; not used for station names to preserve air.
- **Headline** (700, 22px, 1.1, 2.5px tracking, uppercase): Screen titles — BROWSE, FAVORITES, LOG, PLAYLISTS, SETTINGS, NOW PLAYING. The bench label.
- **Title** (600, 13px, 1.2, 0.4px): Station names in lists, playlist names — bold, one line, ellipsis. The readable voice.
- **Body** (400, 13px, 1.6, 0.2px): Descriptions, helper text, dialogs; max 65–75ch.
- **Label** (700, 10px, 1, 1px tracking, uppercase): Metadata — K, CODEC, COUNTRY, • counts, button labels, captions. Always mono, always tracked.

### Named Rules
**The Caps For Instrument Rule.** All navigation, section headers, and metadata are uppercase mono with ≥0.8px tracking. Station names may be title case in data but render uppercase in list for bench consistency.
**The No Serif Rule.** No serif display face. The lab has no serifs; the mechanism is measurement, not editorial.

## Layout

Spatial model is a **single-column instrument bench** on phones — 16dp horizontal gutters, 10dp between enclosures, 14dp internal padding, 6–12dp between label rows. Content sits inside Panel enclosures that float on Pure Black with 1dp Gridline chrome; the bottom nav is an inset pill (20dp radius, 12dp outer padding, 6dp inner) docked to Ink, honoring window insets and IME. Browse, Favorites, Log, and Playlists share the same list grammar so the collection feels like one logbook.

Density is **minimal transistor** — airy, one primary action per viewport, big touch targets, generous separation above headings. Responsive: on expanded width (>600dp) the bottom nav becomes a rail/drawer per Material; window insets (status, nav, cutout, IME) are applied so nothing hides behind chrome. Motion and state are centralized; lists recycle composition.

## Elevation & Depth

No drop shadows. Depth is **tonal layering + hairline chrome** on OLED black. Panel (#121214) lifts from Pure Black (#050507) by tone; Ink (#09090B) lifts navigation one step above. Active elevation is a 1dp border shift to Hairline or Signal Red at 45% opacity, plus a tonal bump to PanelHover (#17171A). The system reads as mil-spec enclosures stacked on a bench, not cards casting shadows. This keeps red honest and performance quiet on low-end devices.

### Named Rules
**The Flat By Tone Rule.** Surfaces are flat. Elevation is tone + 1dp stroke. Shadows appear never; a red glow appears only on the central play control when playing (Spot 12dp, 35% red) as the instrument's single luminous exception.

## Shapes

Enclosures are **soft mil-spec** — not brutalist sharp, not playful pill. The bench's silhouette is consistent so any screen reads as RadioThing in one glance.

- **Enclosure (18dp):** Outer screen containers and sheets.
- **Card (16dp):** Station cards, playlist items, settings cards, empty-state plate.
- **Card Inner (12dp):** Artwork thumbnails, icon wells, sub-plates.
- **Chip (6–10dp):** Tags, bitrate pills, filter chips — 6dp tags, 10dp filter chips.
- **Pill (999px):** Segmented meter blocks, dot indicators, nav pills (14dp internally).
- **Borders:** 1dp everywhere. Inactive = Gridline (#232326); focused/playing = BrightRed 45–55% or Hairline (#2A2A2E). No left-border accents >1px.

## Components

### Buttons
- **Shape:** 12dp radius, 1dp border.
- **Primary:** Signal Red fill, white mono bold label (11sp, 1px tracking), 10dp vertical / 18dp horizontal padding; hover/press darkens to Live Red, no scale.
- **Ghost / Secondary:** Panel fill, Gridline border, white label; active/selected swaps to red fill + red border.
- **Icon Button:** 48dp minimum, circular or 14dp rounded; playing state adds red tint or border. All handle TalkBack and keyboard focus with Ink focus ring.

### Chips
- **Style:** 10dp radius, 1dp border. Idle: Panel + Gridline + Text Secondary; Active: Signal Red + Signal Red + white. Filter chips snap to module (8dp) per Module-Snap discipline.
- **Tag Pills:** 5–6dp radius, 1C1C1F ground, Text Muted mono 9sp, no border — secondary metadata only.

### Cards / Containers — Station Row
- **Corner:** 16dp, Panel fill, 1dp Gridline (becomes 1dp Red 55% when playing).
- **Internal:** 14dp horizontal, 12dp vertical padding; 13dp gap between 52dp artwork and text; 48dp circular favorite well on trailing edge.
- **Artwork:** 52dp square, 12dp radius, 1dp Hairline, dot-matrix fallback “∿” when favicon missing; playing adds 7dp red dot with white ring at top-end.
- **States:** Idle / Playing (pulse dot + alpha animation + red border) / Pressed (PanelHover) / Favorited (red circular well, red heart). Art-forward discipline: favicon renders at full 52dp when available.

### Inputs / Fields — Search
- **Style:** Panel fill, 1dp Hairline border, 14dp radius, 14dp horizontal / 12dp vertical padding; mono 13sp white text, 35% hint (“SEARCH — NAME, COUNTRY, TAG…”).
- **Focus:** Indicator line shifts to Signal Red; cursor is Signal Red.
- **Trailing Action:** GO pill — 8dp radius, Signal Red fill, white bold mono 11sp, 48dp touch.
- **Error / Disabled:** Error text in Live Red (#FF1A2D) on 1A0A0A plate; disabled uses Text Muted + GridLine.

### Navigation — Bottom Bar
- **Style:** Ink ground with inset Panel pill (20dp radius, 1dp Gridline stroke, 6dp inner padding). Five destinations evenly spaced.
- **Item:** 56dp tall, 14dp radius, mono 9sp label (1px tracking) below 20dp DotMatrixIcon. Idle: Text Muted icon/label; Active: 14dp-wide 2dp red underline + Panel 8% red fill + 1dp red 45% stroke + white/bold label + red icon. 48dp minimum touch, 8dp between targets. Matches WhatConverts to phone; rail variant on expanded width.

### Mini Player
- **Style:** Ink dock + Panel enclosure (16dp radius, 1dp chrome — Gridline idle, Red 45% playing). 3dp vertical strip at leading edge (Gridline idle, pulsing red playing).
- **Behavior:** Slides vertically; swipe up expands to Now Playing. Central 42dp red play/pause circle (white icon). Marquee station name (mono bold 12sp caps), bitrate/codec/queue line (mono 10sp). Respects navigation bar inset.

### Now Playing — Instrument Bench
- **Hero Plate:** Station header as Panel enclosure (16dp, Gridline) with 58dp artwork, bitrate pill (red stroke), codec/country meta, sleep countdown. Living Signal is the 142dp DotMatrixVisualizer with meter blocks and MiniWave — the signature interaction.
- **Volume:** Panel plate with 12-segment block bar (red vs 1A1A1E), white mono percent, Material slider (red active, 222 inactive).
- **Transport:** Panel pill (20dp) with prev/play/next — central 78dp red circle with 12dp red glow when playing, side 54dp prev/next.
- **Utility Row:** Panel pill with four ActionChips (Fav/Share/Sleep/Queue) — 42dp circular wells, red when active.

## Do's and Don'ts

### Do:
- **Do** keep lists airy — 10dp between cards, 16dp gutters, 14dp inside. Air is the transistor's craft.
- **Do** use red only for live/playing/active/primary — its scarcity makes the instrument legible.
- **Do** give every touch target 48dp and 8dp separation — thumb-first bench use.
- **Do** render favicons at 52dp when available (art-forward discipline) — choosing a station is choosing a face.
- **Do** keep chrome to 1dp hairlines in Gridline/Hairline — the enclosure, not the border, carries the form.
- **Do** honor insets — edge-to-edge with status/nav/cutout/IME insets applied so nothing hides behind chrome.
- **Do** use mono caps with 0.8–1.5px tracking for all labels — the bench speaks one voice.

### Don't:
- **Don't** scatter red as decoration — no red cards, red streaks, or red shadows beyond the live signal.
- **Don't** use cards of icon+heading+text as page structure — the station row is the only card; sections are enclosures, not nested cards.
- **Don't** draw hard offset shadows (4px 4px 0) — this is not neobrutalist costume; depth is tone + hairline.
- **Don't** use gradient text or glass blur as decoration — the lab is matte, not gloss.
- **Don't** borrow serif display or terracotta-cream publisher palettes — the North Star is the field instrument, not the bookshelf.
- **Don't** shrink station artwork below 44dp — the art-forward raise is normative.
- **Don't** port iOS bottom-bar-only navigation to tablet without rail adaptation — honor Material size classes.
