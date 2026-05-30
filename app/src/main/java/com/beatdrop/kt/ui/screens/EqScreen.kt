package com.beatdrop.kt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.playback.EqEngine
import com.beatdrop.kt.ui.components.pressableScale
import com.beatdrop.kt.ui.theme.LocalAppColors

/** Real EQ UI backed by android.media.audiofx.Equalizer (native DSP). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EqScreen(onBack: () -> Unit) {
    val C = LocalAppColors.current
    val enabled by EqEngine.enabled.collectAsState()
    val bands by EqEngine.bands.collectAsState()
    val presets by EqEngine.presets.collectAsState()
    val bass by EqEngine.bassStrength.collectAsState()

    LazyColumn(Modifier.fillMaxSize().statusBarsPadding(), contentPadding = PaddingValues(bottom = 160.dp)) {
        item {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, "Back", tint = C.text) }
                Text("Equalizer", color = C.text, fontWeight = FontWeight.Black, fontSize = 22.sp, modifier = Modifier.weight(1f))
                Switch(checked = enabled, onCheckedChange = { EqEngine.setEnabled(it) })
            }
        }

        if (bands.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
                    Text(
                        "EQ initialises once playback starts.\nPlay a track, then return here.",
                        color = C.textSecondary, textAlign = TextAlign.Center,
                    )
                }
            }
            return@LazyColumn
        }

        // Horizontal band rows (reliable layout, each band = label + slider + dB)
        items(bands.size) { i ->
            val band = bands[i]
            val db = band.levelMb / 100f
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(freqLabel(band.centerFreqHz), color = C.textSecondary, fontSize = 12.sp, modifier = Modifier.width(48.dp))
                Slider(
                    value = band.levelMb.toFloat(),
                    onValueChange = { EqEngine.setBandLevel(band.index, it.toInt().toShort()) },
                    valueRange = band.minMb.toFloat()..band.maxMb.toFloat(),
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
                Text("%+.0f dB".format(db), color = C.text, fontSize = 11.sp, modifier = Modifier.width(56.dp), textAlign = TextAlign.End)
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Bass Boost  ${bass / 10}%", color = C.text, fontWeight = FontWeight.SemiBold)
                Slider(value = bass.toFloat(), onValueChange = { EqEngine.setBassStrength(it.toInt()) },
                    valueRange = 0f..1000f, enabled = enabled)
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Text("Presets", color = C.textTertiary, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, bottom = 6.dp))
            FlowRow(Modifier.padding(horizontal = 12.dp)) {
                presets.forEachIndexed { i, name ->
                    Box(
                        Modifier.padding(4.dp).clip(RoundedCornerShape(20.dp)).background(C.bg3)
                            .pressableScale(onClick = { EqEngine.setEnabled(true); EqEngine.applyPreset(i.toShort()) })
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text(name, color = C.text, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

private fun freqLabel(hz: Int): String = if (hz >= 1000) "${hz / 1000}k" else "$hz"
