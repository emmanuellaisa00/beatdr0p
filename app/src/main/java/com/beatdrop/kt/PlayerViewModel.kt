package com.beatdrop.kt

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.exoplayer.ExoPlayer
import com.beatdrop.kt.data.*
import com.beatdrop.kt.lyrics.LrcParser
import com.beatdrop.kt.lyrics.LyricLine
import com.beatdrop.kt.playback.PlaybackService
import com.beatdrop.kt.youtube.OnlineResult
import com.beatdrop.kt.youtube.OnlineSearch
import com.beatdrop.kt.youtube.YoutubeExtractor
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Replaces the RN Zustand store + PlayerService. Holds all playback + library
 * UI state as StateFlows and drives a MediaController bound to PlaybackService.
 */
class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = MediaRepository(app)
    private val prefs = Prefs(app)

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    fun setQuery(q: String) { _query.value = q }

    private val _sort = MutableStateFlow(SortMode.TITLE_ASC)
    val sort: StateFlow<SortMode> = _sort.asStateFlow()
    fun setSort(s: SortMode) { _sort.value = s }

    private val _current = MutableStateFlow<Track?>(null)
    val current: StateFlow<Track?> = _current.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _position = MutableStateFlow(0L)
    val position: StateFlow<Long> = _position.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _lyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyrics: StateFlow<List<LyricLine>> = _lyrics.asStateFlow()
    private val _activeLyric = MutableStateFlow(-1)
    val activeLyric: StateFlow<Int> = _activeLyric.asStateFlow()

    private val _liked = MutableStateFlow<Set<String>>(emptySet())
    val liked: StateFlow<Set<String>> = _liked.asStateFlow()
    fun isLiked(id: String) = _liked.value.contains(id)
    fun toggleLike(id: String) {
        val next = _liked.value.toMutableSet().apply { if (!add(id)) remove(id) }
        _liked.value = next
        viewModelScope.launch { prefs.setLiked(next) }
    }

    // ── Playlists (name -> ordered track ids) ─────────────────────────────────
    private val _playlists = MutableStateFlow<Map<String, List<String>>>(emptyMap())
    val playlists: StateFlow<Map<String, List<String>>> = _playlists.asStateFlow()
    fun createPlaylist(name: String) {
        if (name.isBlank() || _playlists.value.containsKey(name)) return
        persistPlaylists(_playlists.value + (name.trim() to emptyList()))
    }
    fun deletePlaylist(name: String) { persistPlaylists(_playlists.value - name) }
    fun addToPlaylist(name: String, trackId: String) {
        val cur = _playlists.value[name] ?: return
        if (trackId in cur) return
        persistPlaylists(_playlists.value + (name to (cur + trackId)))
    }
    fun removeFromPlaylist(name: String, trackId: String) {
        val cur = _playlists.value[name] ?: return
        persistPlaylists(_playlists.value + (name to (cur - trackId)))
    }
    fun playlistTracks(name: String): List<Track> {
        val ids = _playlists.value[name] ?: return emptyList()
        val byId = _tracks.value.associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }
    private fun persistPlaylists(m: Map<String, List<String>>) {
        _playlists.value = m
        viewModelScope.launch { prefs.setPlaylists(m) }
    }

    // ── Play counts / stats ───────────────────────────────────────────────────
    private val _playCounts = MutableStateFlow<Map<String, Int>>(emptyMap())
    val playCounts: StateFlow<Map<String, Int>> = _playCounts.asStateFlow()

    // ── Settings ──────────────────────────────────────────────────────────────
    private val _haptics = MutableStateFlow(true)
    val haptics: StateFlow<Boolean> = _haptics.asStateFlow()
    fun setHaptics(v: Boolean) { _haptics.value = v; viewModelScope.launch { prefs.setHaptics(v) } }

    private val _theme = MutableStateFlow("system")
    val theme: StateFlow<String> = _theme.asStateFlow()
    fun setTheme(v: String) { _theme.value = v; viewModelScope.launch { prefs.setTheme(v) } }

    private val _defaultShuffle = MutableStateFlow(false)
    val defaultShuffle: StateFlow<Boolean> = _defaultShuffle.asStateFlow()
    fun setDefaultShuffle(v: Boolean) { _defaultShuffle.value = v; viewModelScope.launch { prefs.setDefaultShuffle(v) } }

    private fun observePrefs() {
        prefs.likedFlow.onEach { _liked.value = it }.launchIn(viewModelScope)
        prefs.playlistsFlow.onEach { _playlists.value = it }.launchIn(viewModelScope)
        prefs.playCountsFlow.onEach { _playCounts.value = it }.launchIn(viewModelScope)
        prefs.hapticsFlow.onEach { _haptics.value = it }.launchIn(viewModelScope)
        prefs.themeFlow.onEach { _theme.value = it }.launchIn(viewModelScope)
        prefs.defaultShuffleFlow.onEach { _defaultShuffle.value = it }.launchIn(viewModelScope)
    }

    // ── Queue / shuffle / repeat ──────────────────────────────────────────────
    private val _queue = MutableStateFlow<List<Track>>(emptyList())
    val queue: StateFlow<List<Track>> = _queue.asStateFlow()

    private val _shuffle = MutableStateFlow(false)
    val shuffle: StateFlow<Boolean> = _shuffle.asStateFlow()
    fun toggleShuffle() {
        _shuffle.value = !_shuffle.value
        controller?.shuffleModeEnabled = _shuffle.value
    }

    private val _repeat = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeat: StateFlow<Int> = _repeat.asStateFlow()
    fun cycleRepeat() {
        val next = when (_repeat.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeat.value = next
        controller?.repeatMode = next
    }

    private fun refreshQueueFromController() {
        val c = controller ?: return
        val out = ArrayList<Track>()
        for (i in 0 until c.mediaItemCount) {
            val id = c.getMediaItemAt(i).mediaId
            _tracks.value.firstOrNull { it.id == id }?.let { out.add(it) }
        }
        _queue.value = out
    }

    fun playQueueIndex(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) { c.seekTo(index, 0L); c.play() }
    }

    fun removeFromQueue(index: Int) {
        val c = controller ?: return
        if (index in 0 until c.mediaItemCount) { c.removeMediaItem(index); refreshQueueFromController() }
    }

    fun shuffleAll() {
        val all = _tracks.value
        if (all.isEmpty()) return
        val list = all.shuffled()
        playList(list, list.first().id)
        _shuffle.value = true
        controller?.shuffleModeEnabled = true
    }


    private var controller: MediaController? = null

    // Derived helpers ----------------------------------------------------------
    fun filteredSorted(): List<Track> {
        val q = _query.value.trim()
        val base = if (q.isBlank()) _tracks.value else _tracks.value.filter {
            it.title.contains(q, true) || it.artist.contains(q, true) || it.album.contains(q, true)
        }
        return repo.sort(base, _sort.value)
    }

    fun albums(): List<AlbumGroup> = repo.groupAlbums(_tracks.value)
    fun artists(): List<ArtistGroup> = repo.groupArtists(_tracks.value)

    // Lifecycle ----------------------------------------------------------------
    fun connect() {
        observePrefs()
        val token = SessionToken(getApplication(), ComponentName(getApplication(), PlaybackService::class.java))
        val future = MediaController.Builder(getApplication(), token).buildAsync()
        future.addListener({
            controller = future.get()
            attach()
            startTicker()
        }, ContextCompat.getMainExecutor(getApplication()))
    }

    private fun attach() {
        controller?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(p: Boolean) { _isPlaying.value = p }
            override fun onMediaItemTransition(item: MediaItem?, reason: Int) { syncCurrent() }
            override fun onTimelineChanged(t: Timeline, reason: Int) { refreshQueueFromController() }
            override fun onPlaybackStateChanged(state: Int) {
                _duration.value = controller?.duration?.coerceAtLeast(0L) ?: 0L
            }
        })
    }

    private fun syncCurrent() {
        val id = controller?.currentMediaItem?.mediaId ?: return
        val t = _tracks.value.firstOrNull { it.id == id } ?: return
        _current.value = t
        _duration.value = t.durationMs
        loadLyrics(t)
    }

    fun loadLibrary() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) { repo.loadTracks() }
            _tracks.value = list
            _loaded.value = true
        }
    }

    fun play(track: Track) {
        val c = controller ?: return
        val list = filteredSorted()
        val startIndex = list.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        c.setMediaItems(list.map { it.toMediaItem() }, startIndex, 0L)
        c.prepare(); c.play()
        _current.value = track
        _duration.value = track.durationMs
        loadLyrics(track)
        refreshQueueFromController()
        viewModelScope.launch { prefs.incrementPlayCount(track.id) }
    }

    fun playList(tracks: List<Track>, startId: String) {
        val c = controller ?: return
        val idx = tracks.indexOfFirst { it.id == startId }.coerceAtLeast(0)
        c.setMediaItems(tracks.map { it.toMediaItem() }, idx, 0L)
        c.prepare(); c.play()
        _current.value = tracks.getOrNull(idx)
        refreshQueueFromController()
    }

    fun togglePlay() { controller?.let { if (it.isPlaying) it.pause() else it.play() } }
    fun next() { controller?.seekToNextMediaItem() }
    fun prev() { controller?.seekToPreviousMediaItem() }
    fun seekTo(ms: Long) { controller?.seekTo(ms); _position.value = ms }

    private fun loadLyrics(track: Track) {
        viewModelScope.launch {
            val parsed = withContext(Dispatchers.IO) { LrcParser.findAndParse(track) }
            _lyrics.value = parsed
            _activeLyric.value = -1
        }
    }

    private fun startTicker() {
        viewModelScope.launch {
            while (true) {
                controller?.let {
                    _position.value = it.currentPosition.coerceAtLeast(0L)
                    if (it.duration > 0) _duration.value = it.duration
                    if (_lyrics.value.isNotEmpty())
                        _activeLyric.value = LrcParser.activeIndex(_lyrics.value, _position.value)
                }
                delay(300)
            }
        }
    }

    // ── DJ Mode: two independent decks with crossfade ─────────────────────────
    // Deck A = the main MediaController; Deck B = a private ExoPlayer.
    private var deckB: ExoPlayer? = null

    private val _deckATrack = MutableStateFlow<Track?>(null)
    val deckATrack: StateFlow<Track?> = _deckATrack.asStateFlow()
    private val _deckBTrack = MutableStateFlow<Track?>(null)
    val deckBTrack: StateFlow<Track?> = _deckBTrack.asStateFlow()

    private val _deckAPlaying = MutableStateFlow(false)
    val deckAPlaying: StateFlow<Boolean> = _deckAPlaying.asStateFlow()
    private val _deckBPlaying = MutableStateFlow(false)
    val deckBPlaying: StateFlow<Boolean> = _deckBPlaying.asStateFlow()

    /** Crossfader: 0f = full A, 1f = full B. */
    private val _crossfade = MutableStateFlow(0f)
    val crossfade: StateFlow<Float> = _crossfade.asStateFlow()

    private fun ensureDeckB(): ExoPlayer {
        val existing = deckB
        if (existing != null) return existing
        val p = ExoPlayer.Builder(getApplication()).build()
        p.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) { _deckBPlaying.value = playing }
        })
        deckB = p
        return p
    }

    fun loadDeckA(track: Track) {
        // Deck A reuses the main controller (so the system notification reflects it).
        play(track)
        _deckATrack.value = track
        _deckAPlaying.value = true
        applyCrossfade(_crossfade.value)
    }

    fun loadDeckB(track: Track) {
        val p = ensureDeckB()
        p.setMediaItem(MediaItem.Builder().setMediaId(track.id).setUri(track.uri).build())
        p.prepare()
        p.playWhenReady = true
        _deckBTrack.value = track
        applyCrossfade(_crossfade.value)
    }

    fun toggleDeckA() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
        _deckAPlaying.value = c.isPlaying
    }

    fun toggleDeckB() {
        val p = deckB ?: return
        p.playWhenReady = !p.isPlaying
    }

    fun setCrossfade(v: Float) {
        val x = v.coerceIn(0f, 1f)
        _crossfade.value = x
        applyCrossfade(x)
    }

    private fun applyCrossfade(x: Float) {
        // Equal-power-ish: simple linear is fine for a phone DJ toy.
        controller?.volume = (1f - x)
        deckB?.volume = x
    }

    // ── Online search (pluggable backend) ─────────────────────────────────────
    private val _onlineQuery = MutableStateFlow("")
    val onlineQuery: StateFlow<String> = _onlineQuery.asStateFlow()
    fun setOnlineQuery(q: String) { _onlineQuery.value = q }

    private val _onlineResults = MutableStateFlow<List<OnlineResult>>(emptyList())
    val onlineResults: StateFlow<List<OnlineResult>> = _onlineResults.asStateFlow()

    private val _onlineLoading = MutableStateFlow(false)
    val onlineLoading: StateFlow<Boolean> = _onlineLoading.asStateFlow()

    private val _onlineMessage = MutableStateFlow<String?>(null)
    val onlineMessage: StateFlow<String?> = _onlineMessage.asStateFlow()
    fun clearOnlineMessage() { _onlineMessage.value = null }

    val searchConfigured: Boolean get() = OnlineSearch.isConfigured
    val extractorConfigured: Boolean get() = YoutubeExtractor.isConfigured

    fun runOnlineSearch() {
        val q = _onlineQuery.value.trim()
        if (q.isBlank()) return
        if (!OnlineSearch.isConfigured) {
            _onlineMessage.value = "Search backend not configured. Set OnlineSearch.provider."
            return
        }
        _onlineLoading.value = true
        viewModelScope.launch {
            val res = runCatching { OnlineSearch.provider.search(q) }.getOrElse {
                _onlineMessage.value = "Search failed: ${it.message}"; emptyList()
            }
            _onlineResults.value = res
            _onlineLoading.value = false
        }
    }

    /** Resolve a result to a stream URL (via your extractor) and play it. */
    fun playOnline(result: OnlineResult) {
        viewModelScope.launch {
            val url = runCatching { YoutubeExtractor.extractStreamUrl(result.videoId) }.getOrNull()
            if (url == null) {
                _onlineMessage.value = "Playback backend not configured (YoutubeExtractor)."
                return@launch
            }
            val c = controller ?: return@launch
            c.setMediaItem(
                MediaItem.Builder().setMediaId("yt_${result.videoId}").setUri(Uri.parse(url))
                    .setMediaMetadata(
                        MediaMetadata.Builder().setTitle(result.title).setArtist(result.author).build()
                    ).build()
            )
            c.prepare(); c.play()
        }
    }

    /** Hook for downloading — resolves URL then hands off to your download code. */
    fun downloadOnline(result: OnlineResult, onUrl: (String) -> Unit) {
        viewModelScope.launch {
            val url = runCatching { YoutubeExtractor.extractStreamUrl(result.videoId) }.getOrNull()
            if (url == null) {
                _onlineMessage.value = "Download backend not configured (YoutubeExtractor)."
                return@launch
            }
            onUrl(url)
        }
    }

    override fun onCleared() { controller?.release(); deckB?.release(); super.onCleared() }
}

private fun Track.toMediaItem(): MediaItem =
    MediaItem.Builder()
        .setMediaId(id)
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title).setArtist(artist).setAlbumTitle(album)
                .setArtworkUri(artworkUri).build()
        )
        .build()
