package com.beatdrop.kt.youtube

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Hidden WebView host — the Kotlin counterpart of RN's YoutubeStreamExtractor.
 *
 * ⚠️ INTENTIONALLY A SHELL. It provides the WebView + a JS-injection hook only.
 * The stream-extraction logic itself is NOT implemented here (YouTube ToS /
 * copyright). Everything else in the app is wired so that the instant you supply
 * [YoutubeExtractor.extractStreamUrl] (and optionally a [SearchProvider]), the
 * full search → play → download flow works.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YoutubeWebViewHost(
    modifier: Modifier = Modifier,
    onReady: (WebView) -> Unit = {},
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                webViewClient = WebViewClient()
                onReady(this)
            }
        },
    )
}

/**
 * Single hook for turning a videoId into a playable/downloadable stream URL.
 *
 * Left returning null by design. To make the app's online playback + downloads
 * work, replace the body of [extractStreamUrl] with your own implementation.
 *
 * For reference, the original React Native app obtained URLs by:
 *   1. Loading the YouTube embed page in a hidden WebView (so YouTube's own JS,
 *      BotGuard, PO-token and cookies all run in Chromium).
 *   2. Reading `ytInitialPlayerResponse.streamingData` out of the page context
 *      via injected JS.
 *   3. Returning the chosen audio stream URL to the player / download manager.
 *
 * That approach is omitted here intentionally. If you implement it, you are
 * responsible for complying with YouTube's Terms of Service and copyright law.
 */
object YoutubeExtractor {
    /** @return a direct stream URL, or null if extraction is not configured. */
    suspend fun extractStreamUrl(@Suppress("UNUSED_PARAMETER") videoId: String): String? {
        // TODO(you): implement (left unimplemented by design).
        return null
    }

    /** True once you've wired real extraction (override this when you do). */
    val isConfigured: Boolean get() = false
}
