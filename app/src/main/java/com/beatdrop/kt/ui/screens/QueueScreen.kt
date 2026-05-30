package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.beatdrop.kt.PlayerViewModel
import com.beatdrop.kt.ui.components.pressableScale
import com.beatdrop.kt.ui.theme.LocalAppColors
import com.beatdrop.kt.ui.theme.Radius

@Composable
fun QueueScreen(vm: PlayerViewModel, onClose: () -> Unit) {
    val C = LocalAppColors.current
    val ctx = LocalContext.current
    val queue by vm.queue.collectAsState()
    val current by vm.current.collectAsState()

    Column(Modifier.fillMaxSize().background(C.bg0).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClose) { Icon(Icons.Filled.KeyboardArrowDown, "Close", tint = C.text) }
            Text("Up Next", color = C.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        if (queue.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Queue is empty", color = C.textSecondary) }
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 40.dp)) {
            itemsIndexed(queue) { index, t ->
                val isCurrent = current?.id == t.id
                Row(
                    Modifier.fillMaxWidth().pressableScale(onClick = { vm.playQueueIndex(index) })
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(44.dp).clip(RoundedCornerShape(Radius.sm)).background(C.bg3)) {
                        AsyncImage(model = ImageRequest.Builder(ctx).data(t.artworkUri).crossfade(true).build(),
                            contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, color = if (isCurrent) C.accent else C.text,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                        Text(t.artist, color = C.textSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (isCurrent) {
                        Icon(Icons.Filled.GraphicEq, "Now playing", tint = C.accent, modifier = Modifier.size(18.dp))
                    } else {
                        IconButton(onClick = { vm.removeFromQueue(index) }) {
                            Icon(Icons.Filled.Close, "Remove", tint = C.textTertiary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
