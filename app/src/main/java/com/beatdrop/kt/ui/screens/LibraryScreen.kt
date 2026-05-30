package com.beatdrop.kt.ui.screens

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.beatdrop.kt.PlayerViewModel
import com.beatdrop.kt.data.Track
import com.beatdrop.kt.ui.components.pressableScale
import com.beatdrop.kt.ui.theme.LocalAppColors
import com.beatdrop.kt.ui.theme.Radius
import com.beatdrop.kt.ui.theme.Spacing

private enum class LibTab(val label: String) { SONGS("Songs"), ALBUMS("Albums"), ARTISTS("Artists") }

@Composable
fun LibraryScreen(
    vm: PlayerViewModel,
    onOpenAlbum: (String, String) -> Unit = { _, _ -> },
    onOpenArtist: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onOpenPlaylists: () -> Unit = {},
    onOpenStats: () -> Unit = {},
) {
    val C = LocalAppColors.current
    val query by vm.query.collectAsState()
    val loaded by vm.loaded.collectAsState()
    val tracks by vm.tracks.collectAsState()
    var tab by remember { mutableStateOf(LibTab.SONGS) }

    Column(Modifier.fillMaxSize()) {
        // Header
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Beat", color = C.accent, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("Drop", color = C.text, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenPlaylists) { Icon(Icons.Filled.QueueMusic, "Playlists", tint = C.textSecondary) }
            IconButton(onClick = onOpenStats) { Icon(Icons.Filled.BarChart, "Stats", tint = C.textSecondary) }
            IconButton(onClick = onOpenSettings) { Icon(Icons.Filled.Settings, "Settings", tint = C.textSecondary) }
        }
        OutlinedTextField(
            value = query, onValueChange = vm::setQuery,
            placeholder = { Text("Search your library") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true, shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        // Segmented tabs
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LibTab.values().forEach { t ->
                val active = t == tab
                Box(
                    Modifier.clip(RoundedCornerShape(20.dp))
                        .background(if (active) C.accent else C.bg3)
                        .pressableScale(onClick = { tab = t })
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(t.label, color = if (active) Color.White else C.textSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        when {
            !loaded -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = C.accent) }
            tracks.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No music found on this device.", color = C.textSecondary) }
            else -> when (tab) {
                LibTab.SONGS -> SongsList(vm)
                LibTab.ALBUMS -> AlbumsGrid(vm, onOpenAlbum)
                LibTab.ARTISTS -> ArtistsList(vm, onOpenArtist)
            }
        }
    }
}

@Composable
private fun SongsList(vm: PlayerViewModel) {
    val list = vm.filteredSorted()
    val current by vm.current.collectAsState()
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        itemsIndexed(list) { index, song ->
            AnimatedRow(index) { SongRow(song, current?.id == song.id) { vm.play(song) } }
        }
    }
}

@Composable
private fun AlbumsGrid(vm: PlayerViewModel, onOpen: (String, String) -> Unit) {
    val C = LocalAppColors.current
    val ctx = LocalContext.current
    val albums = remember(vm.tracks.value) { vm.albums() }
    LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 160.dp)) {
        items(albums) { a ->
            Column(Modifier.padding(6.dp).pressableScale(onClick = { onOpen(a.album, a.artist) })) {
                Box(Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(Radius.md)).background(C.bg3)) {
                    AsyncImage(model = ImageRequest.Builder(ctx).data(a.artworkUri).crossfade(true).build(),
                        contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
                Text(a.album, color = C.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                Text(a.artist, color = C.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun ArtistsList(vm: PlayerViewModel, onOpen: (String) -> Unit) {
    val C = LocalAppColors.current
    val artists = remember(vm.tracks.value) { vm.artists() }
    LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
        itemsIndexed(artists) { index, ar ->
            AnimatedRow(index) {
                Row(Modifier.fillMaxWidth().pressableScale(onClick = { onOpen(ar.artist) }).padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(24.dp)).background(C.accentSoft), Alignment.Center) {
                        Text(ar.artist.take(1).uppercase(), color = C.accent, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(ar.artist, color = C.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${ar.trackCount} songs", color = C.textSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: Track, isCurrent: Boolean, onClick: () -> Unit) {
    val C = LocalAppColors.current
    val ctx = LocalContext.current
    Row(Modifier.fillMaxWidth().pressableScale(onClick = onClick).padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(52.dp).clip(RoundedCornerShape(Radius.sm)).background(C.bg3), Alignment.Center) {
            AsyncImage(model = ImageRequest.Builder(ctx).data(song.artworkUri).crossfade(true).build(),
                contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Icon(Icons.Filled.MusicNote, null, tint = C.textTertiary, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = if (isCurrent) C.accent else C.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = C.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(fmt(song.durationMs), color = C.textTertiary, fontSize = 12.sp)
    }
}

/** Staggered entrance (port of AnimatedListItem). */
@Composable
private fun AnimatedRow(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    androidx.compose.animation.AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, delayMillis = (index.coerceAtMost(12)) * 35)) +
            slideInVertically(tween(280, delayMillis = (index.coerceAtMost(12)) * 35)) { it / 4 },
    ) { content() }
}

fun fmt(ms: Long): String { val s = (ms / 1000).toInt(); return "%d:%02d".format(s / 60, s % 60) }
