# Wiring the online (YouTube) backend

The app's online Search → Play → Download flow is **fully built and wired**. The
only unimplemented pieces are two clearly-marked hooks (left out by design for
ToS/copyright reasons). Implement them and everything lights up — **no UI edits
needed.**

> ⚠️ If you implement YouTube stream extraction/downloading, you are responsible
> for complying with YouTube's Terms of Service and applicable copyright law.
> A fully legal alternative is to point the provider at a licensed/royalty-free
> catalog (Jamendo, Free Music Archive, your own API).

## 1. Search results — `SearchProvider`

File: `app/src/main/java/com/beatdrop/kt/youtube/SearchProvider.kt`

Implement the interface and register it once (e.g. in `BeatDropApp.onCreate`):

```kotlin
class MyProvider : SearchProvider {
    override suspend fun search(query: String): List<OnlineResult> {
        // call your API, map to OnlineResult(videoId, title, author, thumbnailUrl, durationText)
        return ...
    }
}

// in BeatDropApp.onCreate():
OnlineSearch.provider = MyProvider()
```

That's all the Search screen needs to show real results.

## 2. Playback / download — `YoutubeExtractor`

File: `app/src/main/java/com/beatdrop/kt/youtube/YoutubeWebView.kt`

```kotlin
object YoutubeExtractor {
    suspend fun extractStreamUrl(videoId: String): String? {
        // return a direct, playable stream URL for videoId (or null)
        return ...
    }
    val isConfigured get() = true   // flip to true when implemented
}
```

- `PlayerViewModel.playOnline(result)` resolves the URL and plays it via Media3.
- `PlayerViewModel.downloadOnline(result) { url -> ... }` resolves the URL and
  hands it to your download code (DownloadManager, OkHttp, etc.).
- `YoutubeWebViewHost` is provided if you want the hidden-WebView approach the
  original used (load embed page → read `ytInitialPlayerResponse.streamingData`).

## What already works without you touching anything
- Search box, debounced submit, loading state, result list, thumbnails
- Tap-to-play and a per-row download button (call your hook)
- Graceful "not configured" messages + snackbar errors
- Mini-player / Now Playing / queue all handle the online MediaItem the same as
  local tracks
