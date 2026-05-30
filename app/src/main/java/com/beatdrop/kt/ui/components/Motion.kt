package com.beatdrop.kt.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalView

/**
 * Port of the RN Motion.tsx `ScalePressable`: spring press-scale + haptic.
 * Use as a Modifier so it composes onto any tappable surface.
 */
@Composable
fun Modifier.pressableScale(
    onClick: () -> Unit,
    scaleTo: Float = 0.97f,
    haptic: Boolean = true,
): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) scaleTo else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "pressScale",
    )
    val view = LocalView.current
    return this
        .scale(scale)
        .clickable(interaction, indication = null) {
            if (haptic) view.performHapticFeedback(android.view.HapticFeedbackConstants.VIRTUAL_KEY)
            onClick()
        }
}
