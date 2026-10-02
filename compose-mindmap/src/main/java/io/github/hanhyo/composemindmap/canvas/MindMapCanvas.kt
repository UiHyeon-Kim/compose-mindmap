package io.github.hanhyo.composemindmap.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import io.github.hanhyo.composemindmap.R
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEngine
import io.github.hanhyo.composemindmap.layout.MindMapLayoutInput
import io.github.hanhyo.composemindmap.layout.MindMapLayoutNode
import io.github.hanhyo.composemindmap.layout.MindMapLayoutResult
import io.github.hanhyo.composemindmap.layout.MindMapRootAlignment
import io.github.hanhyo.composemindmap.layout.TopDownTreeLayoutEngine
import io.github.hanhyo.composemindmap.model.InitialViewportPolicy
import io.github.hanhyo.composemindmap.model.MindMapBehavior
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.MindMapStyle
import io.github.hanhyo.composemindmap.model.MindMapValidationResult
import io.github.hanhyo.composemindmap.model.defaultNodeSize
import io.github.hanhyo.composemindmap.model.validateMindMapNodes
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Displays a single-root tree of [nodes]. The caller owns edits and supplies the updated list.
 * [nodeContent] replaces Canvas-drawn cards; keep [nodeSize] aligned with its measured size.
 * Invalid input invokes [onValidationError] and renders [errorContent] or a localized default.
 */
