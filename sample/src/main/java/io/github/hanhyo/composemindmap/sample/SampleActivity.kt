package io.github.hanhyo.composemindmap.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import io.github.hanhyo.composemindmap.canvas.CurvedMindMapEdgeRenderer
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.canvas.MindMapEdgeRenderer
import io.github.hanhyo.composemindmap.canvas.MindMapNodeVisualState
import io.github.hanhyo.composemindmap.canvas.OrthogonalMindMapEdgeRenderer
import io.github.hanhyo.composemindmap.canvas.PayloadMindMapCanvas
import io.github.hanhyo.composemindmap.canvas.StraightMindMapEdgeRenderer
import io.github.hanhyo.composemindmap.canvas.rememberSaveableMindMapCanvasState
import io.github.hanhyo.composemindmap.controller.MindMapEditController
import io.github.hanhyo.composemindmap.layout.LeftToRightTreeLayoutEngine
import io.github.hanhyo.composemindmap.layout.MindMapLayoutEngine
import io.github.hanhyo.composemindmap.layout.TopDownTreeLayoutEngine
import io.github.hanhyo.composemindmap.model.MindMapNode
import io.github.hanhyo.composemindmap.model.MindMapBehavior
import io.github.hanhyo.composemindmap.model.MindMapNodeWithPayload
import io.github.hanhyo.composemindmap.model.MindMapStyle
import io.github.hanhyo.composemindmap.model.InitialViewportPolicy
import io.github.hanhyo.composemindmap.model.withPayload

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true
        setContent { MaterialTheme { Showcase() } }
    }
}

private data class TopicPayload(val category: String)

