package com.beatdrop.kt.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beatdrop.kt.lyrics.LyricLine
import com.beatdrop.kt.ui.theme.LocalAppColors

/**
 * Apple-Music-style synced lyrics:
 *  - big, bold, bright ACTIVE line that scales up
 *  - past/future lines dimmed and slightly smaller
 *  - smooth spring transitions as the active line changes
 *  - active line auto-centers in the viewport
 *  - tap a line to seek to it
 *  - pulsing dots placeholder for instrumental gaps (blank lines)
 */
@Composable
fun AppleLyrics(
    lines: List<LyricLine>,
    activeIndex: Int,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit = {},
) {
    val C = LocalAppColors.current
    val state = rememberLazyListState()

    // Keep the active line vertically centered (offset so it sits ~1/3 down).
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0) {
            state.animateScrollToItem(
                index = activeIndex.coerceAtLeast(0),
                scrollOffset = -260, // pull the active line up toward center
            )
        }
    }

    LazyColumn(
        state = state,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(top = 120.dp, bottom = 260.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        itemsIndexed(lines, key = { i, _ -> i }) { i, line ->
            val isActive = i == activeIndex
            val isPast = i < activeIndex

            val scale by animateFloatAsState(
                targetValue = if (isActive) 1f else 0.92f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "lyricScale",
            )
            val targetColor = when {
                isActive -> Color.White
                isPast -> C.textTertiary.copy(alpha = 0.45f)
                else -> C.textSecondary.copy(alpha = 0.55f)
            }
            val color by animateColorAsState(targetColor, label = "lyricColor")

            if (line.text.isBlank()) {
                // Instrumental gap → three pulsing dots
                GapDots(isActive)
            } else {
                Text(
                    text = line.text,
                    color = color,
                    fontSize = if (isActive) 26.sp else 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    lineHeight = if (isActive) 32.sp else 28.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                        .graphicsLayer {
                            scaleX = scale; scaleY = scale
                            transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0f, 0.5f)
                        }
                        .pressableScale(onClick = { onSeek(line.timeMs) }, scaleTo = 0.97f, haptic = false),
                )
            }
        }
    }
}

@Composable
private fun GapDots(active: Boolean) {
    val C = LocalAppColors.current
    val alpha by animateFloatAsState(if (active) 1f else 0.4f, label = "gap")
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(3) {
            Text("●", color = (if (active) Color.White else C.textTertiary).copy(alpha = alpha), fontSize = 12.sp)
        }
    }
}
