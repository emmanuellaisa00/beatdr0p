package com.beatdrop.kt.youtube

/**
 * Online search result (what the Search UI renders).
 * Maps closely to the RN YoutubeSearchResult shape so your backend can populate it.
 */
data class OnlineResult(
    val videoId: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String?,
    val durationText: String,
)

/**
 * Pluggable search backend. The UI talks ONLY to this interface, so wiring your
 * own implementation (YouTube Data API, an internal service, etc.) is a one-file
 * change — no UI edits required.
 *
 * NOTE: returning metadata for search is fine; actual stream extraction lives in
 * [YoutubeExtractor] and is left unimplemented by design (ToS/copyright).
 */
interface SearchProvider {
    suspend fun search(query: String): List<OnlineResult>
}

/**
 * Default provider — returns nothing and signals "not configured" so the UI can
 * show a clear hint instead of fake data. Swap this out via [OnlineSearch.provider].
 */
object NotConfiguredProvider : SearchProvider {
    override suspend fun search(query: String): List<OnlineResult> = emptyList()
}

/**
 * Single injection point. Replace [provider] with your own implementation:
 *
 *   OnlineSearch.provider = MyYoutubeDataApiProvider(apiKey = "…")
 */
object OnlineSearch {
    @Volatile
    var provider: SearchProvider = NotConfiguredProvider

    /** True while the default (unconfigured) provider is in place. */
    val isConfigured: Boolean get() = provider !== NotConfiguredProvider
}
