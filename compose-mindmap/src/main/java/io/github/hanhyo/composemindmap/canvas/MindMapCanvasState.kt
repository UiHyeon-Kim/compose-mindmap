package io.github.hanhyo.composemindmap.canvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal data class NodeDragState(
    val nodeId: String,
    val screenPos: Offset,
    val dropTargetId: String?,
)

internal sealed class ViewportCommand {
    data object CenterRoot : ViewportCommand()
    data class FitContent(val padding: Dp) : ViewportCommand()
    data class FocusNode(val nodeId: String, val padding: Dp) : ViewportCommand()
    data class ZoomBy(val factor: Float) : ViewportCommand()
}

@Stable
/** Viewport scale/offset and commands for [MindMapCanvas]. The caller cannot mutate coordinates directly. */
class MindMapCanvasState(initialScale: Float = 1f) {
    init {
        require(initialScale.isFinite() && initialScale > 0f) { "initialScale must be finite and greater than zero" }
    }
    var scale by mutableFloatStateOf(initialScale)
        internal set
    var offset by mutableStateOf(Offset.Zero)
        internal set
    internal var dragging by mutableStateOf<NodeDragState?>(null)

    internal var commandVersion by mutableIntStateOf(0)
    internal var pendingCommand: ViewportCommand? = null
    internal var initialViewportApplied by mutableStateOf(false)

    fun centerRoot() {
        pendingCommand = ViewportCommand.CenterRoot
        commandVersion++
    }

    fun fitContent(padding: Dp = 24.dp) {
        pendingCommand = ViewportCommand.FitContent(padding)
        commandVersion++
    }

    /** Centers [nodeId], reducing zoom as needed to fit within screen [padding] when scale limits allow. */
    fun focusNode(nodeId: String, padding: Dp = 24.dp) {
        pendingCommand = ViewportCommand.FocusNode(nodeId, padding)
        commandVersion++
    }

    /** Changes zoom around the viewport center, clamped to [io.github.hanhyo.composemindmap.model.MindMapBehavior] limits. */
    fun zoomBy(factor: Float) {
        require(factor > 0f && factor.isFinite()) { "factor must be finite and greater than zero" }
        pendingCommand = ViewportCommand.ZoomBy(factor)
        commandVersion++
    }

    @Deprecated("Use centerRoot()", ReplaceWith("centerRoot()"))
    fun center() = centerRoot()
}

@Composable
/** Remembers a viewport for the current composition only. */
fun rememberMindMapCanvasState(initialScale: Float = 1f): MindMapCanvasState =
    remember { MindMapCanvasState(initialScale) }

@Composable
/** Saves scale, translation, and initial-alignment completion across Activity recreation. */
fun rememberSaveableMindMapCanvasState(initialScale: Float = 1f): MindMapCanvasState =
    rememberSaveable(saver = mindMapCanvasStateSaver(initialScale)) {
        MindMapCanvasState(initialScale)
    }

private fun mindMapCanvasStateSaver(initialScale: Float) = Saver<MindMapCanvasState, FloatArray>(
    save = { state -> floatArrayOf(state.scale, state.offset.x, state.offset.y, if (state.initialViewportApplied) 1f else 0f) },
    restore = { saved ->
        MindMapCanvasState(initialScale).apply {
            scale = saved[0]
            offset = Offset(saved[1], saved[2])
            initialViewportApplied = saved.getOrNull(3) == 1f
        }
    },
)