@Composable
fun MindMapCanvas(
    nodes: List<MindMapNode>,
    modifier: Modifier = Modifier,
    state: MindMapCanvasState = rememberMindMapCanvasState(),
    style: MindMapStyle = MindMapStyle(),
    behavior: MindMapBehavior = MindMapBehavior(),
    selectedNodeId: String? = null,
    editMode: Boolean = false,
    layoutEngine: MindMapLayoutEngine = TopDownTreeLayoutEngine,
    nodeSize: (MindMapNode) -> DpSize = { style.defaultNodeSize },
    canvasNodeRenderer: MindMapCanvasNodeRenderer = DefaultMindMapCanvasNodeRenderer,
    edgeRenderer: MindMapEdgeRenderer = CurvedMindMapEdgeRenderer,
    collapsedNodeIds: Set<String> = emptySet(),
    editPolicy: MindMapEditPolicy = DefaultMindMapEditPolicy,
    addChildActionLayout: MindMapAddChildActionLayout = DefaultMindMapAddChildActionLayout,
    editDecorationRenderer: MindMapEditDecorationRenderer? = null,
    semanticLabelProvider: MindMapSemanticLabelProvider = DefaultMindMapSemanticLabelProvider,
    accessibilityActionLabels: MindMapAccessibilityActionLabels? = null,
    nodeContent: (@Composable (MindMapNode, MindMapNodeVisualState) -> Unit)? = null,
    errorContent: (@Composable (MindMapValidationResult.Invalid) -> Unit)? = null,
    onValidationError: (MindMapValidationResult.Invalid) -> Unit = {},
    onNodeClick: (nodeId: String) -> Unit = {},
    onNodeLongClick: (nodeId: String) -> Unit = {},
    onCanvasClick: () -> Unit = {},
    onAddChildClick: (parentId: String) -> Unit = {},
    onNodeMove: (nodeId: String, newParentId: String) -> Unit = { _, _ -> },
) {
    val density = LocalDensity.current
    val labels = accessibilityActionLabels ?: MindMapAccessibilityActionLabels(
        select = stringResource(R.string.mind_map_select),
        longPress = stringResource(R.string.mind_map_long_press),
        addChild = stringResource(R.string.mind_map_add_child),
    )
    val defaultErrorMessage = stringResource(R.string.mind_map_invalid_tree)
    val visibleNodes = remember(nodes, collapsedNodeIds) {
        nodes.withoutCollapsedSubtrees(collapsedNodeIds)
    }
    val childNodeIds = remember(nodes) { nodes.mapNotNullTo(mutableSetOf()) { it.parentId } }
    val parentById = remember(nodes) { nodes.associate { it.id to it.parentId } }
    val validation = remember(nodes) { validateMindMapNodes(nodes) }
    val layoutResult = remember(visibleNodes, style, nodeSize, validation, layoutEngine, density) {
        if (validation is MindMapValidationResult.Valid) {
            layoutEngine.layout(MindMapLayoutInput(visibleNodes, style, density, nodeSize))
        } else {
            MindMapLayoutResult()
        }
    }
    val layoutedNodes = layoutResult.nodes
    val textMeasurer = rememberTextMeasurer()
    val decorationRenderer = remember(editDecorationRenderer, textMeasurer) {
        editDecorationRenderer ?: DefaultMindMapEditDecorationRenderer(textMeasurer)
    }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var centeredLayoutEngine by remember(state) {
        mutableStateOf<MindMapLayoutEngine?>(if (state.initialViewportApplied) layoutEngine else null)
    }

    LaunchedEffect(validation) {
        if (validation is MindMapValidationResult.Invalid) onValidationError(validation)
    }
    if (validation is MindMapValidationResult.Invalid) {
        Box(modifier.fillMaxSize()) {
            if (errorContent != null) errorContent(validation) else BasicText(defaultErrorMessage)
        }
        return
    }
    LaunchedEffect(canvasSize, state.commandVersion, layoutedNodes.isNotEmpty(), layoutEngine) {
        if (canvasSize.width <= 0 || layoutedNodes.isEmpty()) return@LaunchedEffect
        val command = state.pendingCommand
        val applyInitialPolicy = !state.initialViewportApplied || centeredLayoutEngine !== layoutEngine
        if (command == null && !applyInitialPolicy) return@LaunchedEffect

        val effectiveCommand = command ?: when (behavior.initialViewportPolicy) {
            InitialViewportPolicy.ROOT_ALIGNED -> ViewportCommand.CenterRoot
            InitialViewportPolicy.FIT_CONTENT -> ViewportCommand.FitContent(behavior.fitContentPadding)
            InitialViewportPolicy.NONE -> null
        } ?: return@LaunchedEffect

        applyViewportCommand(effectiveCommand, layoutedNodes, layoutEngine, canvasSize, state, behavior, density)
        state.initialViewportApplied = true
        centeredLayoutEngine = layoutEngine
        state.pendingCommand = null
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
            .pointerInput(layoutedNodes, editMode, selectedNodeId, behavior, editPolicy, collapsedNodeIds) {
                val touchSlop = viewConfiguration.touchSlop
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val localStart = (down.position - state.offset) / state.scale
                    val addChildActions = if (editMode && behavior.addChildButtonsVisible) {
                        layoutedNodes
                            .filter { editPolicy.canAddChild(it.node) }
                            .map { addChildActionLayout.layout(it, style, density) }
                    } else {
                        emptyList()
                    }
                    val hitAction = addChildActions.firstOrNull { it.hitTest(localStart) }
                    val hitNode = layoutedNodes.firstOrNull { it.contains(localStart) }
                    var dragging = false
                    var longPressed = false
                    var totalDrag = Offset.Zero
                    var pressed = true
                    var multiTouchOccurred = false
                    var lastUptimeMillis = down.uptimeMillis

                    while (pressed) {
                        val event = if (!multiTouchOccurred && !dragging && !longPressed && hitNode != null) {
                            val elapsed = lastUptimeMillis - down.uptimeMillis
                            val remaining = (viewConfiguration.longPressTimeoutMillis - elapsed).coerceAtLeast(1L)
                            withTimeoutOrNull(remaining) { awaitPointerEvent() }
                                ?: run {
                                    longPressed = true
                                    onNodeLongClick(hitNode.node.id)
                                    continue
                                }
                        } else {
                            awaitPointerEvent()
                        }
                        val change = event.changes.firstOrNull() ?: break
                        lastUptimeMillis = change.uptimeMillis
                        pressed = event.changes.any { it.pressed }
                        if (event.changes.count { it.pressed } > 1) {
                            multiTouchOccurred = true
                            state.dragging = null
                            val centroid = event.calculateCentroid(useCurrent = true)
                            val pan = event.calculatePan()
                            val oldScale = state.scale
                            val newScale = if (behavior.zoomEnabled) {
                                (oldScale * event.calculateZoom()).coerceIn(behavior.minScale, behavior.maxScale)
                            } else oldScale
                            val appliedPan = if (behavior.panEnabled) pan else Offset.Zero
                            if (centroid != Offset.Unspecified) {
                                state.offset = centroid - (centroid - appliedPan - state.offset) * (newScale / oldScale)
                                state.scale = newScale
                            }
                            event.changes.forEach { it.consume() }
                            continue
                        }
                        if (multiTouchOccurred) continue
                        if (!change.pressed) continue

                        val delta = change.positionChange()
                        totalDrag += delta
                        if (!dragging && (abs(totalDrag.x) > touchSlop || abs(totalDrag.y) > touchSlop)) {
                            dragging = true
                        }
                        if (!dragging) continue

                        if (
                            behavior.nodeDraggingEnabled && editMode && hitNode != null &&
                            editPolicy.canDrag(hitNode.node) && hitNode.node.id == selectedNodeId
                        ) {
                            val localPos = (change.position - state.offset) / state.scale
                            val dropTarget = layoutedNodes.firstOrNull {
                                it.node.id != hitNode.node.id &&
                                    it.contains(localPos) &&
                                    !parentById.isDescendantOf(it.node.id, hitNode.node.id) &&
                                    editPolicy.canDrop(hitNode.node, it.node)
                            }
                            state.dragging = NodeDragState(hitNode.node.id, change.position, dropTarget?.node?.id)
                        } else if (behavior.panEnabled && hitNode == null && hitAction == null) {
                            state.offset += delta
                        }
                        change.consume()
                    }

                    val dragState = state.dragging
                    if (multiTouchOccurred) {
                        state.dragging = null
                    } else if (dragState != null) {
                        dragState.dropTargetId?.let { onNodeMove(dragState.nodeId, it) }
                        state.dragging = null
                    } else if (!dragging && !longPressed) {
                        when {
                            hitAction != null -> onAddChildClick(hitAction.nodeId)
                            hitNode != null -> onNodeClick(hitNode.node.id)
                            else -> onCanvasClick()
                        }
                    }
                }
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            withTransform({
                scale(state.scale, state.scale, pivot = Offset.Zero)
                translate(state.offset.x / state.scale, state.offset.y / state.scale)
            }) {
                val visLeft = -state.offset.x / state.scale
                val visTop = -state.offset.y / state.scale
                val visRight = (canvasSize.width - state.offset.x) / state.scale
                val visBottom = (canvasSize.height - state.offset.y) / state.scale
                layoutResult.edges.forEach { edge ->
                    with(edgeRenderer) { draw(edge, style) }
                }
                layoutedNodes.forEach { layouted ->
                    if (layouted.offset.x + layouted.size.width < visLeft ||
                        layouted.offset.x > visRight ||
                        layouted.offset.y + layouted.size.height < visTop ||
                        layouted.offset.y > visBottom
                    ) return@forEach
                    val visualState = layouted.visualState(state, selectedNodeId, childNodeIds, collapsedNodeIds)
                    if (nodeContent == null) {
                        with(canvasNodeRenderer) {
                            draw(layouted.node, layouted.offset, layouted.size, visualState, style, textMeasurer)
                        }
                    }
                    if (visualState.isDropTarget) {
                        with(decorationRenderer) { drawDropTarget(layouted, style) }
                    }
                    if (editMode && behavior.addChildButtonsVisible && editPolicy.canAddChild(layouted.node)) {
                        val action = addChildActionLayout.layout(layouted, style, density)
                        with(decorationRenderer) { drawAddChildAction(action, style) }
                    }
                }
                val dragState = state.dragging
                val dragged = layoutedNodes.firstOrNull { it.node.id == dragState?.nodeId }
                if (dragState != null && dragged != null && nodeContent == null) {
                    val localPos = (dragState.screenPos - state.offset) / state.scale
                    with(canvasNodeRenderer) {
                        draw(
                            dragged.node,
                            localPos - Offset(dragged.size.width / 2f, dragged.size.height / 2f),
                            dragged.size,
                            MindMapNodeVisualState(isDragGhost = true),
                            style,
                            textMeasurer,
                        )
                    }
                }
            }
        }

        val slotTransformModifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = state.offset.x
                translationY = state.offset.y
                scaleX = state.scale
                scaleY = state.scale
                transformOrigin = TransformOrigin(0f, 0f)
            }

        Box(slotTransformModifier) {
            layoutedNodes.forEach { layouted ->
                val visState = layouted.visualState(state, selectedNodeId, childNodeIds, collapsedNodeIds)
                key(layouted.node.id) {
                    Box(
                        Modifier
                            .offset {
                                IntOffset(layouted.offset.x.roundToInt(), layouted.offset.y.roundToInt())
                            }
                            .size(
                                width = with(density) { layouted.size.width.toDp() },
                                height = with(density) { layouted.size.height.toDp() },
                            )
                            .semantics(mergeDescendants = true) {
                                contentDescription = semanticLabelProvider.label(layouted.node, visState)
                                onClick(label = labels.select) { onNodeClick(layouted.node.id); true }
                                onLongClick(label = labels.longPress) { onNodeLongClick(layouted.node.id); true }
                                if (editMode && behavior.addChildButtonsVisible && editPolicy.canAddChild(layouted.node)) {
                                    customActions = listOf(
                                        CustomAccessibilityAction(
                                            label = labels.addChild,
                                            action = { onAddChildClick(layouted.node.id); true },
                                        )
                                    )
                                }
                            },
                    )
                }
            }
        }

        if (nodeContent != null) {
            Box(slotTransformModifier) {
                layoutedNodes.forEach { layouted ->
                    val visLeft = -state.offset.x / state.scale
                    val visTop = -state.offset.y / state.scale
                    val visRight = (canvasSize.width - state.offset.x) / state.scale
                    val visBottom = (canvasSize.height - state.offset.y) / state.scale
                    if (layouted.offset.x + layouted.size.width < visLeft ||
                        layouted.offset.x > visRight ||
                        layouted.offset.y + layouted.size.height < visTop ||
                        layouted.offset.y > visBottom
                    ) return@forEach
                    key(layouted.node.id) {
                        Box(
                            Modifier
                                .offset {
                                    IntOffset(layouted.offset.x.roundToInt(), layouted.offset.y.roundToInt())
                                }
                                .size(
                                    width = with(density) { layouted.size.width.toDp() },
                                    height = with(density) { layouted.size.height.toDp() },
                                )
                                .clearAndSetSemantics { },
                        ) {
                            nodeContent(layouted.node, layouted.visualState(state, selectedNodeId, childNodeIds, collapsedNodeIds))
                        }
                    }
                }

                val dragState = state.dragging
                val dragged = layoutedNodes.firstOrNull { it.node.id == dragState?.nodeId }
                if (dragState != null && dragged != null) {
                    val localPos = (dragState.screenPos - state.offset) / state.scale
                    Box(
                        Modifier
                            .offset {
                                IntOffset(
                                    x = (localPos.x - dragged.size.width / 2f).roundToInt(),
                                    y = (localPos.y - dragged.size.height / 2f).roundToInt(),
                                )
                            }
                            .size(
                                width = with(density) { dragged.size.width.toDp() },
                                height = with(density) { dragged.size.height.toDp() },
                            ),
                    ) {
                        nodeContent(dragged.node, MindMapNodeVisualState(isDragGhost = true))
                    }
                }
            }
        }
    }
}

