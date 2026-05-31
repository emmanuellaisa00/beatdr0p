package com.beatdrop.kt.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.beatdrop.kt.PlayerViewModel
import com.beatdrop.kt.ui.theme.LocalAppColors
import com.beatdrop.kt.ui.components.AppleLyrics
import com.beatdrop.kt.ui.components.rememberArtworkColor
import com.beatdrop.kt.ui.components.glassBlur
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.draw.alpha

@Composable
fun NowPlayingScreen(vm: PlayerViewModel, onCollapse: () -> Unit, onOpenQueue: () -> Unit = {}) {
    val C = LocalAppColors.current
    val ctx = LocalContext.current
    val track by vm.current.collectAsState()
    val isPlaying by vm.isPlaying.collectAsState()
    val pos by vm.position.collectAsState()
    val dur by vm.duration.collectAsState()
    val lyrics by vm.lyrics.collectAsState()
    val lyricsLoading by vm.lyricsLoading.collectAsState()
    val activeLyric by vm.activeLyric.collectAsState()
    var showLyrics by remember { mutableStateOf(false) }
    val shuffle by vm.shuffle.collectAsState()
    val repeat by vm.repeat.collectAsState()

    val t = track ?: run {
        Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Nothing playing", color = C.textSecondary) }
        return
    }

    // Breathing pulse while playing (port of NowPlaying artwork pulse)
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.025f,
        animationSpec = infiniteRepeatable(tween(2600, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "pulseScale",
    )
    val artScale by animateFloatAsState(if (isPlaying) 1f else 0.9f, spring(stiffness = Spring.StiffnessLow), label = "art")

    // Dynamic color extracted from the album art (Apple Music / Spotify style).
    val artColor = rememberArtworkColor(t.artworkUri)

    // Swipe-down-to-dismiss
    var dragAccum by remember { mutableStateOf(0f) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(artColor.copy(alpha = 0.55f), Color(0xFF0B0B0F))))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = { if (dragAccum > 180f) onCollapse(); dragAccum = 0f },
                ) { _, dy -> if (dy > 0) dragAccum += dy }
            }
    ) {
        // Blurred album-art backdrop for liquid-glass depth (API 31+).
        AsyncImage(
            model = ImageRequest.Builder(ctx).data(t.artworkUri).crossfade(true).build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize().glassBlur(60f).alpha(0.35f),
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
      Box(Modifier.fillMaxSize().statusBarsPadding()) {
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCollapse) { Icon(Icons.Filled.KeyboardArrowDown, "Collapse", tint = C.text) }
                Spacer(Modifier.weight(1f))
                Text("NOW PLAYING", color = C.textSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { com.beatdrop.kt.playback.AudioOutput.openSwitcher(ctx) }) {
                    Icon(Icons.Filled.Speaker, "Audio output", tint = C.text)
                }
                IconButton(onClick = onOpenQueue) { Icon(Icons.Filled.QueueMusic, "Queue", tint = C.text) }
            }
            Spacer(Modifier.height(12.dp))

            if (!showLyrics) {
                Box(Modifier.fillMaxWidth().weight(1f).padding(8.dp), Alignment.Center) {
                    Box(Modifier.fillMaxWidth().aspectRatio(1f).scale(artScale * (if (isPlaying) pulse else 1f))
                        .clip(RoundedCornerShape(24.dp)).background(C.bg3), Alignment.Center) {
                        AsyncImage(model = ImageRequest.Builder(ctx).data(t.artworkUri).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                }
            } else {
                if (lyricsLoading && lyrics.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        CircularProgressIndicator(color = C.accent)
                    }
                } else if (lyrics.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        Text("No lyrics found.\nAdd a .lrc next to the audio.", color = C.textSecondary, textAlign = TextAlign.Center)
                    }
                } else {
                    AppleLyrics(lyrics, activeLyric, Modifier.weight(1f)) { vm.seekTo(it) }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(t.title, color = C.text, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(t.artist, color = C.textSecondary, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))

            val safeDur = dur.coerceAtLeast(1L)
            Slider(value = pos.coerceIn(0, safeDur).toFloat(), onValueChange = { vm.seekTo(it.toLong()) }, valueRange = 0f..safeDur.toFloat())
            Row(Modifier.fillMaxWidth()) {
                Text(fmt(pos), color = C.textSecondary, fontSize = 11.sp)
                Spacer(Modifier.weight(1f))
                Text(fmt(dur), color = C.textSecondary, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), Arrangement.Center, Alignment.CenterVertically) {
                IconButton(onClick = { vm.toggleShuffle() }) {
                    Icon(Icons.Filled.Shuffle, "Shuffle", tint = if (shuffle) C.accent else C.textSecondary)
                }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vm.prev() }, Modifier.size(56.dp)) { Icon(Icons.Filled.SkipPrevious, null, tint = C.text, modifier = Modifier.size(36.dp)) }
                Spacer(Modifier.width(16.dp))
                Box(Modifier.size(72.dp).clip(CircleShape).background(C.accent), Alignment.Center) {
                    IconButton(onClick = { vm.togglePlay() }, Modifier.size(72.dp)) {
                        Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = { vm.next() }, Modifier.size(56.dp)) { Icon(Icons.Filled.SkipNext, null, tint = C.text, modifier = Modifier.size(36.dp)) }
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { vm.cycleRepeat() }) {
                    Icon(
                        if (repeat == androidx.media3.common.Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        "Repeat",
                        tint = if (repeat != androidx.media3.common.Player.REPEAT_MODE_OFF) C.accent else C.textSecondary,
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showLyrics = !showLyrics }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(
                    if (showLyrics) "Show artwork"
                    else if (lyricsLoading) "Finding lyrics…"
                    else if (lyrics.isEmpty()) "No lyrics found"
                    else "Show lyrics",
                    color = if (lyrics.isEmpty() && !showLyrics) C.textTertiary else C.accent,
                )
            }
        }
        }
    }
}

