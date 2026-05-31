package com.beatdrop.kt.ui.components

import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.beatdrop.kt.ui.theme.LocalAppColors

/**
 * Real backdrop-blur (frosted glass). On API 31+ uses RenderEffect for a true
 * blur; on older devices it gracefully degrades to a translucent scrim (the
 * approach Chris Banes' Haze and the platform itself use).
 *
 * Apply [glassBlur] to the BACKGROUND layer you want blurred, then draw the
 * glass panel (translucent + border) on top.
 */
fun Modifier.glassBlur(radiusPx: Float = 40f): Modifier =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        this.graphicsLayer {
            renderEffect = RenderEffect
                .createBlurEffect(radiusPx, radiusPx, Shader.TileMode.CLAMP)
                .asComposeRenderEffect()
        }
    } else this

/**
 * A frosted-glass surface: translucent fill + subtle border + top highlight.
 * Sits on top of a blurred backdrop for the iOS-26 "liquid glass" look.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable () -> Unit,
) {
    val C = LocalAppColors.current
    Box(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(if (C.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.55f))
            .background(if (C.isDark) C.glassTint else Color.White.copy(alpha = 0.35f))
            .border(0.8.dp, C.liquidGlassBorder, RoundedCornerShape(cornerRadius)),
    ) { content() }
}