private fun MindMapLayoutNode.visualState(
    state: MindMapCanvasState,
    selectedNodeId: String?,
    childNodeIds: Set<String>,
    collapsedNodeIds: Set<String>,
): MindMapNodeVisualState = MindMapNodeVisualState(
    isSelected = node.id == selectedNodeId,
    isDragging = node.id == state.dragging?.nodeId,
    isDropTarget = node.id == state.dragging?.dropTargetId,
    hasChildren = node.id in childNodeIds,
    isCollapsed = node.id in collapsedNodeIds,
)

private fun MindMapLayoutNode.contains(point: Offset): Boolean =
    point.x in offset.x..(offset.x + size.width) && point.y in offset.y..(offset.y + size.height)

internal fun List<MindMapNode>.isDescendantOf(candidateId: String, ancestorId: String): Boolean {
    return associate { it.id to it.parentId }.isDescendantOf(candidateId, ancestorId)
}

private fun Map<String, String?>.isDescendantOf(candidateId: String, ancestorId: String): Boolean {
    val visited = mutableSetOf<String>()
    var current = this[candidateId]
    while (current != null && visited.add(current)) {
        if (current == ancestorId) return true
        current = this[current]
    }
    return false
}

private fun List<MindMapNode>.withoutCollapsedSubtrees(collapsedNodeIds: Set<String>): List<MindMapNode> {
    if (collapsedNodeIds.isEmpty()) return this
    val nodeIds = mapTo(mutableSetOf()) { it.id }
    val validCollapsed = collapsedNodeIds.filter { it in nodeIds }
    if (validCollapsed.isEmpty()) return this
    val childrenMap = groupBy { it.parentId }
    val excluded = mutableSetOf<String>()
    val queue = ArrayDeque<String>()
    for (id in validCollapsed) childrenMap[id].orEmpty().forEach { queue.add(it.id) }
    while (queue.isNotEmpty()) {
        val id = queue.removeFirst()
        if (excluded.add(id)) childrenMap[id].orEmpty().forEach { queue.add(it.id) }
    }
    return filter { it.id !in excluded }
}

