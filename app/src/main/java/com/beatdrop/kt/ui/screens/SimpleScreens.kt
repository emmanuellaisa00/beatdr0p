package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.ui.theme.LocalAppColors

/** Discover / Radio / DJ placeholders — port targets for later turns. */
@Composable
fun PlaceholderScreen(title: String, subtitle: String) {
    val C = LocalAppColors.current
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(24.dp)) {
        Text(title, color = C.text, fontSize = 26.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 10.dp))
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.MusicNote, null, tint = C.accent, modifier = Modifier.size(56.dp))
                Spacer(Modifier.height(12.dp))
                Text(subtitle, color = C.textSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun PermissionPrompt(onRequest: () -> Unit) {
    val C = LocalAppColors.current
    Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Filled.MusicNote, null, tint = C.accent, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text("Allow access to your music", color = C.text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))
        Text("BeatDrop reads audio files on your device to build your library.",
            color = C.textSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRequest) { Text("Grant permission") }
    }
}
