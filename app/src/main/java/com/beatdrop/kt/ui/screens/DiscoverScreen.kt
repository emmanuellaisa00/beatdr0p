package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
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

/** Local-only Discover: recommendations derived from the on-device library. */
@Composable
fun DiscoverScreen(vm: PlayerViewModel, onOpenSearch: () -> Unit = {}) {
    val C = LocalAppColors.current
    val tracks by vm.tracks.collectAsState()
    val counts by vm.playCounts.collectAsState()

    val recent = remember(tracks) { tracks.sortedByDescending { it.dateAdded }.take(12) }
    val mostPlayed = remember(tracks, counts) {
        val byId = tracks.associateBy { it.id }
        counts.entries.sortedByDescending { it.value }.mapNotNull { byId[it.key] }.take(12)
    }
    val randomMix = remember(tracks) { tracks.shuffled().take(12) }

    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(bottom = 160.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(16.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Discover", color = C.text, fontWeight = FontWeight.Black, fontSize = 26.sp, modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenSearch) { Icon(Icons.Filled.Search, "Online search", tint = C.accent) }
            }
        }
        if (tracks.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text("Your library is empty.", color = C.textSecondary) } }
            return@LazyColumn
        }
        if (mostPlayed.isNotEmpty()) item { Section("Most Played", mostPlayed, vm) }
        item { Section("Recently Added", recent, vm) }
        item { Section("Random Mix", randomMix, vm) }
    }
}

@Composable
private fun Section(title: String, list: List<Track>, vm: PlayerViewModel) {
    val C = LocalAppColors.current
    val ctx = LocalContext.current
    Column(Modifier.padding(top = 18.dp)) {
        Text(title, color = C.text, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 16.dp, bottom = 12.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(list) { t ->
                Column(Modifier.width(140.dp).pressableScale(onClick = { vm.playList(list, t.id) })) {
                    Box(Modifier.size(140.dp).clip(RoundedCornerShape(Radius.md)).background(C.bg3)) {
                        AsyncImage(model = ImageRequest.Builder(ctx).data(t.artworkUri).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        Box(Modifier.align(Alignment.BottomEnd).padding(8.dp).size(34.dp)
                            .clip(RoundedCornerShape(17.dp)).background(C.accent), Alignment.Center) {
                            Icon(Icons.Filled.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(t.title, color = C.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    Text(t.artist, color = C.textSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
                }
            }
        }
    }
}
