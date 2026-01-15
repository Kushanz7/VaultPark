package com.kushan.vaultpark.ui.utils

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * Pulsing scale animation for status indicators
 * Animates between 1f and 1.15f over 2 seconds
 */
@Composable
fun pulsingScaleAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_scale")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
}

/**
 * Pulsing opacity animation for subtle glow effects
 * Animates between 0.6f and 1f over 2 seconds
 */
@Composable
fun pulsingOpacityAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_opacity")
    return infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_opacity"
    )
}

/**
 * Button press scale animation
 * Animates between 1f and 0.95f
 */
@Composable
fun pressScaleAnimation(isPressed: Boolean): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "press_animation")
    return infiniteTransition.animateFloat(
        initialValue = if (isPressed) 0.95f else 1f,
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween<Float>(100),
            repeatMode = RepeatMode.Reverse
        ),
        label = "press_scale"
    )
}

/**
 * Refresh indicator glow animation
 * Creates a pulsing glow effect on refresh
 */
@Composable
fun refreshGlowAnimation(): State<Float> {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_glow")
    return infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "refresh_glow"
    )
}
