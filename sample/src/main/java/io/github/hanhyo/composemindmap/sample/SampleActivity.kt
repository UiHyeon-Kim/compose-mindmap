package io.github.hanhyo.composemindmap.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Showcase() {
    val viewport = rememberSaveableMindMapCanvasState()
    val editor = remember { MindMapEditController() }
    var nodes by remember { mutableStateOf(sampleNodes) }
    var selected by remember { mutableStateOf<String?>(null) }
    var collapsed by remember { mutableStateOf(emptySet<String>()) }
    var nextId by remember { mutableIntStateOf(1) }
    var leftToRight by remember { mutableStateOf(true) }
    var edgeIndex by remember { mutableIntStateOf(0) }
    var customCards by remember { mutableStateOf(false) }
    var colored by remember { mutableStateOf(true) }
    var showControls by remember { mutableStateOf(false) }

    val layout: MindMapLayoutEngine = if (leftToRight) LeftToRightTreeLayoutEngine else TopDownTreeLayoutEngine
    val edge: MindMapEdgeRenderer = when (edgeIndex) {
        1 -> StraightMindMapEdgeRenderer
        2 -> OrthogonalMindMapEdgeRenderer
        else -> CurvedMindMapEdgeRenderer
    }
    val baseStyle = MindMapStyle(nodeHeight = 80.dp, verticalGap = 64.dp)
    val style = if (colored) baseStyle.copy(
        defaultNodeColor = Color(0xFFEAF4FF),
        selectedStrokeColor = Color(0xFF2764B6),
        edgeColor = Color(0xFF85A9D4),
    ) else baseStyle
    val behavior = MindMapBehavior(minScale = 0.35f, initialViewportPolicy = InitialViewportPolicy.FIT_CONTENT)

    fun addChild(parentId: String) {
        nodes = editor.addNode(nodes, MindMapNode("new-${nextId++}", "New topic", parentId = parentId))
    }

    val selectedNode = nodes.firstOrNull { it.id == selected }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
        ) {
            val canvasModifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 8.dp, vertical = 4.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Compose MindMap", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = selectedNode?.let {
                            "${it.title} selected · drag to move · pinch to zoom"
                        } ?: "Tap to select · drag to move · pinch to zoom",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                TextButton(onClick = { showControls = true }) {
                    Text("Controls")
                }
            }

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

            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = { viewport.centerRoot() }) { Text("Center root") }
                    TextButton(onClick = { viewport.fitContent() }) { Text("Fit all") }
                    TextButton(onClick = { viewport.zoomBy(1f / 1.4f) }) { Text("Zoom −") }
                    TextButton(onClick = { viewport.zoomBy(1.4f) }) { Text("Zoom +") }
                }
            }
        }

        if (showControls) {
            ModalBottomSheet(onDismissRequest = { showControls = false }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Mind map controls", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = selectedNode?.let { "Selected: ${it.title}" }
                                    ?: "Select a node to enable node actions",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                "Scroll for display, node and history actions",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = { showControls = false }) { Text("Done") }
                    }

                    ControlGroup("Viewport") {
                        Control("Center root") { viewport.centerRoot(); showControls = false }
                        Control("Fit all") { viewport.fitContent(); showControls = false }
                        Control("Zoom +") { viewport.zoomBy(1.4f); showControls = false }
                        Control("Zoom −") { viewport.zoomBy(1f / 1.4f); showControls = false }
                        Control("Focus selected", selected != null) {
                            selected?.let { viewport.focusNode(it, 48.dp) }
                            showControls = false
                        }
                    }

                    ControlGroup("Display") {
                        Control("Layout: ${if (leftToRight) "Left → right" else "Top → down"}") {
                            leftToRight = !leftToRight
                            showControls = false
                        }
                        Control("Edge: ${listOf("Curve", "Straight", "Elbow")[edgeIndex]}") {
                            edgeIndex = (edgeIndex + 1) % 3
                            showControls = false
                        }
                        Control("Card: ${if (customCards) "Payload" else "Canvas"}") {
                            customCards = !customCards
                            showControls = false
                        }
                        Control("Colors: ${if (colored) "On" else "Off"}") {
                            colored = !colored
                            showControls = false
                        }
                    }

                    ControlGroup("Selected node") {
                        Control("Add child", selected != null) {
                            selected?.let(::addChild)
                            showControls = false
                        }
                        Control("Rename", selected != null) {
                            val node = nodes.firstOrNull { it.id == selected } ?: return@Control
                            nodes = editor.updateNode(nodes, node, node.copy(title = "Edited topic"))
                            showControls = false
                        }
                        Control(
                            if (selected in collapsed) "Expand" else "Collapse",
                            selected != null,
                        ) {
                            selected?.let { collapsed = if (it in collapsed) collapsed - it else collapsed + it }
                            showControls = false
                        }
                        Control("Delete", selected != null && selected != "root") {
                            val node = nodes.firstOrNull { it.id == selected } ?: return@Control
                            nodes = editor.deleteNode(nodes, node, editor.collectSubtree(nodes, node.id))
                            collapsed = collapsed - node.id
                            selected = null
                            showControls = false
                        }
                    }

                    ControlGroup("History") {
                        Control("Undo", editor.canUndo) {
                            editor.undo(nodes)?.let { nodes = it }
                            showControls = false
                        }
                        Control("Redo", editor.canRedo) {
                            editor.redo(nodes)?.let { nodes = it }
                            showControls = false
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControlGroup(title: String, content: @Composable FlowRowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        ControlRow(content)
    }
}

@Composable
private fun ControlRow(content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
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
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
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
