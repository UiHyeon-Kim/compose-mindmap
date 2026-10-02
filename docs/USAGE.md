# Usage guide

This guide expands the [README quick start](../README.md#quick-start) with complete examples for custom Compose cards, app-owned edits, and viewport controls. The library accepts a flat list of nodes; your app keeps that list as its source of truth.

## AGP 9.1 build settings

This repository uses AGP 9.1.0 with `android.builtInKotlin=false` and `android.newDsl=false` in its root [gradle.properties](../gradle.properties). The standalone consumer fixture mirrors those settings. If your consumer also uses AGP 9.1.0, use the same properties in its root `gradle.properties`; validate the settings against your chosen AGP before copying them to a different version.

## Tree data

Provide exactly one root (`parentId = null`), a unique non-empty `id` for every node, and a parent ID that exists in the same list for every non-root node. Input is validated before layout. Invalid input calls `onValidationError` and renders the default localized error message unless you supply `errorContent`.

## Custom cards and `nodeSize`

`nodeContent` replaces the default Canvas card renderer with Compose UI. Tell the layout engine the size of each card using `nodeSize`; the dimensions must match the content's measured size so spacing, hit testing, and connector endpoints line up.

This example uses Material 3 components. Add Material 3 to the app module; the BOM version below matches this repository's sample catalog in [`gradle/libs.versions.toml`](../gradle/libs.versions.toml):

```kotlin
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.03.01"))
    implementation("androidx.compose.material3:material3")
}
```

If your app already imports a Compose BOM, keep its existing version and add only the Material 3 artifact. You can also replace these components with your own Compose design-system components.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

@Composable
fun CustomCardMindMap(nodes: List<MindMapNode>) {
    MindMapCanvas(
        nodes = nodes,
        modifier = Modifier.fillMaxSize(),
        nodeSize = { node ->
            if (node.id == "root") DpSize(196.dp, 88.dp) else DpSize(160.dp, 72.dp)
        },
        nodeContent = { node, visualState ->
            val colors = MaterialTheme.colorScheme
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = if (visualState.isSelected) colors.primaryContainer else colors.surfaceVariant,
                tonalElevation = if (visualState.isSelected) 4.dp else 1.dp,
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(node.title, style = MaterialTheme.typography.titleSmall)
                    if (node.subtitle.isNotBlank()) {
                        Text(node.subtitle, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
    )
}
```

The library keeps the node's accessibility actions and label around custom content. Use `semanticLabelProvider` to tailor the node description, or `accessibilityActionLabels` to replace the localized action names. Avoid adding a second accessibility node for the same card inside `nodeContent`.

## App-owned editing and undo/redo

`MindMapCanvas` reports user intent through callbacks. Update your own node list with `MindMapEditController`, then pass the new list back to the Canvas. The controller stores only in-memory undo/redo commands; persist node data separately and decide how your app should restore history after process death.

```kotlin
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
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.controller.MindMapEditController
import io.github.hanhyo.composemindmap.model.MindMapNode

private val initialNodes = listOf(
    MindMapNode(id = "root", title = "My idea"),
    MindMapNode(id = "research", title = "Research", parentId = "root"),
)

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
```

Set `editMode = true` to show add-child affordances and permit node dragging. Select a node before dragging it to another branch; `onNodeMove` passes the node ID and proposed parent ID to your app, and the example applies `editor.moveNode`. The controller rejects moves that would create a cycle. The example also owns `collapsedNodeIds` and toggles the selected node when it has children; collapse state is separate from the controller's edit history.

## Saveable viewport controls

Use `rememberSaveableMindMapCanvasState()` to save scale, translation, and initial alignment across Activity recreation. The viewport does not save your node list. The regular `rememberMindMapCanvasState()` variant keeps state only for the current composition.

```kotlin
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.canvas.rememberSaveableMindMapCanvasState
import io.github.hanhyo.composemindmap.model.MindMapNode

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
```

`focusNode` centers the requested node and zooms out as needed to fit it within the requested padding, subject to `MindMapBehavior.minScale`. If the whole tree is larger than the viewport at the minimum scale, some nodes may remain off-screen; use `fitContent`, pan, or adjust the behavior's scale bounds.

## More examples

- The [sample activity](../sample/src/main/java/io/github/hanhyo/composemindmap/sample/SampleActivity.kt) shows typed payload cards, layout and edge selection, collapse, and viewport controls.
- [Migration from 0.1.1](MIGRATION-0.2.0.md) summarizes API changes.
- Browse the [library source](../compose-mindmap/src/main/java/io/github/hanhyo/composemindmap) for API declarations.
