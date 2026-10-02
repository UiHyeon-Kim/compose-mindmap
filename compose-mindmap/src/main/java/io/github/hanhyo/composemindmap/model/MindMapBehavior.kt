package io.github.hanhyo.composemindmap.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** How the viewport is placed when no saved position or explicit command exists. */
enum class InitialViewportPolicy {
    ROOT_ALIGNED,
    FIT_CONTENT,
    NONE,
}

@Immutable
/** Gesture switches, zoom limits, edit affordances, and initial viewport behavior. */
data class MindMapBehavior(
    val minScale: Float = 0.5f,
    val maxScale: Float = 3f,
    val zoomEnabled: Boolean = true,
    val panEnabled: Boolean = true,
    val nodeDraggingEnabled: Boolean = true,
    val addChildButtonsVisible: Boolean = true,
    val initialViewportPolicy: InitialViewportPolicy = InitialViewportPolicy.ROOT_ALIGNED,
    val centerTopPadding: Dp = 48.dp,
    val centerStartPadding: Dp = 48.dp,
    val fitContentPadding: Dp = 24.dp,
) {
    init {
        require(minScale.isFinite() && minScale > 0f) { "minScale must be finite and greater than zero" }
        require(maxScale.isFinite() && maxScale >= minScale) { "maxScale must be finite and greater than or equal to minScale" }
    }
}
