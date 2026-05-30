package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.PlayerViewModel
import com.beatdrop.kt.ui.components.pressableScale
import com.beatdrop.kt.ui.theme.LocalAppColors
import com.beatdrop.kt.ui.theme.Radius

private data class Station(val title: String, val subtitle: String, val c1: Color, val c2: Color, val pick: (List<com.beatdrop.kt.data.Track>) -> List<com.beatdrop.kt.data.Track>)

/** Radio = themed mixes built from the local library (no network). */
@Composable
fun RadioScreen(vm: PlayerViewModel) {
    val C = LocalAppColors.current
    val tracks by vm.tracks.collectAsState()
    val counts by vm.playCounts.collectAsState()

    val stations = remember(tracks, counts) {
        listOf(
            Station("Shuffle All", "Everything, randomized", Color(0xFFC77DFF), Color(0xFF7B2CBF)) { it.shuffled() },
            Station("Heavy Rotation", "Your most played", Color(0xFFFF6B6B), Color(0xFFEF476F)) { t ->
                val byId = t.associateBy { s -> s.id }
                counts.entries.sortedByDescending { it.value }.mapNotNull { byId[it.key] }.ifEmpty { t.shuffled() }
            },
            Station("Fresh Finds", "Recently added", Color(0xFF4ECDC4), Color(0xFF1B9AAA)) { it.sortedByDescending { s -> s.dateAdded } },
            Station("Deep Cuts", "Rarely played", Color(0xFFFFE66D), Color(0xFFFF9F1C)) { t ->
                t.sortedBy { s -> counts[s.id] ?: 0 }
            },
            Station("A–Z Journey", "Alphabetical ride", Color(0xFF06D6A0), Color(0xFF118AB2)) { it.sortedBy { s -> s.title.lowercase() } },
            Station("Time Machine", "Oldest first", Color(0xFF8E8E93), Color(0xFF3A3A3C)) { it.sortedBy { s -> s.dateAdded } },
        )
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        Text("Radio", color = C.text, fontWeight = FontWeight.Black, fontSize = 26.sp, modifier = Modifier.padding(16.dp, 10.dp))
        if (tracks.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Add music to start a station.", color = C.textSecondary) }
            return@Column
        }
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 160.dp)) {
            items(stations) { st ->
                Box(
                    Modifier.padding(6.dp).fillMaxWidth().aspectRatio(1.1f)
                        .clip(RoundedCornerShape(Radius.lg))
                        .background(Brush.linearGradient(listOf(st.c1, st.c2)))
                        .pressableScale(onClick = {
                            val mix = st.pick(tracks).take(100)
                            if (mix.isNotEmpty()) vm.playList(mix, mix.first().id)
                        })
                        .padding(14.dp),
                ) {
                    Icon(Icons.Filled.Radio, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.align(Alignment.TopEnd).size(22.dp))
                    Column(Modifier.align(Alignment.BottomStart)) {
                        Text(st.title, color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
                        Text(st.subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
