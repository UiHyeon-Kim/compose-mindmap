package io.github.hanhyo.composemindmap.consumer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.canvas.rememberSaveableMindMapCanvasState
import io.github.hanhyo.composemindmap.controller.MindMapEditController
import io.github.hanhyo.composemindmap.model.MindMapNode

private val initialNodes = listOf(
    MindMapNode(id = "root", title = "My idea"),
    MindMapNode(id = "research", title = "Research", parentId = "root"),
)

// Compile fixtures copied from the complete app-owned editing and viewport examples in docs/USAGE.md.
@Composable
fun EditableMindMap() {
    val editor = remember { MindMapEditController() }
    var nodes by remember { mutableStateOf(initialNodes) }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }
    var nextNodeId by remember { mutableIntStateOf(1) }
    var collapsedNodeIds by remember { mutableStateOf(emptySet<String>()) }
    val selectedIsCollapsed = selectedNodeId?.let { it in collapsedNodeIds } == true

    Column(modifier = Modifier.fillMaxSize()) {
        MindMapCanvas(
            nodes = nodes,
            modifier = Modifier.weight(1f),
            editMode = true,
            selectedNodeId = selectedNodeId,
            collapsedNodeIds = collapsedNodeIds,
            onNodeClick = { selectedNodeId = it },
            onAddChildClick = { parentId ->
                val child = MindMapNode(
                    id = "topic-${nextNodeId++}",
                    title = "New topic",
                    parentId = parentId,
                )
                nodes = editor.addNode(nodes, child)
            },
            onNodeMove = { nodeId, newParentId ->
                nodes = editor.moveNode(nodes, nodeId, newParentId)
            },
        )

        Row {
            Button(
                onClick = {
                    selectedNodeId?.let { nodeId ->
                        collapsedNodeIds = if (nodeId in collapsedNodeIds) {
                            collapsedNodeIds - nodeId
                        } else {
                            collapsedNodeIds + nodeId
                        }
                    }
                },
                enabled = selectedNodeId != null && nodes.any { it.parentId == selectedNodeId },
            ) {
                Text(if (selectedIsCollapsed) "Expand" else "Collapse")
            }
            Button(
                onClick = { nodes = editor.undo(nodes) ?: nodes },
                enabled = editor.canUndo,
            ) {
                Text("Undo")
            }
            Button(
                onClick = { nodes = editor.redo(nodes) ?: nodes },
                enabled = editor.canRedo,
            ) {
                Text("Redo")
            }
        }
    }
}

@Composable
fun MindMapWithViewportControls(nodes: List<MindMapNode>) {
    val viewport = rememberSaveableMindMapCanvasState()

    Column(modifier = Modifier.fillMaxSize()) {
        MindMapCanvas(
            nodes = nodes,
            modifier = Modifier.weight(1f),
            state = viewport,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewport.centerRoot() }) { Text("Center root") }
            Button(onClick = { viewport.fitContent(padding = 24.dp) }) { Text("Fit all") }
            Button(onClick = { viewport.zoomBy(1.4f) }) { Text("Zoom in") }
            Button(onClick = { viewport.focusNode("research", padding = 32.dp) }) {
                Text("Focus research")
            }
        }
    }
}
