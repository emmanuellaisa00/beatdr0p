package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.PlayerViewModel
import com.beatdrop.kt.ui.theme.LocalAppColors
import com.beatdrop.kt.ui.theme.Radius

/** Listening stats derived from local play counts (persisted via DataStore). */
@Composable
fun StatsScreen(vm: PlayerViewModel) {
    val C = LocalAppColors.current
    val counts by vm.playCounts.collectAsState()
    val tracks by vm.tracks.collectAsState()
    val byId = remember(tracks) { tracks.associateBy { it.id } }

    val totalPlays = counts.values.sum()
    val topTracks = remember(counts, tracks) {
        counts.entries.sortedByDescending { it.value }.mapNotNull { e -> byId[e.key]?.let { it to e.value } }.take(10)
    }
    val topArtists = remember(counts, tracks) {
        counts.entries.mapNotNull { e -> byId[e.key]?.let { it.artist to e.value } }
            .groupBy { it.first }.mapValues { it.value.sumOf { p -> p.second } }
            .entries.sortedByDescending { it.value }.take(5)
    }
    val maxPlay = (topTracks.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(bottom = 160.dp)) {
        item {
            Text("Your Stats", color = C.text, fontWeight = FontWeight.Black, fontSize = 26.sp,
                modifier = Modifier.padding(16.dp, 10.dp))
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).clip(RoundedCornerShape(Radius.lg))
                    .background(C.accentSoft).padding(20.dp),
            ) {
                Column {
                    Text("$totalPlays", color = C.accent, fontSize = 40.sp, fontWeight = FontWeight.Black)
                    Text("total plays", color = C.textSecondary, fontSize = 13.sp)
                }
            }
        }
        if (totalPlays == 0) {
            item { Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) { Text("Play some music to see stats.", color = C.textSecondary) } }
            return@LazyColumn
        }
        item { Header("TOP ARTISTS") }
        itemsIndexed(topArtists) { i, (artist, plays) ->
            Row(Modifier.fillMaxWidth().padding(16.dp, 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("${i + 1}", color = C.accent, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                Text(artist, color = C.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$plays plays", color = C.textSecondary, fontSize = 12.sp)
            }
        }
        item { Header("TOP SONGS") }
        itemsIndexed(topTracks) { i, (t, plays) ->
            Column(Modifier.fillMaxWidth().padding(16.dp, 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${i + 1}", color = C.accent, fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, color = C.text, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        Text(t.artist, color = C.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Text("$plays", color = C.textSecondary, fontSize = 12.sp)
                }
                Spacer(Modifier.height(6.dp))
                Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(C.bg3)) {
                    Box(Modifier.fillMaxWidth(plays.toFloat() / maxPlay).fillMaxHeight().background(C.accent))
                }
            }
        }
    }
}

@Composable
private fun Header(t: String) {
    val C = LocalAppColors.current
    Text(t, color = C.textTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, top = 18.dp, bottom = 4.dp))
}
