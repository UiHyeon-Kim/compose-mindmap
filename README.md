# Compose MindMap

Jetpack Compose canvas-based mind map library for Android.

[![](https://jitpack.io/v/UiHyeon-Kim/compose-mindmap.svg)](https://jitpack.io/#UiHyeon-Kim/compose-mindmap)

## Setup

```kotlin
// settings.gradle.kts
repositories { maven("https://jitpack.io") }

// build.gradle.kts
dependencies {
    implementation("com.github.UiHyeon-Kim:compose-mindmap:0.1.0")
}
```

## Quick Start

```kotlin
val nodes = listOf(
    MindMapNode(id = "root", title = "My Map"),
    MindMapNode(id = "a", title = "Topic A", parentId = "root"),
    MindMapNode(id = "b", title = "Topic B", parentId = "root"),
)

MindMapCanvas(
    nodes = nodes,
    modifier = Modifier.fillMaxSize(),
)
```

## Typed Payload

```kotlin
data class BookPayload(val coverUrl: String?)

val nodes = listOf(
    MindMapNode(id = "root", title = "Reading List")
        .withPayload(BookPayload(coverUrl = null)),
    MindMapNode(id = "book1", title = "Demian", parentId = "root")
        .withPayload(BookPayload(coverUrl = "https://...")),
)

PayloadMindMapCanvas(
    nodes = nodes,
    nodeContent = { item, visualState ->
        // item.node, item.payload available
    },
)
```

## Viewport Control

```kotlin
val state = rememberSaveableMindMapCanvasState()

MindMapCanvas(nodes = nodes, state = state)

// Control viewport
Button(onClick = { state.centerRoot() }) { Text("Center") }
Button(onClick = { state.fitContent() }) { Text("Fit") }
Button(onClick = { state.focusNode("book1") }) { Text("Focus") }
```

## Edit Policy

```kotlin
val policy = object : MindMapEditPolicy {
    override fun canAddChild(node: MindMapNode) = node.parentId != null
    override fun canDrag(node: MindMapNode) = node.parentId != null
    override fun canDrop(dragged: MindMapNode, target: MindMapNode) = true
}

MindMapCanvas(nodes = nodes, editPolicy = policy)
```

## Collapse

```kotlin
var collapsed by remember { mutableStateOf(emptySet<String>()) }

MindMapCanvas(
    nodes = nodes,
    collapsedNodeIds = collapsed,
    nodeContent = { node, visualState ->
        NodeCard(
            node = node,
            isCollapsed = visualState.isCollapsed,
            hasChildren = visualState.hasChildren,
            onToggle = {
                collapsed = if (node.id in collapsed)
                    collapsed - node.id else collapsed + node.id
            },
        )
    },
)
```

## Layout Engines

| Engine | Alignment |
|---|---|
| `TopDownTreeLayoutEngine` | Top-center root |
| `LeftToRightTreeLayoutEngine` | Center-start root |

## License

Apache-2.0 — see [LICENSE](LICENSE)
