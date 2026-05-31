package io.github.hanhyo.composemindmap.canvas

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEngine
import io.github.hanhyo.composemindmap.layout.TopDownTreeLayoutEngine
import io.github.hanhyo.composemindmap.model.MindMapBehavior
import io.github.hanhyo.composemindmap.model.MindMapNodeWithPayload
import io.github.hanhyo.composemindmap.model.MindMapStyle
import io.github.hanhyo.composemindmap.model.MindMapValidationResult
import io.github.hanhyo.composemindmap.model.defaultNodeSize

@Composable
fun <T> PayloadMindMapCanvas(
    nodes: List<MindMapNodeWithPayload<T>>,
    modifier: Modifier = Modifier,
    state: MindMapCanvasState = rememberMindMapCanvasState(),
    style: MindMapStyle = MindMapStyle(),
    behavior: MindMapBehavior = MindMapBehavior(),
    selectedNodeId: String? = null,
    editMode: Boolean = false,
    layoutEngine: MindMapLayoutEngine = TopDownTreeLayoutEngine,
    nodeSize: (MindMapNodeWithPayload<T>) -> DpSize = { style.defaultNodeSize },
    canvasNodeRenderer: MindMapCanvasNodeRenderer = DefaultMindMapCanvasNodeRenderer,
    edgeRenderer: MindMapEdgeRenderer = CurvedMindMapEdgeRenderer,
    collapsedNodeIds: Set<String> = emptySet(),
    editPolicy: MindMapEditPolicy = DefaultMindMapEditPolicy,
    addChildActionLayout: MindMapAddChildActionLayout = DefaultMindMapAddChildActionLayout,
    editDecorationRenderer: MindMapEditDecorationRenderer? = null,
    semanticLabelProvider: MindMapSemanticLabelProvider = DefaultMindMapSemanticLabelProvider,
    nodeContent: (@Composable (MindMapNodeWithPayload<T>, MindMapNodeVisualState) -> Unit)? = null,
    onValidationError: (MindMapValidationResult.Invalid) -> Unit = {},
    onNodeClick: (nodeId: String) -> Unit = {},
    onNodeLongClick: (nodeId: String) -> Unit = {},
    onCanvasClick: () -> Unit = {},
    onAddChildClick: (parentId: String) -> Unit = {},
    onNodeMove: (nodeId: String, newParentId: String) -> Unit = { _, _ -> },
) {
    val payloadById = nodes.associateBy { it.node.id }
    MindMapCanvas(
        nodes = nodes.map { it.node },
        modifier = modifier,
        state = state,
        style = style,
        behavior = behavior,
        selectedNodeId = selectedNodeId,
        editMode = editMode,
        layoutEngine = layoutEngine,
        nodeSize = { node -> nodeSize(payloadById.getValue(node.id)) },
        canvasNodeRenderer = canvasNodeRenderer,
        edgeRenderer = edgeRenderer,
        collapsedNodeIds = collapsedNodeIds,
        editPolicy = editPolicy,
        addChildActionLayout = addChildActionLayout,
        editDecorationRenderer = editDecorationRenderer,
        semanticLabelProvider = semanticLabelProvider,
        nodeContent = nodeContent?.let { content ->
            { node, visualState -> content(payloadById.getValue(node.id), visualState) }
        },
        onValidationError = onValidationError,
        onNodeClick = onNodeClick,
        onNodeLongClick = onNodeLongClick,
        onCanvasClick = onCanvasClick,
        onAddChildClick = onAddChildClick,
        onNodeMove = onNodeMove,
    )
}
