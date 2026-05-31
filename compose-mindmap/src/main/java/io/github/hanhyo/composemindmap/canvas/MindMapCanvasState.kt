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
}

@Stable
class MindMapCanvasState(initialScale: Float = 1f) {
    var scale by mutableFloatStateOf(initialScale)
        internal set
    var offset by mutableStateOf(Offset.Zero)
        internal set
    internal var dragging by mutableStateOf<NodeDragState?>(null)

    internal var commandVersion by mutableIntStateOf(0)
    internal var pendingCommand: ViewportCommand? = null

    fun centerRoot() {
        pendingCommand = ViewportCommand.CenterRoot
        commandVersion++
    }

    fun fitContent(padding: Dp = 24.dp) {
        pendingCommand = ViewportCommand.FitContent(padding)
        commandVersion++
    }

    fun focusNode(nodeId: String, padding: Dp = 24.dp) {
        pendingCommand = ViewportCommand.FocusNode(nodeId, padding)
        commandVersion++
    }

    @Deprecated("Use centerRoot()", ReplaceWith("centerRoot()"))
    fun center() = centerRoot()
}

@Composable
fun rememberMindMapCanvasState(initialScale: Float = 1f): MindMapCanvasState =
    remember { MindMapCanvasState(initialScale) }

@Composable
fun rememberSaveableMindMapCanvasState(initialScale: Float = 1f): MindMapCanvasState =
    rememberSaveable(saver = mindMapCanvasStateSaver(initialScale)) {
        MindMapCanvasState(initialScale)
    }

private fun mindMapCanvasStateSaver(initialScale: Float) = Saver<MindMapCanvasState, FloatArray>(
    save = { state -> floatArrayOf(state.scale, state.offset.x, state.offset.y) },
    restore = { saved ->
        MindMapCanvasState(initialScale).apply {
            scale = saved[0]
            offset = Offset(saved[1], saved[2])
        }
    },
)
