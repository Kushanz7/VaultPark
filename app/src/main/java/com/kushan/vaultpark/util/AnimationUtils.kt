package com.kushan.vaultpark.util

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Slide up animation for bottom sheets and modals
 */
fun slideUpEnter(): EnterTransition = slideInVertically(initialOffsetY = { it })
fun slideUpExit(): ExitTransition = slideOutVertically(targetOffsetY = { it })

/**
 * Slide down animation for dropdowns
 */
fun slideDownEnter(): EnterTransition = slideInVertically(initialOffsetY = { -it })
fun slideDownExit(): ExitTransition = slideOutVertically(targetOffsetY = { -it })

/**
 * Fade + Scale animation for dialogs
 */
fun dialogEnter(): EnterTransition = fadeIn() + scaleIn(initialScale = 0.9f)
fun dialogExit(): ExitTransition = fadeOut() + scaleOut(targetScale = 0.9f)

/**
 * Staggered entrance animation for list items
 */
@Composable
fun AnimatedListItem(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(initialScale = 0.95f),
        exit = fadeOut() + scaleOut(targetScale = 0.95f),
        modifier = modifier,
        content = content
    )
}

/**
 * Fade-only animation for text and simple elements
 */
fun fadeOnlyEnter(): EnterTransition = fadeIn()
fun fadeOnlyExit(): ExitTransition = fadeOut()
