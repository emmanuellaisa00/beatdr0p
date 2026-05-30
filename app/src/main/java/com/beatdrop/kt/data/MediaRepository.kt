package com.beatdrop.kt.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

/**
 * Port of RN MediaStoreModule + MediaLibraryService — reads the on-device
 * audio library via MediaStore (the user's own files, no network).
 */
class MediaRepository(private val context: Context) {

    fun loadTracks(): List<Track> {
        val out = ArrayList<Track>()
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
        )
        // Mirror RN filter: real music, >= 20s to skip notification blips.
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND " +
            "${MediaStore.Audio.Media.DURATION} >= 20000"
        val sort = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        context.contentResolver.query(collection, projection, selection, null, sort)?.use { c ->
            val idC = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val tC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val arC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val alC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val alIdC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val dC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val daC = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            while (c.moveToNext()) {
                val id = c.getLong(idC)
                out.add(
                    Track(
                        id = id.toString(),
                        uri = ContentUris.withAppendedId(collection, id),
                        title = c.getString(tC) ?: "Unknown",
                        artist = c.getString(arC) ?: "Unknown artist",
                        album = c.getString(alC) ?: "",
                        albumId = c.getLong(alIdC),
                        durationMs = c.getLong(dC),
                        data = c.getString(dataC),
                        dateAdded = c.getLong(daC),
                    )
                )
            }
        }
        return out
    }

    fun groupAlbums(tracks: List<Track>): List<AlbumGroup> =
        tracks.groupBy { it.album to it.artist }
            .map { (k, v) -> AlbumGroup(k.first, k.second, v.first().artworkUri, v) }
            .sortedBy { it.album.lowercase() }

    fun groupArtists(tracks: List<Track>): List<ArtistGroup> =
        tracks.groupBy { it.artist }
            .map { (artist, v) -> ArtistGroup(artist, v.size, v) }
            .sortedBy { it.artist.lowercase() }

    fun sort(tracks: List<Track>, mode: SortMode): List<Track> = when (mode) {
        SortMode.TITLE_ASC -> tracks.sortedBy { it.title.lowercase() }
        SortMode.TITLE_DESC -> tracks.sortedByDescending { it.title.lowercase() }
        SortMode.ARTIST -> tracks.sortedBy { it.artist.lowercase() }
        SortMode.RECENT -> tracks.sortedByDescending { it.dateAdded }
    }
}
