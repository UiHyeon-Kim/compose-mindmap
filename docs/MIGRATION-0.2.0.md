# Migrate from 0.1.1 to 0.2.0

`0.2.0` keeps the Android tree-map model and existing renderers. The intentional source-breaking change is the accessibility label provider, which now receives visual state so the label can describe selection and collapse

```kotlin
// 0.1.1
semanticLabelProvider = MindMapSemanticLabelProvider { node -> node.title }

// 0.2.0
semanticLabelProvider = MindMapSemanticLabelProvider { node, visual ->
    buildString {
        append(node.title)
        if (visual.isSelected) append(", selected")
        if (visual.isCollapsed) append(", collapsed")
    }
}
```

`MindMapCanvas` and `PayloadMindMapCanvas` have two new optional parameters: `accessibilityActionLabels` to override localized action names, and `errorContent` to render a custom invalid-tree message. If you passed arguments positionally after `semanticLabelProvider`, switch to named arguments

The default error view is no longer blank. Validation is performed on the full input tree, including branches hidden by `collapsedNodeIds`. `focusNode(id, padding)` now uses the padding and may reduce the current zoom to fit the focused card. Saveable viewport state keeps initial alignment from overwriting a restored position. The default add-child hit area is at least 48 dp in diameter

`MindMapCanvasState` now rejects non-finite or non-positive initial zoom values, and `MindMapBehavior` requires finite zoom limits. Replace invalid sentinel values with real positive bounds. `MindMapCanvasState.zoomBy(factor)` is a new center-anchored viewport command and requires a finite positive factor

Update the dependency to `com.github.UiHyeon-Kim:compose-mindmap:0.2.0` after the Git tag and JitPack build are available. See [README](../README.md#install) for the full repository setup
