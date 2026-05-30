package com.beatdrop.kt.data

import android.net.Uri

/** Port of the RN `Track` type. */
data class Track(
    val id: String,
    val uri: Uri,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val data: String?,          // file path — used for sibling .lrc lookup
    val dateAdded: Long,
) {
    val artworkUri: Uri
        get() = Uri.parse("content://media/external/audio/albumart/$albumId")
}

data class AlbumGroup(val album: String, val artist: String, val artworkUri: Uri, val tracks: List<Track>)
data class ArtistGroup(val artist: String, val trackCount: Int, val tracks: List<Track>)

enum class SortMode(val label: String) {
    TITLE_ASC("Title A–Z"),
    TITLE_DESC("Title Z–A"),
    ARTIST("Artist"),
    RECENT("Recently added"),
}