@Composable
private fun Showcase() {
    val viewport = rememberSaveableMindMapCanvasState()
    val editor = remember { MindMapEditController() }
    var nodes by remember { mutableStateOf(sampleNodes) }
    var selected by remember { mutableStateOf<String?>(null) }
    var collapsed by remember { mutableStateOf(emptySet<String>()) }
    var nextId by remember { mutableIntStateOf(1) }
    var leftToRight by remember { mutableStateOf(false) }
    var edgeIndex by remember { mutableIntStateOf(0) }
    var customCards by remember { mutableStateOf(false) }
    var colored by remember { mutableStateOf(true) }

    val layout: MindMapLayoutEngine = if (leftToRight) LeftToRightTreeLayoutEngine else TopDownTreeLayoutEngine
    val edge: MindMapEdgeRenderer = when (edgeIndex) {
        1 -> StraightMindMapEdgeRenderer
        2 -> OrthogonalMindMapEdgeRenderer
        else -> CurvedMindMapEdgeRenderer
    }
    val style = if (colored) MindMapStyle(
        defaultNodeColor = Color(0xFFEAF4FF),
        selectedStrokeColor = Color(0xFF2764B6),
        edgeColor = Color(0xFF85A9D4),
    ) else MindMapStyle()
    val behavior = MindMapBehavior(minScale = 0.35f, initialViewportPolicy = InitialViewportPolicy.FIT_CONTENT)

    fun addChild(parentId: String) {
        nodes = editor.addNode(nodes, MindMapNode("new-${nextId++}", "New topic", parentId = parentId))
    }

    Surface(Modifier.fillMaxSize()) {
        Column {
            Text(
                "Compose MindMap",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
            )
            Text(
                "Tap a node to select it. Drag the selected node onto another node to move it; pinch to zoom.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            ControlRow {
                Control("Layout: ${if (leftToRight) "Left → right" else "Top → down"}") { leftToRight = !leftToRight }
                Control("Edge: ${listOf("Curve", "Straight", "Elbow")[edgeIndex]}") { edgeIndex = (edgeIndex + 1) % 3 }
                Control("Card: ${if (customCards) "Payload" else "Canvas"}") { customCards = !customCards }
                Control("Colors: ${if (colored) "On" else "Off"}") { colored = !colored }
            }
            ControlRow {
                Control("Add child", selected != null) { selected?.let(::addChild) }
                Control("Rename", selected != null) {
                    val node = nodes.firstOrNull { it.id == selected } ?: return@Control
                    nodes = editor.updateNode(nodes, node, node.copy(title = "Edited topic"))
                }
                Control("Collapse", selected != null) {
                    selected?.let { collapsed = if (it in collapsed) collapsed - it else collapsed + it }
                }
                Control("Delete", selected != null && selected != "root") {
                    val node = nodes.firstOrNull { it.id == selected } ?: return@Control
                    nodes = editor.deleteNode(nodes, node, editor.collectSubtree(nodes, node.id))
                    collapsed = collapsed - node.id
                    selected = null
                }
                Control("Undo", editor.canUndo) { editor.undo(nodes)?.let { nodes = it } }
                Control("Redo", editor.canRedo) { editor.redo(nodes)?.let { nodes = it } }
            }
            ControlRow {
                Control("Center root") { viewport.centerRoot() }
                Control("Fit all") { viewport.fitContent() }
                Control("Zoom +") { viewport.zoomBy(1.4f) }
                Control("Zoom −") { viewport.zoomBy(1f / 1.4f) }
                Control("Focus selected", selected != null) { selected?.let { viewport.focusNode(it, 48.dp) } }
            }

            val canvasModifier = Modifier.fillMaxWidth().weight(1f)
            val onSelect: (String) -> Unit = { selected = if (selected == it) null else it }
            val onMove: (String, String) -> Unit = { id, parent -> nodes = editor.moveNode(nodes, id, parent) }
            if (customCards) {
                val typedNodes: List<MindMapNodeWithPayload<TopicPayload>> = nodes.map {
                    it.withPayload(TopicPayload(if (it.parentId == null) "ROOT" else "TOPIC"))
                }
                PayloadMindMapCanvas(
                    nodes = typedNodes,
                    modifier = canvasModifier,
                    state = viewport,
                    style = style,
                    behavior = behavior,
                    layoutEngine = layout,
                    edgeRenderer = edge,
                    selectedNodeId = selected,
                    editMode = true,
                    collapsedNodeIds = collapsed,
                    nodeSize = { if (it.node.id == "root") DpSize(196.dp, 84.dp) else DpSize(160.dp, 72.dp) },
                    nodeContent = { item, visualState -> PayloadCard(item, visualState) },
                    onNodeClick = onSelect,
                    onCanvasClick = { selected = null },
                    onAddChildClick = ::addChild,
                    onNodeMove = onMove,
                )
            } else {
                MindMapCanvas(
                    nodes = nodes,
                    modifier = canvasModifier,
                    state = viewport,
                    style = style,
                    behavior = behavior,
                    layoutEngine = layout,
                    edgeRenderer = edge,
                    selectedNodeId = selected,
                    editMode = true,
                    collapsedNodeIds = collapsed,
                    onNodeClick = onSelect,
                    onCanvasClick = { selected = null },
                    onAddChildClick = ::addChild,
                    onNodeMove = onMove,
                )
            }
        }
    }
}

@Composable
private fun ControlRow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = content,
    )
}

@Composable
private fun Control(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
    ) {
        Text(label)
    }
}

@Composable
private fun PayloadCard(item: MindMapNodeWithPayload<TopicPayload>, visualState: MindMapNodeVisualState) {
    Surface(
        modifier = Modifier.fillMaxSize().padding(2.dp),
        shape = MaterialTheme.shapes.medium,
        color = if (visualState.isSelected) Color(0xFFD3E8FF) else Color(0xFFFFF5DB),
        shadowElevation = 3.dp,
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(item.payload.category, style = MaterialTheme.typography.labelSmall, color = Color(0xFF775000))
            Text(item.node.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            if (visualState.isCollapsed) Text("Collapsed", style = MaterialTheme.typography.labelSmall)
        }
    }
}

private val sampleNodes = listOf(
    MindMapNode("root", "Build an idea", subtitle = "Compose MindMap"),
    MindMapNode("plan", "Plan", subtitle = "Structure", parentId = "root"),
    MindMapNode("design", "Design", subtitle = "Cards & colors", parentId = "root"),
    MindMapNode("share", "Share", subtitle = "Publish", parentId = "root"),
    MindMapNode("layout", "Layouts", parentId = "plan"),
    MindMapNode("gestures", "Gestures", parentId = "plan"),
    MindMapNode("payload", "Payload", parentId = "design"),
    MindMapNode("docs", "Docs", parentId = "share"),
)
