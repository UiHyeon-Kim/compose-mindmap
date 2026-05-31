package io.github.hanhyo.composemindmap.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class InitialViewportPolicy {
    ROOT_ALIGNED,
    FIT_CONTENT,
    NONE,
}

@Immutable
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
        require(minScale > 0f) { "minScale must be greater than zero" }
        require(maxScale >= minScale) { "maxScale must be greater than or equal to minScale" }
    }
}
