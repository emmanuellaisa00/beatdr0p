# 🥁 BeatDrop — Kotlin / Jetpack Compose

A **native Kotlin rewrite** of the React Native BeatDrop music player, built with
**Jetpack Compose + Media3 (ExoPlayer)**. Same iOS-26 liquid-glass design language,
same core music-player behaviour — reimplemented natively.

Push to GitHub → **GitHub Actions builds an installable APK**.

> **This is a faithful port-in-progress, not a mechanical conversion.** React
> Native → Kotlin has no automatic translation; every screen and subsystem is
> re-architected by hand. This repo contains a complete, buildable **foundation +
> core screens**; the remaining screens are stubbed and ported over subsequent passes.

---

## ✅ What's implemented

| Area | RN original | Kotlin replacement |
|------|-------------|--------------------|
| State store | Zustand | `PlayerViewModel` + Kotlin `StateFlow` |
| Navigation | @react-navigation | tab state + Navigation-Compose |
| Playback | react-native-track-player | **Media3 / ExoPlayer + MediaSessionService** |
| Library scan | MediaStore native module | `MediaRepository` (MediaStore) |
| Lyrics (.lrc) | LyricsEngine | `LrcParser` sidecar reader |
| Artwork | Image uri | Coil |
| Settings store | mmkv / async-storage | DataStore (wired in deps) |
| Theme/glass | theme.ts | `Theme.kt` (`AppColors`, same `#C77DFF` accent + glass tokens) |
| Glass tab bar | AppNavigator | `GlassTabBar` (floating pill + spring lozenge) |
| Mini-player | MiniPlayer.tsx | `MiniPlayer.kt` |
| Now Playing | NowPlayingScreen | `NowPlayingScreen.kt` (artwork breathing pulse + synced lyrics) |
| Motion | Reanimated | Compose animations (`pressableScale`, staggered list entrance) |

### Screens
- **Onboarding** — welcome + feature tour, then permission grant ✅
- **Library** — Songs / Albums / Artists, search, header menu (Playlists/Stats/Settings) ✅
- **Album / Artist pages** — header, play/shuffle, track lists ✅
- **Now Playing** — artwork pulse, seek, shuffle/repeat/queue, synced lyrics ✅
- **Queue / Up Next** — tap-to-jump, remove ✅
- **Discover** — local recommendations (Most Played / Recently Added / Random Mix) ✅
- **Radio** — 6 themed mixes built from your library ✅
- **DJ Mode** — dual decks + working crossfader (two players) ✅
- **Playlists** — create/delete, add/remove, Liked Songs, detail view ✅
- **Stats** — total plays, top artists, top songs ✅
- **Settings** — theme, haptics, default-shuffle, counts (DataStore) ✅
- **Equalizer** — REAL native DSP (android.media.audiofx): per-band sliders, presets, bass boost ✅ *(the RN app had no working DSP)*
- **Online Search** — fully wired: search box, results, tap-to-play, download button; backed by a pluggable `SearchProvider` + `YoutubeExtractor` hook (you supply those — see INTEGRATION.md) ✅/⚠️

## ⚠️ YouTube extraction (intentionally a shell)

`youtube/YoutubeWebView.kt` + `YoutubeExtractor.extractStreamUrl()` provide the
WebView host and a hook, but **the audio stream-extraction logic is not
implemented** (YouTube ToS / copyright). The Search UI is wired end-to-end; plug
your own extraction into `YoutubeExtractor` to populate results and play/download.

---

## 📦 Get the APK
1. Push to a GitHub repo.
2. **Actions → Build BeatDrop (Kotlin) APK** → wait for green check.
3. Open the run → **Artifacts** → download **`BeatDrop-kotlin-debug-apk`**.
4. Install on your phone; grant the music permission on launch.

```bash
cd BeatDropKt
git init && git add . && git commit -m "BeatDrop Kotlin port"
git branch -M main
git remote add origin https://github.com/<you>/beatdrop-kotlin.git
git push -u origin main
```

## 🛠 Build locally
```bash
./gradlew assembleDebug   # → app/build/outputs/apk/debug/app-debug.apk
```
Or open the folder in **Android Studio** and hit ▶ Run.

---

## ⚙️ Tech
| | |
|---|---|
| Language | Kotlin 1.9.24 |
| UI | Jetpack Compose (BOM 2024.06) + Material 3 |
| Playback | Media3 / ExoPlayer 1.3.1 + MediaSession |
| Nav | Navigation-Compose 2.7.7 |
| Images | Coil 2.6 |
| Min SDK | 24 · Target 34 |
| CI | GitHub Actions (JDK 17, Gradle 8.7) |

## 🔐 Permissions
Mirrors the RN app: `READ_MEDIA_AUDIO`/`READ_EXTERNAL_STORAGE`, media images,
`MANAGE_EXTERNAL_STORAGE` (kept — needed for sidecar `.lrc`, folder cover-art,
file deletion on sideloaded builds), foreground-service media playback, internet,
post-notifications.

---

## 🚧 Remaining (your call)
The online backend is **fully wired end-to-end** — only two clearly-marked hooks
are left unimplemented by design (ToS/copyright): `SearchProvider` (results) and
`YoutubeExtractor.extractStreamUrl` (stream URL). Implement them — or point them
at a licensed/royalty-free catalog — and Search/Play/Download work with no UI
changes. See **INTEGRATION.md**.
