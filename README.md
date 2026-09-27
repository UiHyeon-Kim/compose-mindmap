# Compose MindMap

An Android Jetpack Compose library for interactive, editable **tree-shaped** mind maps. Start with plain `MindMapNode` data, then change the layout, edges, colors, gestures, and even the entire node card.

![Running sample on Android API 36](docs/images/sample.png)

![Editing a node and changing zoom in the running sample](docs/images/edit-and-zoom.gif)

The [sample app](sample/src/main/java/io/github/hanhyo/composemindmap/sample/SampleActivity.kt) lets you try layout and edge switching, colored and typed-payload cards, collapse, add/move/delete, undo/redo, and viewport controls. [한국어 안내](docs/README.ko.md) · [Migration from 0.1.1](docs/MIGRATION-0.2.0.md)

> `0.2.0` is prepared in this source tree. The JitPack coordinate below becomes available after the `0.2.0` Git tag is published and its build succeeds. Until then, run the source sample.

## Install

Requires an Android app with Jetpack Compose, minSdk **26**, Java **17**, and access to JitPack. In your app's `settings.gradle.kts`:

```kotlin
import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

In your app module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.UiHyeon-Kim:compose-mindmap:0.2.0")
}
```

The exact tag/coordinate is case-sensitive. If Gradle cannot resolve it, check the [JitPack build](https://jitpack.io/#UiHyeon-Kim/compose-mindmap) and [JitPack's Android setup guide](https://docs.jitpack.io/android/). The sample uses `implementation(project(":compose-mindmap"))` so you can run it before publishing.

## Quick start

Paste this composable into a Compose screen:

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

@Composable
fun MyMindMap() {
    val nodes = listOf(
        MindMapNode(id = "root", title = "My idea"),
        MindMapNode(id = "research", title = "Research", parentId = "root"),
        MindMapNode(id = "build", title = "Build", parentId = "root"),
    )
    MindMapCanvas(nodes = nodes, modifier = Modifier.fillMaxSize())
}
```

Provide exactly one root (`parentId = null`), unique IDs, and a parent for every other node. Invalid graphs call `onValidationError` and show a default error message; replace it using `errorContent = { error -> … }`.

## Customize the appearance

For the default Canvas cards, change global colors with `MindMapStyle` and individual colors with `MindMapNode.color`:

```kotlin
MindMapCanvas(
    nodes = nodes.map { if (it.id == "research") it.copy(color = Color(0xFFFFE5B4)) else it },
    style = MindMapStyle(defaultNodeColor = Color(0xFFEAF4FF)),
)
```

For fully custom Compose cards, use the `nodeContent` slot **and** `nodeSize` so layout and hit testing agree with your card dimensions:

```kotlin
MindMapCanvas(
    nodes = nodes,
    nodeSize = { node -> if (node.id == "root") DpSize(192.dp, 80.dp) else DpSize(152.dp, 64.dp) },
    nodeContent = { node, visual ->
        Surface(color = if (visual.isSelected) Color.Cyan else Color.White) {
            Text(node.title, modifier = Modifier.padding(12.dp))
        }
    },
)
```

Import `Color`, `DpSize`, `dp`, `Surface`, `Text`, and `padding` from Compose. The library supplies the accessible node actions and label for custom cards; override `semanticLabelProvider = MindMapSemanticLabelProvider { node, visual -> … }` to describe selected/collapsed state, or `accessibilityActionLabels` to replace the localized action names. Custom child semantics are suppressed so TalkBack does not announce the same card twice.

Use `PayloadMindMapCanvas` if your app has typed per-node data. The sample's [payload card](sample/src/main/java/io/github/hanhyo/composemindmap/sample/SampleActivity.kt) demonstrates `withPayload`, a custom `nodeSize`, and `nodeContent`. You can also provide `layoutEngine` (`TopDownTreeLayoutEngine` or `LeftToRightTreeLayoutEngine`) and `edgeRenderer` (`CurvedMindMapEdgeRenderer`, `StraightMindMapEdgeRenderer`, or `OrthogonalMindMapEdgeRenderer`). `MindMapBehavior` controls zoom/pan, scale bounds, edit affordances, and initial viewport policy.

## Edit and collapse

The Canvas reports user intent; **your app owns the node list**. `MindMapEditController` applies edits and keeps undo/redo history. Pass `editMode = true` to show add-child actions and enable dragging. Tap a node first, then drag it onto a different branch. Dropping on the node itself or a descendant is rejected.

```kotlin
val editor = remember { MindMapEditController() }
var nodes by remember { mutableStateOf(initialNodes) }
var selected by remember { mutableStateOf<String?>(null) }
var collapsed by remember { mutableStateOf(emptySet<String>()) }

MindMapCanvas(
    nodes = nodes,
    selectedNodeId = selected,
    editMode = true,
    collapsedNodeIds = collapsed,
    onNodeClick = { selected = it },
    onAddChildClick = { parentId ->
        nodes = editor.addNode(nodes, MindMapNode(id = newUniqueId(), title = "New topic", parentId = parentId))
    },
    onNodeMove = { nodeId, parentId -> nodes = editor.moveNode(nodes, nodeId, parentId) },
)

// In your own buttons or menu:
nodes = editor.undo(nodes) ?: nodes
selected?.let { collapsed = if (it in collapsed) collapsed - it else collapsed + it }
```

The snippet assumes your app provides `initialNodes` and `newUniqueId()`; see the runnable sample for complete add/rename/delete/redo actions. The controller is in-memory: persist your node list and undo history separately if required by your app.

## Restore and control the viewport

Use the saveable state variant for Activity recreation; the initial alignment will not overwrite a restored scale/offset. Save your **nodes** separately in your app.

```kotlin
val viewport = rememberSaveableMindMapCanvasState()
MindMapCanvas(nodes = nodes, state = viewport)

Button(onClick = { viewport.centerRoot() }) { Text("Center") }
Button(onClick = { viewport.fitContent(24.dp) }) { Text("Fit") }
Button(onClick = { viewport.zoomBy(1.4f) }) { Text("Zoom in") }
Button(onClick = { viewport.focusNode("research", padding = 32.dp) }) { Text("Focus") }
```

`focusNode` centers the chosen node and, if necessary, zooms out so it fits inside the requested screen padding, subject to `MindMapBehavior.minScale`. Pinch zoom is centered under the fingers and supports two-finger translation. Pan with one finger on empty canvas. If the map is larger than the viewport at `minScale`, some content can remain off-screen; lower `minScale` or pan to reach it.

## Run the sample and tests

Open this repository in Android Studio, select the `sample` run configuration, and launch an emulator/device running API 26 or newer. Or run:

```bash
./gradlew :sample:installDebug
./gradlew :compose-mindmap:testDebugUnitTest :compose-mindmap:lintDebug :compose-mindmap:assembleRelease :sample:assembleDebug
```

Connected UI tests: `./gradlew :compose-mindmap:connectedDebugAndroidTest`.

## Scope and limitations

- Android-only Jetpack Compose library for **one rooted tree**. No radial/bidirectional graph layout, cross-links, or image/JSON export yet
- Two built-in tree layouts, three edge renderers, replaceable card drawing/slot, and callback-driven editing
- Large trees are not yet benchmarked; node content and semantics can be expensive at scale
- The sample uses source-module dependency; a separate [consumer smoke project](consumer-smoke/README.md) is reserved for verification after JitPack publishes the tag

See [CHANGELOG](CHANGELOG.md), [contribution guide](CONTRIBUTING.md), and [Apache-2.0 license](LICENSE).
