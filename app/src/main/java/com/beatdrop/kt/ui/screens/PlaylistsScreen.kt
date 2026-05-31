package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.PlayerViewModel
import com.beatdrop.kt.ui.components.pressableScale
import com.beatdrop.kt.ui.theme.LocalAppColors
import com.beatdrop.kt.ui.theme.Radius

@Composable
fun PlaylistsScreen(vm: PlayerViewModel, onBack: () -> Unit = {}, onOpen: (String) -> Unit) {
    val C = LocalAppColors.current
    val playlists by vm.playlists.collectAsState()
    val liked by vm.liked.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp, 10.dp, 16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = C.text) }
            Text("Playlists", color = C.text, fontWeight = FontWeight.Black, fontSize = 26.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = { showCreate = true }) { Icon(Icons.Filled.Add, "New playlist", tint = C.accent) }
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 160.dp)) {
            item {
                // Built-in "Liked Songs"
                Row(
                    Modifier.fillMaxWidth().pressableScale(onClick = { onOpen(LIKED_NAME) }).padding(16.dp, 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(Radius.sm)).background(C.accentSoft), Alignment.Center) {
                        Text("♥", color = C.accent, fontSize = 22.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Liked Songs", color = C.text, fontWeight = FontWeight.SemiBold)
                        Text("${liked.size} songs", color = C.textSecondary, fontSize = 12.sp)
                    }
                }
            }
            items(playlists.keys.toList()) { name ->
                Row(
                    Modifier.fillMaxWidth().pressableScale(onClick = { onOpen(name) }).padding(16.dp, 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(Radius.sm)).background(C.bg3), Alignment.Center) {
                        Icon(Icons.Filled.QueueMusic, null, tint = C.textSecondary)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, color = C.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${playlists[name]?.size ?: 0} songs", color = C.textSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { vm.deletePlaylist(name) }) { Icon(Icons.Filled.Delete, "Delete", tint = C.textTertiary, modifier = Modifier.size(18.dp)) }
                }
            }
        }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            confirmButton = {
                TextButton(onClick = { vm.createPlaylist(newName); newName = ""; showCreate = false }) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Cancel") } },
            title = { Text("New playlist") },
            text = {
                OutlinedTextField(value = newName, onValueChange = { newName = it }, singleLine = true, placeholder = { Text("Name") })
            },
        )
    }
}

const val LIKED_NAME = "__liked__"

@Composable
fun PlaylistDetailScreen(vm: PlayerViewModel, name: String, onBack: () -> Unit) {
    val C = LocalAppColors.current
    val playlists by vm.playlists.collectAsState()
    val liked by vm.liked.collectAsState()
    val tracksAll by vm.tracks.collectAsState()
    val current by vm.current.collectAsState()
    var sheetTrack by remember { mutableStateOf<com.beatdrop.kt.data.Track?>(null) }

    val isLiked = name == LIKED_NAME
    val title = if (isLiked) "Liked Songs" else name
    val tracks = remember(name, playlists, liked, tracksAll) {
        if (isLiked) tracksAll.filter { liked.contains(it.id) } else vm.playlistTracks(name)
    }

  Box(Modifier.fillMaxSize()) {
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(bottom = 160.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = C.text) }
                Text(title, color = C.text, fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(Modifier.padding(16.dp, 4.dp, 16.dp, 12.dp)) {
                Button(onClick = { if (tracks.isNotEmpty()) vm.playList(tracks, tracks.first().id) }) {
                    Icon(Icons.Filled.PlayArrow, null); Spacer(Modifier.width(6.dp)); Text("Play")
                }
            }
        }
        if (tracks.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text("No songs yet.", color = C.textSecondary) } }
        }
        itemsIndexed(tracks, key = { _, t -> t.id }) { index, t ->
            Row(
                Modifier.fillMaxWidth().pressableScale(onClick = { vm.playList(tracks, t.id) }, onLongClick = { sheetTrack = t }).padding(16.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${index + 1}", color = C.textTertiary, modifier = Modifier.width(28.dp))
                Column(Modifier.weight(1f)) {
                    Text(t.title, color = if (current?.id == t.id) C.accent else C.text, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                    Text(t.artist, color = C.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (!isLiked) {
                    IconButton(onClick = { vm.removeFromPlaylist(name, t.id) }) {
                        Icon(Icons.Filled.Delete, "Remove", tint = C.textTertiary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
    sheetTrack?.let { tk ->
        com.beatdrop.kt.ui.components.TrackActionsSheet(vm, tk, onDismiss = { sheetTrack = null })
    }
  }
}
