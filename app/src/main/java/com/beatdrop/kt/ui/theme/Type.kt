package com.beatdrop.kt.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * A tight, opinionated type scale — the backbone of the Apple-Music / Spotify
 * "premium" feel. Use these everywhere instead of ad-hoc font sizes so the app
 * reads as one cohesive product.
 */
object Type {
    val largeTitle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp, lineHeight = 38.sp)
    val title1     = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.4).sp, lineHeight = 32.sp)
    val title2     = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.3).sp, lineHeight = 28.sp)
    val title3     = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp, lineHeight = 24.sp)
    val headline   = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.1).sp, lineHeight = 22.sp)
    val body       = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal, lineHeight = 21.sp)
    val callout    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, lineHeight = 19.sp)
    val subhead    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 18.sp)
    val footnote   = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal, lineHeight = 16.sp)
    val caption    = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp, lineHeight = 14.sp)
    val overline   = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, lineHeight = 14.sp)
}
