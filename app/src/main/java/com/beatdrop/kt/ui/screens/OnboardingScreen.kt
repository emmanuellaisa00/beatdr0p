package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.ui.components.pressableScale
import com.beatdrop.kt.ui.theme.LocalAppColors

@Composable
fun OnboardingScreen(onGetStarted: () -> Unit) {
    val C = LocalAppColors.current
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A1020), Color(0xFF0B0B0F))))
            .statusBarsPadding().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(40.dp))
        Box(Modifier.size(96.dp).clip(CircleShape).background(C.accentSoft), Alignment.Center) {
            Icon(Icons.Filled.MusicNote, null, tint = C.accent, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        Row {
            Text("Beat", color = C.accent, fontSize = 36.sp, fontWeight = FontWeight.Black)
            Text("Drop", color = C.text, fontSize = 36.sp, fontWeight = FontWeight.Black)
        }
        Text("Your music, beautifully played.", color = C.textSecondary, fontSize = 15.sp)
        Spacer(Modifier.height(40.dp))

        Feature(Icons.Filled.MusicNote, "Local library", "Plays the music already on your phone.", C.accent)
        Feature(Icons.Filled.Lyrics, "Synced lyrics", "Drop a .lrc next to a track and sing along.", C.blue)
        Feature(Icons.Filled.QueueMusic, "Playlists & queue", "Build playlists, reorder your queue.", C.green)
        Feature(Icons.Filled.GraphicEq, "DJ mode", "Two decks with a crossfader.", C.orange)

        Spacer(Modifier.weight(1f))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(C.accent)
                .pressableScale(onClick = onGetStarted, haptic = true).padding(vertical = 16.dp),
            Alignment.Center,
        ) {
            Text("Get Started", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
        }
        Spacer(Modifier.height(8.dp))
        Text("BeatDrop reads audio on your device. No account, no uploads.",
            color = C.textTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun Feature(icon: ImageVector, title: String, body: String, accent: Color) {
    val C = LocalAppColors.current
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.18f)), Alignment.Center) {
            Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = C.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(body, color = C.textSecondary, fontSize = 12.sp)
        }
    }
}
