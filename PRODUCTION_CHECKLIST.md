# Production readiness checklist

Status legend: ✅ done · 🔶 ready/needs your input · ❌ not done

## Build & signing
- ✅ Release `signingConfig` (env/`keystore.properties`-driven, safe fallback)
- ✅ R8 minify + resource shrinking enabled for release
- ✅ ProGuard rules for Media3 / Coil / Compose / DataStore / models
- 🔶 Provide a keystore + CI secrets to emit a signed release (see KEYSTORE_SETUP.md)
- ❌ **First successful compile** — must be confirmed in GitHub Actions

## Quality
- ✅ Unit tests for `LrcParser` (run in CI; gate the build)
- ❌ More tests (MediaRepository grouping/sort, ViewModel logic) — recommended
- ❌ Instrumented/UI tests
- ❌ Manual testing on real devices across Android 7–14 / multiple OEMs
- ❌ Crash reporting (Crashlytics / Sentry) not integrated
- ❌ Analytics (optional)

## Compliance / store
- 🔶 `MANAGE_EXTERNAL_STORAGE` — fine for **sideload**, blocks Play Store
      (switch to scoped media access if publishing to Play)
- ❌ Privacy policy (required by Play if published)
- ❌ Store listing assets (icon variants, screenshots, descriptions)
- ✅ Online (YouTube) feature left as a hook — implement legally (INTEGRATION.md)

## Versioning
- ✅ `versionCode` / `versionName` present (bump per release)

## Recommended path to "production for sideload"
1. Push → green **Actions** build (fix any compile errors). ← blocking
2. Add keystore secrets → get a signed release APK.
3. Install on real devices; verify playback, EQ, DJ crossfade, lyrics, downloads.
4. Add crash reporting.
5. Tag a release; the workflow uploads the signed APK.

> The app is a **feature-complete beta candidate**. The single hard gate before
> any production talk is a confirmed compile in CI.
