# Compose MindMap

An Android Jetpack Compose library for interactive, editable **tree-shaped** mind maps. Start with plain `MindMapNode` data, then customize layout, edges, cards, gestures, and viewport behavior.

![Compose MindMap running on an API 36 emulator](docs/images/sample.png)

## See it in action

### Tree layouts and edge renderers

![The sample switches tree layout and edge style](docs/images/layout-edge.gif)

`MindMapLayoutEngine` changes the tree between left-to-right and top-down; `MindMapEdgeRenderer` selects curved, straight, or elbow connectors. In the sample, open `Controls → Display → Layout: Left → right / Top → down` and `Edge: Curve / Straight / Elbow`. The sample fits the tree after a layout change, so the resulting scale depends on the layout.

### Default Canvas cards and typed-payload cards

![Left: default Canvas cards. Right: custom cards rendered from typed-payload nodes](docs/images/default-vs-payload.png)

The left screen uses the default Canvas renderer. The right uses `PayloadMindMapCanvas`, `withPayload`, `nodeContent`, and `nodeSize` for typed data and custom Compose cards. Compare them in the sample at `Controls → Display → Card: Canvas / Payload`.

### Add child, undo, and redo

![The sample adds a child node, undoes it, and redoes it](docs/images/edit.gif)

`MindMapEditController` records edits while your app owns the updated node list. In the sample, select a node, then choose `Controls → Selected node → Add child`; use `Controls → History → Undo / Redo` to reverse and reapply that edit. This demo shows only add-child, undo, and redo.

### Zoom and fit the tree

![The sample zooms into the tree and fits the full content](docs/images/zoom-fit.gif)

`MindMapCanvasState.zoomBy()` and `fitContent()` control the viewport. In the sample, choose `Controls → Viewport → Zoom + / Fit all`. Zooming can move nodes outside the visible viewport; `Fit all` brings the full tree back into view.

## Run the sample

Open the repository in Android Studio and run the `sample` configuration, or install it on an API 26+ emulator/device:

```bash
./gradlew :sample:installDebug
```

The sample uses the library project directly, so it works before a published artifact is available. Its controls cover layout, edge, card, editing, history, and viewport changes. The source is in [SampleActivity.kt](sample/src/main/java/io/github/hanhyo/composemindmap/sample/SampleActivity.kt).

## Install

`0.2.0` is prepared in this source tree but has **not been published**. The JitPack coordinate below becomes usable only after a `0.2.0` Git tag is published and its build succeeds. For now, run the source sample above.

Requires an Android app using Jetpack Compose, minSdk **26**, Java **17**, and JitPack in `settings.gradle.kts`:

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

Then add this dependency to the app module's `build.gradle.kts` after publication:

```kotlin
dependencies {
    implementation("com.github.UiHyeon-Kim:compose-mindmap:0.2.0")
}
```

See the [JitPack build page](https://jitpack.io/#UiHyeon-Kim/compose-mindmap) and [Android setup guide](https://docs.jitpack.io/android/) when the tag is available.

## Quick start

This complete composable can be pasted into a Compose screen:

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.hanhyo.composemindmap.canvas.MindMapCanvas
import io.github.hanhyo.composemindmap.model.MindMapNode

@Composable
fun MyMindMap() {
    val nodes = listOf(
        MindMapNode(id = "idea", title = "My idea"),
        MindMapNode(id = "research", title = "Research", parentId = "idea"),
        MindMapNode(id = "build", title = "Build", parentId = "idea"),
    )

    MindMapCanvas(nodes = nodes, modifier = Modifier.fillMaxSize())
}
```

Supply one root (`parentId = null`), unique IDs, and a valid parent for every other node. Invalid input invokes `onValidationError` and displays the default error content; replace it with `errorContent` when needed.

## Next steps

- [Usage guide](docs/USAGE.md): custom cards and `nodeSize`, app-owned editing with undo/redo, and saveable viewport controls
- [Migration from 0.1.1](docs/MIGRATION-0.2.0.md)
- [Korean guide](docs/README.ko.md)
- [Changelog](CHANGELOG.md) · [Contributing](CONTRIBUTING.md) · [Apache-2.0 license](LICENSE)

Compose MindMap currently supports one rooted tree, two built-in tree layouts, and three edge renderers. It does not provide radial or bidirectional layouts, cross-links, import/export, or Compose Multiplatform support. Large-tree performance has not been benchmarked.
