# Changelog

## [0.1.0] - 2026-05-31

Initial preview release.

### Added
- Compose Canvas-based mind map rendering
- `MindMapCanvas` and `PayloadMindMapCanvas<T>` composables
- Top-down (`TopDownTreeLayoutEngine`) and left-to-right (`LeftToRightTreeLayoutEngine`) layout engines
- Curved (`CurvedMindMapEdgeRenderer`), straight (`StraightMindMapEdgeRenderer`), and orthogonal
  (`OrthogonalMindMapEdgeRenderer`) edge renderers
- Tree validation with `validateMindMapNodes()`
- Undo/redo editing via `MindMapEditController`
- `MindMapEditPolicy`: gate add-child, drag, and drop per node
- `MindMapAddChildActionLayout` + `MindMapEditDecorationRenderer`: customisable add-button placement and rendering
- Viewport API: `centerRoot()`, `fitContent()`, `focusNode()`, `rememberSaveableMindMapCanvasState()`
- `InitialViewportPolicy` (ROOT_ALIGNED / FIT_CONTENT / NONE)
- Controlled collapse via `collapsedNodeIds: Set<String>`
- `MindMapNodeVisualState.hasChildren` and `isCollapsed`
- Accessibility semantics overlay with `MindMapSemanticLabelProvider`
- Viewport culling for Canvas draw and slot composition

### Package
`io.github.hanhyo.composemindmap`

### Artifact
```kotlin
repositories { maven("https://jitpack.io") }
dependencies {
    implementation("com.github.UiHyeon-Kim:compose-mindmap:0.1.0")
}
```