private fun MindMapAddChildAction.hitTest(point: Offset): Boolean {
    val dx = point.x - center.x
    val dy = point.y - center.y
    return sqrt(dx * dx + dy * dy) <= touchRadius
}

private fun applyViewportCommand(
    command: ViewportCommand,
    layoutedNodes: List<MindMapLayoutNode>,
    layoutEngine: MindMapLayoutEngine,
    canvasSize: Size,
    state: MindMapCanvasState,
    behavior: MindMapBehavior,
    density: androidx.compose.ui.unit.Density,
) {
    when (command) {
        is ViewportCommand.ZoomBy -> {
            val center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
            val newScale = (state.scale * command.factor).coerceIn(behavior.minScale, behavior.maxScale)
            state.offset = center - (center - state.offset) * (newScale / state.scale)
            state.scale = newScale
        }
        is ViewportCommand.CenterRoot -> {
            val root = layoutedNodes.firstOrNull { it.node.parentId == null } ?: return
            val rootCenter = Offset(root.offset.x + root.size.width / 2f, root.offset.y + root.size.height / 2f)
            state.offset = when (layoutEngine.rootAlignment) {
                MindMapRootAlignment.TOP_CENTER -> Offset(
                    x = canvasSize.width / 2f - rootCenter.x * state.scale,
                    y = with(density) { behavior.centerTopPadding.toPx() },
                )
                MindMapRootAlignment.CENTER_START -> Offset(
                    x = with(density) { behavior.centerStartPadding.toPx() },
                    y = canvasSize.height / 2f - rootCenter.y * state.scale,
                )
                MindMapRootAlignment.CENTER -> Offset(
                    x = canvasSize.width / 2f - rootCenter.x * state.scale,
                    y = canvasSize.height / 2f - rootCenter.y * state.scale,
                )
            }
        }
        is ViewportCommand.FitContent -> {
            val padPx = with(density) { command.padding.toPx() }
            val minX = layoutedNodes.minOf { it.offset.x }
            val minY = layoutedNodes.minOf { it.offset.y }
            val maxX = layoutedNodes.maxOf { it.offset.x + it.size.width }
            val maxY = layoutedNodes.maxOf { it.offset.y + it.size.height }
            val contentW = maxX - minX
            val contentH = maxY - minY
            if (contentW <= 0 || contentH <= 0) return
            val availableW = canvasSize.width - padPx * 2
            val availableH = canvasSize.height - padPx * 2
            val newScale = (minOf(availableW / contentW, availableH / contentH))
                .coerceIn(behavior.minScale, behavior.maxScale)
            state.scale = newScale
            state.offset = Offset(
                x = canvasSize.width / 2f - (minX + contentW / 2f) * newScale,
                y = canvasSize.height / 2f - (minY + contentH / 2f) * newScale,
            )
        }
        is ViewportCommand.FocusNode -> {
            val target = layoutedNodes.firstOrNull { it.node.id == command.nodeId } ?: return
            val padding = with(density) { command.padding.toPx() }.coerceAtLeast(0f)
            val availableWidth = (canvasSize.width - padding * 2f).coerceAtLeast(1f)
            val availableHeight = (canvasSize.height - padding * 2f).coerceAtLeast(1f)
            val fitScale = minOf(availableWidth / target.size.width, availableHeight / target.size.height)
            state.scale = minOf(state.scale, fitScale).coerceIn(behavior.minScale, behavior.maxScale)
            val nodeCenter = Offset(
                target.offset.x + target.size.width / 2f,
                target.offset.y + target.size.height / 2f,
            )
            state.offset = Offset(
                x = canvasSize.width / 2f - nodeCenter.x * state.scale,
                y = canvasSize.height / 2f - nodeCenter.y * state.scale,
            )
        }
    }
}
