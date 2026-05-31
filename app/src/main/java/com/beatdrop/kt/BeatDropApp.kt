package com.beatdrop.kt

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Custom Coil image loader — a real perceived-speed win. A generous in-memory
 * cache means album art reappears instantly while scrolling, and a disk cache
 * survives app restarts so artwork never re-decodes from MediaStore twice.
 */
class BeatDropApp : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(true)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // up to 25% of app memory for art
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(80L * 1024 * 1024) // 80 MB
                    .build()
            }
            .respectCacheHeaders(false) // local art has none; always cache
            .build()
}
