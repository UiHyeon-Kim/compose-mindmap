# Changelog

## 0.2.0 — pending tag

- Pinch zoom follows the gesture centroid and supports two-finger panning; single-finger pan and node drag remain separate
- Restored viewport is preserved, and `focusNode` applies its padding
- `MindMapCanvasState.zoomBy` provides center-anchored programmatic zoom
- Invalid trees show configurable error content and trigger validation callbacks; collapsed branches are validated too
- Drag/drop rejects descendants, node slots are keyed by ID, and density changes recompute layout
- Accessible labels can use node visual state; action names are localized and overridable; add-child touch target is at least 48 dp
- Sample app now demonstrates layout/edge/style/payload/edit/collapse/viewport controls
- README, Korean guide, migration guide, CI, and contribution guidance added

## [0.1.1] - 2026-06-01

### Added
- Orthogonal edge renderer (`OrthogonalMindMapEdgeRenderer`)

### Changed
- Curved edges now leave and enter nodes in the layout direction before transitioning into a curve

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
